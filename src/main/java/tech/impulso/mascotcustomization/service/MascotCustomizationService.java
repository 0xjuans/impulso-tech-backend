package tech.impulso.mascotcustomization.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.admin.service.AdminActivityLogger;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.gamification.entity.Badge;
import tech.impulso.gamification.entity.UserXp;
import tech.impulso.gamification.repository.BadgeRepository;
import tech.impulso.gamification.repository.UserBadgeRepository;
import tech.impulso.gamification.repository.UserXpRepository;
import tech.impulso.mascotcustomization.dto.CatalogItemResponse;
import tech.impulso.mascotcustomization.dto.CustomizationResponse;
import tech.impulso.mascotcustomization.dto.MascotItemRequest;
import tech.impulso.mascotcustomization.entity.MascotCustomization;
import tech.impulso.mascotcustomization.entity.MascotItem;
import tech.impulso.mascotcustomization.entity.MascotItemSlot;
import tech.impulso.mascotcustomization.entity.MascotUnlock;
import tech.impulso.mascotcustomization.entity.MascotUnlockType;
import tech.impulso.mascotcustomization.repository.MascotCustomizationRepository;
import tech.impulso.mascotcustomization.repository.MascotItemRepository;
import tech.impulso.mascotcustomization.repository.MascotUnlockRepository;
import tech.impulso.users.entity.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servicio que gestiona el catálogo de ítems de la mascota, el
 * desbloqueo automático por progreso y la personalización activa por
 * usuario (RF-022, RF-050, RF-052).
 *
 * <p>El backend es la autoridad sobre los desbloqueos: los ítems se
 * conceden únicamente cuando el estudiante cumple el requisito
 * asociado en {@link MascotItem}. Nunca se utiliza una moneda interna
 * (§31 del CLAUDE.md).</p>
 */
@Service
public class MascotCustomizationService {

    private static final String TARGET_ITEM = "MASCOT_ITEM";
    private static final String ACTION_CREATED = "MASCOT_ITEM_CREATED";
    private static final String ACTION_UPDATED = "MASCOT_ITEM_UPDATED";
    private static final String ACTION_DELETED = "MASCOT_ITEM_DELETED";

    private final MascotItemRepository itemRepository;
    private final MascotUnlockRepository unlockRepository;
    private final MascotCustomizationRepository customizationRepository;
    private final BadgeRepository badgeRepository;
    private final UserXpRepository userXpRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final CurrentUserService currentUserService;
    private final AdminActivityLogger activityLogger;

    public MascotCustomizationService(MascotItemRepository itemRepository,
                                      MascotUnlockRepository unlockRepository,
                                      MascotCustomizationRepository customizationRepository,
                                      BadgeRepository badgeRepository,
                                      UserXpRepository userXpRepository,
                                      UserBadgeRepository userBadgeRepository,
                                      CurrentUserService currentUserService,
                                      AdminActivityLogger activityLogger) {
        this.itemRepository = itemRepository;
        this.unlockRepository = unlockRepository;
        this.customizationRepository = customizationRepository;
        this.badgeRepository = badgeRepository;
        this.userXpRepository = userXpRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.currentUserService = currentUserService;
        this.activityLogger = activityLogger;
    }

    /**
     * Devuelve el catálogo con el estado (desbloqueado / equipado) del
     * usuario autenticado, evaluando previamente los desbloqueos
     * pendientes.
     */
    @Transactional
    public List<CatalogItemResponse> catalog() {
        User user = currentUserService.requireAuthenticatedUser();
        evaluateUnlocks(user);

        Set<Long> unlocked = new HashSet<>(unlockRepository.findUnlockedItemIds(user.getId()));
        MascotCustomization customization = customizationRepository.findById(user.getId()).orElse(null);
        Set<Long> equippedIds = equippedItemIds(customization);

        return itemRepository.findByActiveTrueOrderBySlotAscNameAsc().stream()
                .map(item -> CatalogItemResponse.of(item,
                        unlocked.contains(item.getId()),
                        equippedIds.contains(item.getId())))
                .toList();
    }

    /**
     * Devuelve la personalización actual del usuario autenticado.
     */
    @Transactional(readOnly = true)
    public CustomizationResponse myCustomization() {
        User user = currentUserService.requireAuthenticatedUser();
        MascotCustomization customization = customizationRepository.findById(user.getId()).orElse(null);
        return CustomizationResponse.from(customization);
    }

    /**
     * Equipa el ítem indicado en su ranura correspondiente. Requiere que
     * el usuario tenga el ítem desbloqueado.
     */
    @Transactional
    public CustomizationResponse equip(Long itemId) {
        User user = currentUserService.requireAuthenticatedUser();
        MascotItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "El ítem indicado no existe."));
        if (!item.isActive()) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "El ítem indicado ya no está disponible en el catálogo.");
        }
        if (!unlockRepository.existsByUserIdAndItemId(user.getId(), itemId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Debes desbloquear este ítem antes de poder equiparlo.");
        }

        MascotCustomization customization = customizationRepository.findById(user.getId())
                .orElseGet(() -> {
                    MascotCustomization created = new MascotCustomization();
                    created.setUserId(user.getId());
                    return created;
                });

        switch (item.getSlot()) {
            case SKIN -> customization.setSkin(item);
            case HAT -> customization.setHat(item);
            case ACCESSORY -> customization.setAccessory(item);
            case BACKGROUND -> customization.setBackground(item);
        }
        return CustomizationResponse.from(customizationRepository.save(customization));
    }

    /**
     * Retira el ítem equipado en la ranura indicada, dejándola en su
     * aspecto por defecto.
     */
    @Transactional
    public CustomizationResponse unequip(MascotItemSlot slot) {
        User user = currentUserService.requireAuthenticatedUser();
        MascotCustomization customization = customizationRepository.findById(user.getId()).orElse(null);
        if (customization == null) {
            return CustomizationResponse.from(null);
        }
        switch (slot) {
            case SKIN -> customization.setSkin(null);
            case HAT -> customization.setHat(null);
            case ACCESSORY -> customization.setAccessory(null);
            case BACKGROUND -> customization.setBackground(null);
        }
        return CustomizationResponse.from(customizationRepository.save(customization));
    }

    /**
     * Crea un nuevo ítem en el catálogo administrativo.
     */
    @Transactional
    public MascotItem createItem(MascotItemRequest request) {
        User admin = currentUserService.requireAuthenticatedUser();
        if (itemRepository.existsByCode(request.code())) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Ya existe un ítem con el código indicado.");
        }
        MascotItem item = new MascotItem();
        applyRequest(item, request);
        item = itemRepository.save(item);
        activityLogger.log(admin, ACTION_CREATED, TARGET_ITEM, item.getId(),
                "Ítem %s creado en la ranura %s.".formatted(item.getCode(), item.getSlot()));
        return item;
    }

    /**
     * Actualiza un ítem existente del catálogo administrativo.
     */
    @Transactional
    public MascotItem updateItem(Long id, MascotItemRequest request) {
        User admin = currentUserService.requireAuthenticatedUser();
        MascotItem item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "El ítem indicado no existe."));
        if (!item.getCode().equals(request.code()) && itemRepository.existsByCode(request.code())) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Ya existe otro ítem con el código indicado.");
        }
        applyRequest(item, request);
        item = itemRepository.save(item);
        activityLogger.log(admin, ACTION_UPDATED, TARGET_ITEM, item.getId(),
                "Ítem %s actualizado.".formatted(item.getCode()));
        return item;
    }

    /**
     * Elimina un ítem del catálogo. Los desbloqueos y equipos asociados
     * se limpian por integridad referencial en cascada.
     */
    @Transactional
    public void deleteItem(Long id) {
        User admin = currentUserService.requireAuthenticatedUser();
        MascotItem item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "El ítem indicado no existe."));
        itemRepository.delete(item);
        activityLogger.log(admin, ACTION_DELETED, TARGET_ITEM, id,
                "Ítem %s eliminado.".formatted(item.getCode()));
    }

    /**
     * Evalúa los ítems activos y crea los desbloqueos pendientes según
     * el estado del usuario. Se ejecuta de forma perezosa al consultar
     * el catálogo para reflejar de inmediato los logros recientes.
     */
    private void evaluateUnlocks(User user) {
        List<MascotItem> items = itemRepository.findByActiveTrueOrderBySlotAscNameAsc();
        Set<Long> alreadyUnlocked = new HashSet<>(unlockRepository.findUnlockedItemIds(user.getId()));
        UserXp progress = userXpRepository.findById(user.getId()).orElse(null);
        int level = progress == null ? 1 : progress.getCurrentLevel();
        int xp = progress == null ? 0 : progress.getTotalXp();
        Set<Long> ownedBadges = userBadgeRepository.findBadgeIdsByUserId(user.getId());

        List<MascotUnlock> newUnlocks = new ArrayList<>();
        for (MascotItem item : items) {
            if (alreadyUnlocked.contains(item.getId())) {
                continue;
            }
            if (meetsRequirement(item, level, xp, ownedBadges)) {
                MascotUnlock unlock = new MascotUnlock();
                unlock.setUser(user);
                unlock.setItem(item);
                newUnlocks.add(unlock);
            }
        }
        if (!newUnlocks.isEmpty()) {
            unlockRepository.saveAll(newUnlocks);
        }
    }

    private static boolean meetsRequirement(MascotItem item, int level, int xp, Set<Long> ownedBadges) {
        return switch (item.getUnlockType()) {
            case DEFAULT -> true;
            case LEVEL -> item.getUnlockLevel() != null && level >= item.getUnlockLevel();
            case XP -> item.getUnlockXp() != null && xp >= item.getUnlockXp();
            case BADGE -> item.getUnlockBadge() != null
                    && ownedBadges.contains(item.getUnlockBadge().getId());
        };
    }

    private static Set<Long> equippedItemIds(MascotCustomization customization) {
        Set<Long> ids = new HashSet<>();
        if (customization == null) {
            return ids;
        }
        addIfNotNull(ids, customization.getSkin());
        addIfNotNull(ids, customization.getHat());
        addIfNotNull(ids, customization.getAccessory());
        addIfNotNull(ids, customization.getBackground());
        return ids;
    }

    private static void addIfNotNull(Set<Long> ids, MascotItem item) {
        if (item != null) {
            ids.add(item.getId());
        }
    }

    private void applyRequest(MascotItem item, MascotItemRequest request) {
        item.setCode(request.code().trim());
        item.setName(request.name().trim());
        item.setDescription(request.description());
        item.setSlot(request.slot());
        item.setImageUrl(request.imageUrl());
        item.setUnlockType(request.unlockType());
        item.setUnlockLevel(request.unlockType() == MascotUnlockType.LEVEL ? request.unlockLevel() : null);
        item.setUnlockXp(request.unlockType() == MascotUnlockType.XP ? request.unlockXp() : null);
        item.setUnlockBadge(request.unlockType() == MascotUnlockType.BADGE
                ? resolveBadge(request.unlockBadgeId())
                : null);
        item.setActive(request.active());
        validateRequirement(item);
    }

    private Badge resolveBadge(Long badgeId) {
        if (badgeId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Debe indicar la insignia asociada al ítem.");
        }
        return badgeRepository.findById(badgeId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "La insignia indicada no existe."));
    }

    private static void validateRequirement(MascotItem item) {
        switch (item.getUnlockType()) {
            case DEFAULT -> {
                // Sin requisitos adicionales.
            }
            case LEVEL -> {
                if (item.getUnlockLevel() == null || item.getUnlockLevel() < 1) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST,
                            "El nivel de desbloqueo debe ser mayor o igual a 1.");
                }
            }
            case XP -> {
                if (item.getUnlockXp() == null || item.getUnlockXp() < 0) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST,
                            "El XP de desbloqueo debe ser mayor o igual a 0.");
                }
            }
            case BADGE -> {
                if (item.getUnlockBadge() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST,
                            "Debe indicar la insignia asociada al ítem.");
                }
            }
        }
    }
}

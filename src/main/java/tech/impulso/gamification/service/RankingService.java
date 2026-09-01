package tech.impulso.gamification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.gamification.dto.RankingEntryResponse;
import tech.impulso.gamification.dto.RankingResponse;
import tech.impulso.gamification.entity.RankingPeriod;
import tech.impulso.gamification.repository.RankingRepository;
import tech.impulso.gamification.repository.RankingRepository.RankingPosition;
import tech.impulso.gamification.repository.RankingRepository.RankingRow;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Servicio responsable de construir los rankings de estudiantes
 * (RF-021 / RF-049).
 *
 * <p>Los rankings se calculan sobre la marcha a partir del agregado
 * {@code user_xp} para el histórico y de {@code xp_events} para los
 * periodos. Los usuarios que hayan desactivado su visibilidad pública
 * quedan fuera de los resultados aunque sigan acumulando XP.</p>
 */
@Service
public class RankingService {

    /** Tamaño mínimo que puede solicitar el cliente. */
    private static final int MIN_LIMIT = 1;

    /** Tamaño máximo que se permite devolver en una sola consulta. */
    private static final int MAX_LIMIT = 100;

    private final RankingRepository rankingRepository;
    private final CurrentUserService currentUserService;

    public RankingService(RankingRepository rankingRepository,
                          CurrentUserService currentUserService) {
        this.rankingRepository = rankingRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Construye el ranking correspondiente al periodo indicado.
     *
     * @param period periodo utilizado para agrupar la XP.
     * @param limit  cantidad máxima de entradas a devolver.
     * @return ranking con las primeras posiciones y la ubicación del
     *         usuario autenticado, cuando aplique.
     */
    @Transactional(readOnly = true)
    public RankingResponse getRanking(RankingPeriod period, int limit) {
        int clampedLimit = Math.max(MIN_LIMIT, Math.min(MAX_LIMIT, limit));
        List<RankingRow> rows = fetchTop(period, clampedLimit);

        List<RankingEntryResponse> entries = toEntries(rows);
        RankingEntryResponse mine = resolveMyEntry(period, entries);

        return new RankingResponse(period, entries, mine);
    }

    private List<RankingRow> fetchTop(RankingPeriod period, int limit) {
        return switch (period) {
            case ALL_TIME -> rankingRepository.topAllTime(limit);
            case WEEKLY   -> rankingRepository.topSince(sevenDaysAgo(), limit);
            case MONTHLY  -> rankingRepository.topSince(thirtyDaysAgo(), limit);
        };
    }

    private RankingEntryResponse resolveMyEntry(RankingPeriod period, List<RankingEntryResponse> entries) {
        User user = currentUserService.requireAuthenticatedUser();
        if (!user.isShowInRanking()) {
            return null;
        }

        return entries.stream()
                .filter(entry -> entry.userId().equals(user.getId()))
                .findFirst()
                .orElseGet(() -> lookupMyPosition(period, user));
    }

    private RankingEntryResponse lookupMyPosition(RankingPeriod period, User user) {
        RankingPosition position = switch (period) {
            case ALL_TIME -> rankingRepository.positionAllTime(user.getId());
            case WEEKLY   -> rankingRepository.positionSince(user.getId(), sevenDaysAgo());
            case MONTHLY  -> rankingRepository.positionSince(user.getId(), thirtyDaysAgo());
        };
        if (position == null) {
            return null;
        }
        return new RankingEntryResponse(
                position.position(),
                user.getId(),
                user.getUsername(),
                "%s %s".formatted(user.getFirstName(), user.getLastName()),
                user.getProfilePhotoUrl(),
                position.xp(),
                position.currentLevel()
        );
    }

    private List<RankingEntryResponse> toEntries(List<RankingRow> rows) {
        return java.util.stream.IntStream.range(0, rows.size())
                .mapToObj(i -> {
                    RankingRow row = rows.get(i);
                    return new RankingEntryResponse(
                            i + 1L,
                            row.userId(),
                            row.username(),
                            "%s %s".formatted(row.firstName(), row.lastName()),
                            row.profilePhotoUrl(),
                            row.xp(),
                            row.currentLevel()
                    );
                })
                .toList();
    }

    private OffsetDateTime sevenDaysAgo() {
        return OffsetDateTime.now(ZoneOffset.UTC).minusDays(7);
    }

    private OffsetDateTime thirtyDaysAgo() {
        return OffsetDateTime.now(ZoneOffset.UTC).minusDays(30);
    }
}

package tech.impulso.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.admin.entity.AdminActivityLog;
import tech.impulso.admin.repository.AdminActivityLogRepository;
import tech.impulso.users.entity.User;

/**
 * Servicio de escritura del registro de actividad administrativa
 * (RF-056).
 *
 * <p>Utiliza propagación {@link Propagation#REQUIRES_NEW} para que la
 * escritura del registro no se cancele si la operación de negocio
 * principal falla y hace rollback. Esto garantiza que cualquier intento
 * de acción administrativa quede documentado incluso cuando la operación
 * no se complete satisfactoriamente.</p>
 */
@Service
public class AdminActivityLogger {

    /** Tipo utilizado para acciones que afectan cuentas de usuario. */
    public static final String TARGET_USER = "USER";

    /** Cambio de rol de un usuario. */
    public static final String ACTION_USER_ROLE_CHANGED = "USER_ROLE_CHANGED";

    /** Cambio del estado (activación / desactivación) de un usuario. */
    public static final String ACTION_USER_STATUS_CHANGED = "USER_STATUS_CHANGED";

    /** Creación automática del primer administrador durante el arranque. */
    public static final String ACTION_ADMIN_BOOTSTRAPPED = "ADMIN_BOOTSTRAPPED";

    private final AdminActivityLogRepository repository;

    public AdminActivityLogger(AdminActivityLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra una acción administrativa.
     *
     * @param admin      administrador responsable; puede ser nulo para
     *                   acciones ejecutadas por el sistema.
     * @param action     código de la acción realizada.
     * @param targetType tipo del recurso afectado (por ejemplo, USER).
     * @param targetId   identificador del recurso afectado; puede ser nulo.
     * @param details    detalles adicionales opcionales.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User admin, String action, String targetType, Long targetId, String details) {
        AdminActivityLog entry = new AdminActivityLog();
        entry.setAdmin(admin);
        entry.setAction(action);
        entry.setTargetType(targetType);
        entry.setTargetId(targetId);
        entry.setDetails(details);
        repository.save(entry);
    }
}

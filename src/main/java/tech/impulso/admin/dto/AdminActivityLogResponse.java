package tech.impulso.admin.dto;

import tech.impulso.admin.entity.AdminActivityLog;

import java.time.OffsetDateTime;

/**
 * Representación pública de una entrada del registro de actividad
 * administrativa.
 *
 * @param id             identificador interno de la entrada.
 * @param adminId        identificador del administrador responsable,
 *                       si aplica.
 * @param adminEmail     correo del administrador responsable, si aplica.
 * @param action         código de la acción registrada.
 * @param targetType     tipo del recurso afectado.
 * @param targetId       identificador del recurso afectado.
 * @param details        detalles adicionales de la acción.
 * @param createdAt      fecha y hora en que se registró.
 */
public record AdminActivityLogResponse(
        Long id,
        Long adminId,
        String adminEmail,
        String action,
        String targetType,
        Long targetId,
        String details,
        OffsetDateTime createdAt
) {

    /**
     * Construye la representación pública a partir de la entidad
     * persistente.
     *
     * @param entry entrada del registro.
     * @return DTO listo para devolver desde la API administrativa.
     */
    public static AdminActivityLogResponse from(AdminActivityLog entry) {
        return new AdminActivityLogResponse(
                entry.getId(),
                entry.getAdmin() == null ? null : entry.getAdmin().getId(),
                entry.getAdmin() == null ? null : entry.getAdmin().getEmail(),
                entry.getAction(),
                entry.getTargetType(),
                entry.getTargetId(),
                entry.getDetails(),
                entry.getCreatedAt()
        );
    }
}

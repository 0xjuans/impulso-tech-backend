package tech.impulso.dashboard.dto;

import java.time.OffsetDateTime;

/**
 * Fila del feed "Últimos registros" del panel del administrador
 * (RF-033). Muestra las cuentas más recientes con lo necesario para
 * identificar quién entró y verificar si aún tiene la cuenta pendiente
 * de confirmación.
 */
public record RecentSignupRow(
        Long userId,
        String username,
        String fullName,
        String role,
        String status,
        OffsetDateTime createdAt
) {
}

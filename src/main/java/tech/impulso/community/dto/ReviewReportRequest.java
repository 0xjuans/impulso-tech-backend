package tech.impulso.community.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.impulso.community.entity.ReportStatus;

/**
 * Solicitud del administrador para actualizar el estado de un reporte
 * tras revisarlo.
 *
 * @param status     nuevo estado del reporte ({@code REVISADO} o
 *                   {@code DESESTIMADO}).
 * @param adminNotes observaciones internas del administrador.
 */
public record ReviewReportRequest(
        @NotNull(message = "El estado del reporte es obligatorio.")
        ReportStatus status,

        @Size(max = 2000, message = "Las observaciones no pueden superar los 2000 caracteres.")
        String adminNotes
) {
}

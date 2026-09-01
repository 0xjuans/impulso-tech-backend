package tech.impulso.community.dto;

import tech.impulso.community.entity.CommunityReport;
import tech.impulso.community.entity.ReportStatus;
import tech.impulso.community.entity.ReportTargetType;

import java.time.OffsetDateTime;

/**
 * Representación pública de un reporte de contenido de la comunidad.
 *
 * @param id            identificador del reporte.
 * @param reporterId    identificador del usuario que reportó.
 * @param reporterName  nombre completo del usuario que reportó.
 * @param targetType    tipo del contenido reportado.
 * @param targetId      identificador del contenido reportado.
 * @param reason        motivo del reporte.
 * @param status        estado actual.
 * @param reviewedById  identificador del administrador que revisó, si aplica.
 * @param reviewedAt    momento en que se revisó, si aplica.
 * @param adminNotes    observaciones del administrador.
 * @param createdAt     fecha de creación del reporte.
 */
public record CommunityReportResponse(
        Long id,
        Long reporterId,
        String reporterName,
        ReportTargetType targetType,
        Long targetId,
        String reason,
        ReportStatus status,
        Long reviewedById,
        OffsetDateTime reviewedAt,
        String adminNotes,
        OffsetDateTime createdAt
) {

    /**
     * Construye la representación pública a partir de la entidad
     * persistente.
     *
     * @param report reporte a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static CommunityReportResponse from(CommunityReport report) {
        return new CommunityReportResponse(
                report.getId(),
                report.getReporter().getId(),
                "%s %s".formatted(report.getReporter().getFirstName(), report.getReporter().getLastName()),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getStatus(),
                report.getReviewedBy() == null ? null : report.getReviewedBy().getId(),
                report.getReviewedAt(),
                report.getAdminNotes(),
                report.getCreatedAt()
        );
    }
}

package tech.impulso.community.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.admin.service.AdminActivityLogger;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.community.dto.CommunityReportResponse;
import tech.impulso.community.dto.CreateReportRequest;
import tech.impulso.community.dto.ReviewReportRequest;
import tech.impulso.community.entity.CommunityReport;
import tech.impulso.community.entity.ReportStatus;
import tech.impulso.community.entity.ReportTargetType;
import tech.impulso.community.repository.CommunityPostRepository;
import tech.impulso.community.repository.CommunityReplyRepository;
import tech.impulso.community.repository.CommunityReportRepository;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Servicio con las operaciones de moderación de la comunidad (RF-034).
 *
 * <p>Los usuarios pueden emitir reportes sobre publicaciones o
 * respuestas; los administradores los revisan desde el panel dedicado y
 * los marcan como revisados o desestimados. Cada revisión queda
 * registrada en el {@link AdminActivityLogger} para trazabilidad.</p>
 */
@Service
public class CommunityReportService {

    /** Tipo utilizado en el registro de actividad administrativa. */
    private static final String TARGET_REPORT = "COMMUNITY_REPORT";

    /** Código de acción registrado cuando el administrador revisa un reporte. */
    private static final String ACTION_REPORT_REVIEWED = "COMMUNITY_REPORT_REVIEWED";

    private final CommunityReportRepository reportRepository;
    private final CommunityPostRepository postRepository;
    private final CommunityReplyRepository replyRepository;
    private final CurrentUserService currentUserService;
    private final AdminActivityLogger activityLogger;

    public CommunityReportService(CommunityReportRepository reportRepository,
                                  CommunityPostRepository postRepository,
                                  CommunityReplyRepository replyRepository,
                                  CurrentUserService currentUserService,
                                  AdminActivityLogger activityLogger) {
        this.reportRepository = reportRepository;
        this.postRepository = postRepository;
        this.replyRepository = replyRepository;
        this.currentUserService = currentUserService;
        this.activityLogger = activityLogger;
    }

    /**
     * Registra un reporte de un contenido de la comunidad emitido por
     * el usuario autenticado.
     *
     * @param targetType tipo del contenido reportado.
     * @param targetId   identificador del contenido reportado.
     * @param request    motivo del reporte.
     * @return reporte creado.
     */
    @Transactional
    public CommunityReportResponse createReport(ReportTargetType targetType,
                                                Long targetId,
                                                CreateReportRequest request) {
        User reporter = currentUserService.requireAuthenticatedUser();
        ensureTargetExists(targetType, targetId);

        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporter.getId(), targetType, targetId)) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Ya has reportado este contenido previamente.");
        }

        CommunityReport report = new CommunityReport();
        report.setReporter(reporter);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(request.reason().trim());
        report.setStatus(ReportStatus.PENDIENTE);
        return CommunityReportResponse.from(reportRepository.save(report));
    }

    /**
     * Devuelve una página con los reportes registrados. Reservado a
     * administradores desde el panel correspondiente.
     */
    @Transactional(readOnly = true)
    public PagedResponse<CommunityReportResponse> listReports(ReportStatus status,
                                                              ReportTargetType targetType,
                                                              Pageable pageable) {
        Page<CommunityReport> page = reportRepository.search(status, targetType, pageable);
        return PagedResponse.from(page, CommunityReportResponse::from);
    }

    /**
     * Marca un reporte como revisado o desestimado. Registra al
     * administrador responsable y las observaciones internas.
     *
     * @param reportId identificador del reporte.
     * @param request  nuevo estado y observaciones.
     * @return reporte actualizado.
     */
    @Transactional
    public CommunityReportResponse reviewReport(Long reportId, ReviewReportRequest request) {
        if (request.status() == ReportStatus.PENDIENTE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Un reporte no puede regresar al estado pendiente desde el panel administrativo.");
        }

        User admin = currentUserService.requireAuthenticatedUser();
        CommunityReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "El reporte indicado no existe."));

        ReportStatus previousStatus = report.getStatus();
        report.setStatus(request.status());
        report.setReviewedBy(admin);
        report.setReviewedAt(OffsetDateTime.now(ZoneOffset.UTC));
        report.setAdminNotes(request.adminNotes());

        activityLogger.log(
                admin,
                ACTION_REPORT_REVIEWED,
                TARGET_REPORT,
                report.getId(),
                "%s -> %s".formatted(previousStatus, request.status()));

        return CommunityReportResponse.from(report);
    }

    /**
     * Verifica que el contenido a reportar exista antes de admitir el
     * reporte, evitando registros huérfanos.
     *
     * @param targetType tipo del contenido.
     * @param targetId   identificador del contenido.
     */
    private void ensureTargetExists(ReportTargetType targetType, Long targetId) {
        boolean exists = switch (targetType) {
            case POST  -> postRepository.existsById(targetId);
            case REPLY -> replyRepository.existsById(targetId);
        };
        if (!exists) {
            throw new BusinessException(HttpStatus.NOT_FOUND,
                    "El contenido reportado no existe.");
        }
    }
}

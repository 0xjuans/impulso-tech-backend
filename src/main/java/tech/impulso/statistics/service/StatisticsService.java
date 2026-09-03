package tech.impulso.statistics.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.courses.repository.CourseRepository;
import tech.impulso.enrollments.entity.EnrollmentStatus;
import tech.impulso.enrollments.repository.EnrollmentRepository;
import tech.impulso.statistics.dto.CourseEnrollmentStat;
import tech.impulso.statistics.dto.InstructorStatisticsResponse;
import tech.impulso.statistics.dto.PlatformStatisticsResponse;
import tech.impulso.statistics.dto.TimeSeriesPoint;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;
import tech.impulso.users.repository.UserRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio que expone las estadísticas agregadas de la plataforma
 * (RF-058) para administradores e instructores.
 *
 * <p>Las estadísticas se calculan mediante consultas nativas que
 * aprovechan las capacidades de agregación de PostgreSQL. Los valores
 * porcentuales se acotan al rango [0, 100] y se redondean a dos
 * decimales para facilitar su representación en el frontend.</p>
 */
@Service
public class StatisticsService {

    /** Cantidad de días considerada por defecto en las series temporales. */
    private static final int DEFAULT_WINDOW_DAYS = 30;

    /** Cantidad mínima de días admitida en las series temporales. */
    private static final int MIN_WINDOW_DAYS = 1;

    /** Cantidad máxima de días admitida en las series temporales. */
    private static final int MAX_WINDOW_DAYS = 365;

    /** Cantidad máxima de cursos incluidos en los rankings top-N. */
    private static final int TOP_COURSES_LIMIT = 10;

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CurrentUserService currentUserService;

    public StatisticsService(UserRepository userRepository,
                             CourseRepository courseRepository,
                             EnrollmentRepository enrollmentRepository,
                             CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Devuelve las estadísticas globales de la plataforma.
     *
     * @param days ventana temporal solicitada; se acota al rango
     *             permitido y se aplica un valor por defecto cuando es
     *             nulo o no válido.
     */
    @Transactional(readOnly = true)
    public PlatformStatisticsResponse getPlatformStatistics(Integer days) {
        int window = resolveWindow(days);

        Map<String, Long> usersByRole = new LinkedHashMap<>();
        for (Role role : Role.values()) {
            usersByRole.put(role.name(), userRepository.countByRole(role));
        }

        Map<String, Long> coursesByDifficulty = new LinkedHashMap<>();
        for (DifficultyLevel level : DifficultyLevel.values()) {
            coursesByDifficulty.put(level.name(), courseRepository.countByDifficulty(level));
        }

        long totalEnrollments = enrollmentRepository.count();
        long completedEnrollments = enrollmentRepository.countByStatus(EnrollmentStatus.COMPLETADO);
        double completionRate = percentage(completedEnrollments, totalEnrollments);

        List<TimeSeriesPoint> registrations = toTimeSeries(userRepository.countRegistrationsByDay(window));
        List<TimeSeriesPoint> enrollments = toTimeSeries(enrollmentRepository.countEnrollmentsByDay(window));
        List<CourseEnrollmentStat> topCourses = enrollmentRepository
                .findTopCoursesByEnrollments(TOP_COURSES_LIMIT).stream()
                .map(StatisticsService::toTopCourse)
                .toList();

        return new PlatformStatisticsResponse(
                usersByRole,
                coursesByDifficulty,
                completionRate,
                registrations,
                enrollments,
                topCourses,
                window
        );
    }

    /**
     * Devuelve las estadísticas del instructor autenticado.
     *
     * @param days ventana temporal solicitada.
     */
    @Transactional(readOnly = true)
    public InstructorStatisticsResponse getInstructorStatistics(Integer days) {
        User instructor = currentUserService.requireAuthenticatedUser();
        Long id = instructor.getId();
        int window = resolveWindow(days);

        List<CourseEnrollmentStat> courses = enrollmentRepository
                .findInstructorCourseEnrollmentStats(id, TOP_COURSES_LIMIT).stream()
                .map(StatisticsService::toCourseStat)
                .toList();

        long total = courses.stream().mapToLong(CourseEnrollmentStat::enrollments).sum();
        long completed = courses.stream()
                .map(CourseEnrollmentStat::completed)
                .filter(v -> v != null)
                .mapToLong(Long::longValue)
                .sum();

        List<TimeSeriesPoint> series = toTimeSeries(
                enrollmentRepository.countEnrollmentsByDayForInstructor(id, window));

        return new InstructorStatisticsResponse(
                percentage(completed, total),
                series,
                courses,
                window
        );
    }

    /** Convierte una fila {@code [id, name, total]} en una estadística de curso. */
    private static CourseEnrollmentStat toTopCourse(Object[] row) {
        Long id = ((Number) row[0]).longValue();
        String name = (String) row[1];
        long total = ((Number) row[2]).longValue();
        return new CourseEnrollmentStat(id, name, total, null, null);
    }

    /**
     * Convierte una fila {@code [id, name, total, completed]} en una
     * estadística con detalle de finalización.
     */
    private static CourseEnrollmentStat toCourseStat(Object[] row) {
        Long id = ((Number) row[0]).longValue();
        String name = (String) row[1];
        long total = ((Number) row[2]).longValue();
        long completed = ((Number) row[3]).longValue();
        return new CourseEnrollmentStat(id, name, total, completed, percentage(completed, total));
    }

    /** Convierte filas {@code [Date, Number]} en puntos de serie temporal. */
    private static List<TimeSeriesPoint> toTimeSeries(List<Object[]> rows) {
        return rows.stream()
                .map(row -> new TimeSeriesPoint(
                        ((Date) row[0]).toLocalDate(),
                        ((Number) row[1]).longValue()))
                .toList();
    }

    /** Calcula el porcentaje redondeado a dos decimales, con {@code total = 0} → 0. */
    private static double percentage(long numerator, long total) {
        if (total <= 0) {
            return 0d;
        }
        double value = (numerator * 100d) / total;
        return Math.round(value * 100d) / 100d;
    }

    /** Acota la ventana temporal al rango permitido y aplica el valor por defecto. */
    private static int resolveWindow(Integer requested) {
        if (requested == null) {
            return DEFAULT_WINDOW_DAYS;
        }
        if (requested < MIN_WINDOW_DAYS) {
            return MIN_WINDOW_DAYS;
        }
        if (requested > MAX_WINDOW_DAYS) {
            return MAX_WINDOW_DAYS;
        }
        return requested;
    }
}

package tech.impulso.recommendations.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.challenges.entity.Challenge;
import tech.impulso.challenges.repository.ChallengeAttemptRepository;
import tech.impulso.challenges.repository.ChallengeRepository;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.courses.entity.Course;
import tech.impulso.courses.repository.CourseRepository;
import tech.impulso.enrollments.repository.EnrollmentRepository;
import tech.impulso.learningroutes.entity.LearningRoute;
import tech.impulso.learningroutes.repository.LearningRouteRepository;
import tech.impulso.recommendations.dto.Recommendation;
import tech.impulso.recommendations.dto.RecommendationsResponse;
import tech.impulso.resources.entity.EducationalResource;
import tech.impulso.resources.repository.EducationalResourceRepository;
import tech.impulso.users.entity.User;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Motor de recomendaciones personalizadas para el estudiante (RF-053).
 *
 * <p>Construye un perfil de intereses a partir del historial del
 * estudiante (cursos completados, cursos en progreso y retos
 * aprobados) y puntúa candidatos publicados de cada tipo utilizando
 * heurísticas simples: afinidad de tecnología y coincidencia de nivel
 * de dificultad. La estructura permite refinar los resultados con la
 * IA en el futuro sin cambiar el contrato del controlador.</p>
 */
@Service
public class RecommendationService {

    /** Cantidad de candidatos a considerar por tipo antes de puntuar. */
    private static final int CANDIDATE_POOL_SIZE = 30;

    /** Cantidad de recomendaciones devueltas por tipo. */
    private static final int MAX_RECOMMENDATIONS_PER_TYPE = 5;

    private static final int SCORE_TECHNOLOGY_MATCH = 3;
    private static final int SCORE_DIFFICULTY_MATCH = 2;
    private static final int SCORE_ADJACENT_DIFFICULTY = 1;

    private final CourseRepository courseRepository;
    private final LearningRouteRepository routeRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final EducationalResourceRepository resourceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CurrentUserService currentUserService;

    public RecommendationService(CourseRepository courseRepository,
                                 LearningRouteRepository routeRepository,
                                 ChallengeRepository challengeRepository,
                                 ChallengeAttemptRepository challengeAttemptRepository,
                                 EducationalResourceRepository resourceRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 CurrentUserService currentUserService) {
        this.courseRepository = courseRepository;
        this.routeRepository = routeRepository;
        this.challengeRepository = challengeRepository;
        this.challengeAttemptRepository = challengeAttemptRepository;
        this.resourceRepository = resourceRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Devuelve las recomendaciones agregadas para el usuario
     * autenticado.
     */
    @Transactional(readOnly = true)
    public RecommendationsResponse recommend() {
        User student = currentUserService.requireAuthenticatedUser();
        LearnerProfile profile = buildProfile(student);

        List<Recommendation> courses = recommendCourses(student, profile);
        List<Recommendation> routes = recommendRoutes(profile);
        List<Recommendation> challenges = recommendChallenges(student, profile);
        List<Recommendation> resources = recommendResources(profile);

        return new RecommendationsResponse(
                profile.summarize(),
                profile.dominantLevel.name(),
                List.copyOf(profile.technologies),
                courses,
                routes,
                challenges,
                resources
        );
    }

    /**
     * Construye el perfil del estudiante a partir de su historial.
     */
    private LearnerProfile buildProfile(User student) {
        List<Course> completed = enrollmentRepository.findCompletedCoursesByUser(student.getId());
        List<Course> inProgress = enrollmentRepository.findInProgressCoursesByUser(student.getId());

        Set<String> technologies = new HashSet<>();
        Map<DifficultyLevel, Integer> difficultyCount = new HashMap<>();
        for (Course course : completed) {
            addTechnology(technologies, course.getTechnology());
            countDifficulty(difficultyCount, course.getDifficulty());
        }
        for (Course course : inProgress) {
            addTechnology(technologies, course.getTechnology());
            countDifficulty(difficultyCount, course.getDifficulty());
        }
        return new LearnerProfile(
                technologies,
                dominantDifficulty(difficultyCount, DifficultyLevel.PRINCIPIANTE),
                completed.size(),
                inProgress.size()
        );
    }

    private List<Recommendation> recommendCourses(User student, LearnerProfile profile) {
        Pageable pool = PageRequest.of(0, CANDIDATE_POOL_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        return courseRepository.findPublishedNotEnrolledByUser(student.getId(), pool).stream()
                .map(course -> new ScoredCandidate<>(course,
                        scoreByTechnologyAndLevel(profile, course.getTechnology(), course.getDifficulty())))
                .sorted(byScoreDesc())
                .limit(MAX_RECOMMENDATIONS_PER_TYPE)
                .map(sc -> new Recommendation(
                        sc.value.getId(),
                        "COURSE",
                        sc.value.getName(),
                        sc.value.getDescription(),
                        explain(profile, sc.value.getTechnology(), sc.value.getDifficulty(), "curso"),
                        sc.score))
                .toList();
    }

    private List<Recommendation> recommendRoutes(LearnerProfile profile) {
        Pageable pool = PageRequest.of(0, CANDIDATE_POOL_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        return routeRepository.search(null, null, ContentStatus.PUBLICADO, pool).stream()
                .map(route -> new ScoredCandidate<>(route, scoreRoute(profile, route)))
                .sorted(byScoreDesc())
                .limit(MAX_RECOMMENDATIONS_PER_TYPE)
                .map(sc -> new Recommendation(
                        sc.value.getId(),
                        "ROUTE",
                        sc.value.getName(),
                        sc.value.getDescription(),
                        explain(profile, sc.value.getTechnologies(), sc.value.getDifficulty(), "ruta"),
                        sc.score))
                .toList();
    }

    private List<Recommendation> recommendChallenges(User student, LearnerProfile profile) {
        Set<Long> solved = new HashSet<>(challengeAttemptRepository.findApprovedChallengeIds(student.getId()));
        Pageable pool = PageRequest.of(0, CANDIDATE_POOL_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        return challengeRepository.search(null, null, ContentStatus.PUBLICADO,
                        null, null, null, null, null, pool).stream()
                .filter(challenge -> !solved.contains(challenge.getId()))
                .map(challenge -> new ScoredCandidate<>(challenge,
                        scoreByTechnologyAndLevel(profile, challenge.getAllowedLanguages(), challenge.getDifficulty())))
                .sorted(byScoreDesc())
                .limit(MAX_RECOMMENDATIONS_PER_TYPE)
                .map(sc -> new Recommendation(
                        sc.value.getId(),
                        "CHALLENGE",
                        sc.value.getName(),
                        sc.value.getDescription(),
                        explain(profile, sc.value.getAllowedLanguages(), sc.value.getDifficulty(), "reto"),
                        sc.score))
                .toList();
    }

    private List<Recommendation> recommendResources(LearnerProfile profile) {
        Pageable pool = PageRequest.of(0, CANDIDATE_POOL_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        return resourceRepository.search(null, null, null, ContentStatus.PUBLICADO,
                        null, null, null, null, pool).stream()
                .map(resource -> new ScoredCandidate<>(resource,
                        scoreByTechnologyAndLevel(profile, resource.getTechnology(), resource.getDifficulty())))
                .sorted(byScoreDesc())
                .limit(MAX_RECOMMENDATIONS_PER_TYPE)
                .map(sc -> new Recommendation(
                        sc.value.getId(),
                        "RESOURCE",
                        sc.value.getName(),
                        sc.value.getDescription(),
                        explain(profile, sc.value.getTechnology(), sc.value.getDifficulty(), "recurso"),
                        sc.score))
                .toList();
    }

    /**
     * Puntúa un candidato por coincidencia de tecnología y nivel de
     * dificultad respecto al perfil del estudiante.
     */
    private static int scoreByTechnologyAndLevel(LearnerProfile profile,
                                                 String technologyOrLanguages,
                                                 DifficultyLevel difficulty) {
        int score = 0;
        if (matchesAnyTechnology(profile.technologies, technologyOrLanguages)) {
            score += SCORE_TECHNOLOGY_MATCH;
        }
        if (difficulty != null) {
            if (difficulty == profile.dominantLevel) {
                score += SCORE_DIFFICULTY_MATCH;
            } else if (Math.abs(difficulty.ordinal() - profile.dominantLevel.ordinal()) == 1) {
                score += SCORE_ADJACENT_DIFFICULTY;
            }
        }
        return score;
    }

    private static int scoreRoute(LearnerProfile profile, LearningRoute route) {
        int score = 0;
        if (matchesAnyTechnology(profile.technologies, route.getTechnologies())) {
            score += SCORE_TECHNOLOGY_MATCH;
        }
        if (route.getDifficulty() != null) {
            if (route.getDifficulty() == profile.dominantLevel) {
                score += SCORE_DIFFICULTY_MATCH;
            } else if (Math.abs(route.getDifficulty().ordinal() - profile.dominantLevel.ordinal()) == 1) {
                score += SCORE_ADJACENT_DIFFICULTY;
            }
        }
        return score;
    }

    private static boolean matchesAnyTechnology(Set<String> profileTechnologies, String candidateTechnologies) {
        if (profileTechnologies.isEmpty() || candidateTechnologies == null || candidateTechnologies.isBlank()) {
            return false;
        }
        String normalized = candidateTechnologies.toLowerCase(Locale.ROOT);
        return profileTechnologies.stream().anyMatch(tech -> normalized.contains(tech));
    }

    private static void addTechnology(Set<String> target, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        for (String piece : value.split(",")) {
            String normalized = piece.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty()) {
                target.add(normalized);
            }
        }
    }

    private static void countDifficulty(Map<DifficultyLevel, Integer> counter, DifficultyLevel level) {
        if (level != null) {
            counter.merge(level, 1, Integer::sum);
        }
    }

    private static DifficultyLevel dominantDifficulty(Map<DifficultyLevel, Integer> counter,
                                                      DifficultyLevel fallback) {
        return counter.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(fallback);
    }

    private static String explain(LearnerProfile profile,
                                  String candidateTechnology,
                                  DifficultyLevel candidateLevel,
                                  String label) {
        boolean techMatch = matchesAnyTechnology(profile.technologies, candidateTechnology);
        boolean levelMatch = candidateLevel != null && candidateLevel == profile.dominantLevel;
        if (techMatch && levelMatch) {
            return "Este %s coincide con tu tecnología y tu nivel actual.".formatted(label);
        }
        if (techMatch) {
            return "Este %s trabaja con una tecnología que ya has explorado.".formatted(label);
        }
        if (levelMatch) {
            return "Este %s se ajusta a tu nivel actual.".formatted(label);
        }
        return "Contenido publicado recientemente que podría interesarte.";
    }

    private static <T> Comparator<ScoredCandidate<T>> byScoreDesc() {
        return Comparator.<ScoredCandidate<T>>comparingInt(sc -> sc.score).reversed();
    }

    /** Perfil calculado del estudiante para puntuar candidatos. */
    private record LearnerProfile(Set<String> technologies,
                                  DifficultyLevel dominantLevel,
                                  int completedCourses,
                                  int inProgressCourses) {

        String summarize() {
            if (technologies.isEmpty() && completedCourses == 0 && inProgressCourses == 0) {
                return "Aún no tienes historial suficiente; te sugerimos contenidos recientes para comenzar.";
            }
            return "Perfil basado en %d cursos completados, %d en progreso y %d tecnologías detectadas."
                    .formatted(completedCourses, inProgressCourses, technologies.size());
        }
    }

    /** Candidato con su puntaje calculado. */
    private record ScoredCandidate<T>(T value, int score) {
    }
}

package tech.impulso.notifications.service;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.impulso.common.security.CurrentUserService;
import tech.impulso.notifications.entity.Notification;
import tech.impulso.notifications.entity.NotificationType;
import tech.impulso.notifications.repository.NotificationRepository;
import tech.impulso.preferences.entity.UserPreferences;
import tech.impulso.preferences.repository.UserPreferencesRepository;
import tech.impulso.users.entity.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del cortafuegos de preferencias de {@link NotificationService}.
 *
 * <p>Se enfoca exclusivamente en la lógica de filtrado por preferencia
 * del destinatario: cuando el usuario deshabilita una categoría, el
 * servicio no debe persistir la notificación ni notificar por SSE. Los
 * demás caminos ya están cubiertos por pruebas independientes del
 * broadcaster.</p>
 */
class NotificationServicePreferencesTest {

    private NotificationRepository repository;
    private CurrentUserService currentUserService;
    private NotificationSseBroadcaster broadcaster;
    private UserPreferencesRepository preferencesRepository;
    private NotificationService service;
    private User user;

    @BeforeEach
    void setUp() {
        repository = mock(NotificationRepository.class);
        currentUserService = mock(CurrentUserService.class);
        broadcaster = mock(NotificationSseBroadcaster.class);
        preferencesRepository = mock(UserPreferencesRepository.class);
        service = new NotificationService(repository, currentUserService, broadcaster, preferencesRepository);
        user = new User();
        user.setId(101L);
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void guardaLaNotificacionCuandoLaCategoriaEstaHabilitada() {
        UserPreferences prefs = new UserPreferences();
        prefs.setUser(user);
        prefs.setNotifyAchievements(true);
        when(preferencesRepository.findById(101L)).thenReturn(Optional.of(prefs));

        service.notify(user, NotificationType.BADGE_AWARDED, "t", "m", null, null);

        verify(repository, times(1)).save(any(Notification.class));
    }

    @Test
    void descartaLaNotificacionCuandoLaCategoriaEstaDeshabilitada() {
        UserPreferences prefs = new UserPreferences();
        prefs.setUser(user);
        prefs.setNotifyAchievements(false);
        when(preferencesRepository.findById(101L)).thenReturn(Optional.of(prefs));

        service.notify(user, NotificationType.BADGE_AWARDED, "t", "m", null, null);

        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void permiteLasGenericasAunSiOtrasCategoriasEstanDeshabilitadas() {
        UserPreferences prefs = new UserPreferences();
        prefs.setUser(user);
        prefs.setNotifyAchievements(false);
        prefs.setNotifyEvaluations(false);
        prefs.setNotifyProgress(false);
        when(preferencesRepository.findById(101L)).thenReturn(Optional.of(prefs));

        service.notify(user, NotificationType.GENERIC, "t", "m", null, null);

        verify(repository, times(1)).save(any(Notification.class));
    }

    @Test
    void guardaCuandoElUsuarioNoTienePreferenciasCreadas() {
        when(preferencesRepository.findById(101L)).thenReturn(Optional.empty());

        service.notify(user, NotificationType.EVALUATION_PASSED, "t", "m", null, null);

        verify(repository, times(1)).save(any(Notification.class));
    }

    @Test
    void mapeoStreakMilestoneUsaBanderaDeProgreso() {
        UserPreferences prefs = new UserPreferences();
        prefs.setUser(user);
        prefs.setNotifyProgress(false);
        when(preferencesRepository.findById(101L)).thenReturn(Optional.of(prefs));

        service.notify(user, NotificationType.STREAK_MILESTONE, "t", "m", null, null);

        verify(repository, never()).save(any(Notification.class));
    }
}

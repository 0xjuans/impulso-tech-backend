package tech.impulso.users.entity;

/**
 * Estados posibles del ciclo de vida de una cuenta de usuario.
 *
 * <p>El estado determina si el usuario puede autenticarse y utilizar las
 * funcionalidades de la plataforma. Se apoya en los requerimientos
 * RF-001 (verificación) y RF-030 (activación / desactivación de cuentas).</p>
 */
public enum UserStatus {

    /**
     * La cuenta ha sido creada mediante el formulario de registro, pero
     * aún no se ha confirmado la propiedad del correo electrónico. Los
     * usuarios en este estado no pueden iniciar sesión.
     */
    PENDIENTE_VERIFICACION,

    /**
     * La cuenta ha sido verificada y se encuentra habilitada para
     * autenticarse y utilizar las funcionalidades correspondientes a su
     * rol.
     */
    ACTIVA,

    /**
     * La cuenta ha sido desactivada por el administrador. Se conserva la
     * información histórica del usuario, pero se le impide iniciar sesión
     * hasta que la cuenta sea nuevamente activada.
     */
    DESACTIVADA
}

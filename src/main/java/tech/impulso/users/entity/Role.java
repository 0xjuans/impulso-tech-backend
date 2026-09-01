package tech.impulso.users.entity;

/**
 * Roles funcionales definidos por Impulso Tech.
 *
 * <p>El rol determina las funcionalidades a las que puede acceder cada
 * usuario dentro de la plataforma. Un usuario tiene siempre un único rol
 * principal activo. La asignación y modificación de roles corresponde de
 * manera exclusiva al {@link #ADMINISTRADOR}, tal como se establece en
 * los requerimientos RF-006 y RF-031.</p>
 */
public enum Role {

    /**
     * Persona que utiliza la plataforma para aprender programación,
     * realizar laboratorios, resolver retos, presentar evaluaciones y
     * consultar su progreso. Es el rol asignado por defecto a los
     * usuarios recién registrados.
     */
    ESTUDIANTE,

    /**
     * Persona encargada de crear, administrar y supervisar contenidos
     * educativos, laboratorios, retos y evaluaciones. Puede consultar el
     * progreso de los estudiantes vinculados a los contenidos que
     * administra.
     */
    INSTRUCTOR,

    /**
     * Usuario encargado de gestionar la plataforma, los usuarios, los
     * permisos, los contenidos y la configuración general del sistema.
     */
    ADMINISTRADOR
}

package tech.impulso.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.admin.dto.AdminUserResponse;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.admin.dto.UpdateUserRoleRequest;
import tech.impulso.admin.dto.UpdateUserStatusRequest;
import tech.impulso.admin.service.AdminUserService;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.UserStatus;

/**
 * Controlador REST del panel del administrador para la gestión de
 * usuarios (RF-030 / RF-031).
 *
 * <p>Todos los endpoints requieren el rol {@link Role#ADMINISTRADOR}.
 * La autorización se aplica de forma declarativa mediante
 * {@link PreAuthorize} a nivel de clase.</p>
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administración de usuarios",
        description = "Operaciones administrativas sobre las cuentas de la plataforma.")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Devuelve una página con los usuarios registrados en la plataforma.
     *
     * @param search   filtro por correo, nombre de usuario, nombre o apellido.
     * @param role     filtro por rol.
     * @param status   filtro por estado.
     * @param pageable configuración de paginación y ordenamiento.
     * @return página con los usuarios coincidentes.
     */
    @Operation(summary = "Listar usuarios",
            description = "Devuelve los usuarios registrados en la plataforma con soporte de búsqueda y filtros.")
    @GetMapping
    public ResponseEntity<PagedResponse<AdminUserResponse>> list(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "role", required = false) Role role,
            @RequestParam(value = "status", required = false) UserStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(adminUserService.search(search, role, status, pageable));
    }

    /**
     * Consulta el detalle administrativo de un usuario específico.
     *
     * @param id identificador del usuario.
     * @return información detallada del usuario.
     */
    @Operation(summary = "Consultar un usuario",
            description = "Devuelve la información administrativa detallada de un usuario específico.")
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(adminUserService.findById(id));
    }

    /**
     * Modifica el rol funcional de un usuario.
     *
     * @param id      identificador del usuario a modificar.
     * @param request nuevo rol a asignar.
     * @return información actualizada del usuario.
     */
    @Operation(summary = "Cambiar el rol de un usuario",
            description = "Asigna un nuevo rol al usuario. Un administrador no puede modificar su propio rol.")
    @PatchMapping("/{id}/role")
    public ResponseEntity<AdminUserResponse> updateRole(@PathVariable("id") Long id,
                                                        @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(adminUserService.changeRole(id, request.role()));
    }

    /**
     * Activa o desactiva una cuenta de usuario. La eliminación
     * definitiva no está permitida.
     *
     * @param id      identificador del usuario a modificar.
     * @param request nuevo estado que se aplicará.
     * @return información actualizada del usuario.
     */
    @Operation(summary = "Cambiar el estado de una cuenta",
            description = "Activa o desactiva la cuenta indicada. Un administrador no puede modificar su propio estado.")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminUserResponse> updateStatus(@PathVariable("id") Long id,
                                                          @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(adminUserService.changeStatus(id, request.status()));
    }
}

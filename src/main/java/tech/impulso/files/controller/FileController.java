package tech.impulso.files.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tech.impulso.files.dto.StoredFileResponse;
import tech.impulso.files.entity.FilePurpose;
import tech.impulso.files.service.FileService;

/**
 * Controlador REST para la gestión de archivos almacenados en
 * Cloudflare R2 (RF-059).
 *
 * <p>Todo el acceso al binario pasa por URLs firmadas temporales
 * generadas por el backend; las credenciales de almacenamiento jamás
 * llegan al cliente.</p>
 */
@RestController
@RequestMapping("/api/files")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Archivos",
        description = "Subida, descarga y borrado de archivos almacenados en Cloudflare R2.")
public class FileController {

    private final FileService service;

    public FileController(FileService service) {
        this.service = service;
    }

    @Operation(summary = "Subir un archivo",
            description = "Devuelve la metadata y una URL firmada temporal para acceder al archivo.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoredFileResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("purpose") FilePurpose purpose) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.upload(file, purpose));
    }

    @Operation(summary = "Obtener la URL firmada de un archivo")
    @GetMapping("/{id}")
    public ResponseEntity<StoredFileResponse> download(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.download(id));
    }

    @Operation(summary = "Eliminar un archivo")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar mis archivos",
            description = "Permite filtrar opcionalmente por propósito.")
    @GetMapping("/mine")
    public ResponseEntity<Page<StoredFileResponse>> listMine(
            @RequestParam(value = "purpose", required = false) FilePurpose purpose,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(service.listMine(purpose, pageable));
    }
}

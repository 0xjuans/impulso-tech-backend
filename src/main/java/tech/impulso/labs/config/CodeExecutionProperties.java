package tech.impulso.labs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuración del sandbox de ejecución de código (RF-030, RF-037).
 *
 * <p>Define el modo activo, los límites de recursos aplicados al
 * contenedor Docker y el catálogo de lenguajes soportados con la imagen
 * y la línea de comando específica. El namespace es
 * {@code impulso.code-exec.*}.</p>
 *
 * <p>El modo por defecto es {@link Mode#STUB} para no requerir Docker en
 * entornos que no lo proveen (por ejemplo, el hosting actual del backend
 * en Fly.io). Un runner dedicado con Docker debe activar
 * {@code impulso.code-exec.mode=docker-cli} y contar con el binario de
 * {@code docker} accesible al proceso.</p>
 */
@ConfigurationProperties(prefix = "impulso.code-exec")
public class CodeExecutionProperties {

    /** Modo del sandbox. */
    public enum Mode {
        /** Rechaza toda ejecución con un mensaje explícito. */
        STUB,
        /** Ejecuta cada solicitud en un contenedor Docker efímero. */
        DOCKER_CLI,
        /** Delegar la ejecución al sandbox público Piston. */
        PISTON,
        /** Delegar la ejecución al sandbox público CodeX. */
        CODEX,
        /** Delegar la ejecución a Judge0 CE (vía RapidAPI). */
        JUDGE0
    }

    /** Modo activo del sandbox. */
    private Mode mode = Mode.STUB;

    /** Ruta o nombre del binario de Docker. */
    private String dockerBinary = "docker";

    /** Timeout aplicado si el laboratorio no define uno explícito. */
    private int defaultTimeoutMs = 5_000;

    /** Timeout máximo aceptado; cualquier valor mayor se recorta. */
    private int maxTimeoutMs = 15_000;

    /** Bytes máximos capturados por flujo (stdout y stderr). */
    private int maxOutputBytes = 65_536;

    /** Memoria máxima del contenedor (formato de {@code docker --memory}). */
    private String memoryLimit = "128m";

    /** Núcleos de CPU asignados al contenedor. */
    private String cpus = "0.5";

    /** Cantidad máxima de procesos dentro del contenedor. */
    private int pidsLimit = 64;

    /** Usuario dentro del contenedor (UID:GID). Por defecto {@code nobody}. */
    private String user = "65534:65534";

    /** Tamaño del tmpfs montado en {@code /tmp} dentro del contenedor. */
    private String tmpfsSize = "32m";

    /** Catálogo de lenguajes soportados indexado por identificador. */
    private Map<String, LanguageProfile> languages = new HashMap<>();

    /** Configuración específica para el modo Piston. */
    private Piston piston = new Piston();

    /**
     * Configuración del cliente al sandbox público de Piston.
     *
     * <p>Piston expone {@code POST {baseUrl}/execute} para lanzar código
     * en múltiples lenguajes sin autenticación. Se usa como plan B cuando
     * el runner con Docker no está disponible (por ejemplo, en Fly.io).</p>
     */
    public static class Piston {
        private String baseUrl = "https://emkc.org/api/v2/piston";
        private int connectTimeoutMs = 5_000;
        private int readTimeoutMs = 20_000;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
        public int getReadTimeoutMs() { return readTimeoutMs; }
        public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    }

    public Piston getPiston() { return piston; }
    public void setPiston(Piston piston) { this.piston = piston; }

    /** Configuración específica para el modo CodeX. */
    private Codex codex = new Codex();

    /**
     * Configuración del cliente al sandbox público de CodeX.
     *
     * <p>CodeX expone {@code POST {baseUrl}/} y devuelve la salida del
     * programa junto con posibles errores de compilación. Es la
     * alternativa recomendada tras el cierre de la API pública de
     * Piston.</p>
     */
    public static class Codex {
        private String baseUrl = "https://api.codex.jaagrav.in";
        private int connectTimeoutMs = 5_000;
        private int readTimeoutMs = 20_000;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
        public int getReadTimeoutMs() { return readTimeoutMs; }
        public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    }

    public Codex getCodex() { return codex; }
    public void setCodex(Codex codex) { this.codex = codex; }

    /** Configuración específica para el modo Judge0. */
    private Judge0 judge0 = new Judge0();

    /**
     * Configuración del cliente al sandbox Judge0 CE (RapidAPI).
     *
     * <p>La clave API es obligatoria y debe suministrarse mediante el
     * secreto {@code CODE_EXEC_JUDGE0_KEY}. La URL base y el host
     * apuntan por defecto al mirror oficial hospedado en RapidAPI.</p>
     */
    public static class Judge0 {
        private String baseUrl = "https://judge0-ce.p.rapidapi.com";
        private String rapidApiHost = "judge0-ce.p.rapidapi.com";
        private String apiKey = "";
        private int connectTimeoutMs = 5_000;
        private int readTimeoutMs = 25_000;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getRapidApiHost() { return rapidApiHost; }
        public void setRapidApiHost(String rapidApiHost) { this.rapidApiHost = rapidApiHost; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
        public int getReadTimeoutMs() { return readTimeoutMs; }
        public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    }

    public Judge0 getJudge0() { return judge0; }
    public void setJudge0(Judge0 judge0) { this.judge0 = judge0; }

    /**
     * Perfil de lenguaje soportado por el sandbox.
     *
     * <p>Cada lenguaje se traduce en una imagen Docker, el nombre del
     * archivo temporal donde se escribe el código y la línea de comando
     * ejecutada. El token {@code {file}} en {@link #command} se
     * reemplaza por la ruta absoluta al archivo dentro del contenedor
     * ({@code /workspace/&lt;filename&gt;}).</p>
     */
    public static class LanguageProfile {
        private String image;
        private String filename;
        private List<String> command = new ArrayList<>();

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public List<String> getCommand() {
            return command;
        }

        public void setCommand(List<String> command) {
            this.command = command;
        }
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getDockerBinary() {
        return dockerBinary;
    }

    public void setDockerBinary(String dockerBinary) {
        this.dockerBinary = dockerBinary;
    }

    public int getDefaultTimeoutMs() {
        return defaultTimeoutMs;
    }

    public void setDefaultTimeoutMs(int defaultTimeoutMs) {
        this.defaultTimeoutMs = defaultTimeoutMs;
    }

    public int getMaxTimeoutMs() {
        return maxTimeoutMs;
    }

    public void setMaxTimeoutMs(int maxTimeoutMs) {
        this.maxTimeoutMs = maxTimeoutMs;
    }

    public int getMaxOutputBytes() {
        return maxOutputBytes;
    }

    public void setMaxOutputBytes(int maxOutputBytes) {
        this.maxOutputBytes = maxOutputBytes;
    }

    public String getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(String memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public String getCpus() {
        return cpus;
    }

    public void setCpus(String cpus) {
        this.cpus = cpus;
    }

    public int getPidsLimit() {
        return pidsLimit;
    }

    public void setPidsLimit(int pidsLimit) {
        this.pidsLimit = pidsLimit;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTmpfsSize() {
        return tmpfsSize;
    }

    public void setTmpfsSize(String tmpfsSize) {
        this.tmpfsSize = tmpfsSize;
    }

    public Map<String, LanguageProfile> getLanguages() {
        return languages;
    }

    public void setLanguages(Map<String, LanguageProfile> languages) {
        this.languages = languages;
    }

    /**
     * Resuelve el timeout efectivo aplicando el valor por defecto y el
     * tope superior configurados.
     */
    public int resolveTimeoutMs(int requestedMs) {
        int base = requestedMs > 0 ? requestedMs : defaultTimeoutMs;
        return Math.min(base, maxTimeoutMs);
    }
}

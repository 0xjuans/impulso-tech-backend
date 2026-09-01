package tech.impulso.gamification.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio dedicado al cálculo de rankings de estudiantes
 * (RF-021 / RF-049).
 *
 * <p>Utiliza {@link JdbcTemplate} porque las agregaciones y ventanas
 * necesarias se resuelven de manera más limpia con SQL nativo que con
 * JPQL. Los resultados se devuelven como filas simples que el servicio
 * transforma en DTOs.</p>
 */
@Repository
public class RankingRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RankingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Devuelve las primeras {@code limit} entradas del ranking histórico,
     * ordenadas por XP total descendente.
     *
     * @param limit cantidad máxima de entradas.
     * @return filas del ranking.
     */
    public List<RankingRow> topAllTime(int limit) {
        String sql = """
                select u.id,
                       u.username,
                       u.first_name,
                       u.last_name,
                       u.profile_photo_url,
                       ux.total_xp,
                       ux.current_level
                from users u
                join user_xp ux on ux.user_id = u.id
                where u.status = 'ACTIVA'
                  and u.show_in_ranking = true
                order by ux.total_xp desc, ux.updated_at asc
                limit ?
                """;
        return jdbcTemplate.query(sql, this::mapRow, limit);
    }

    /**
     * Devuelve las primeras entradas del ranking por periodo (semanal o
     * mensual), calculado a partir de la XP otorgada dentro del
     * intervalo indicado.
     *
     * @param since fecha desde la cual se acumula la XP.
     * @param limit cantidad máxima de entradas.
     * @return filas del ranking.
     */
    public List<RankingRow> topSince(OffsetDateTime since, int limit) {
        String sql = """
                select u.id,
                       u.username,
                       u.first_name,
                       u.last_name,
                       u.profile_photo_url,
                       coalesce(sum(xe.xp_awarded), 0) as period_xp,
                       ux.current_level
                from users u
                join user_xp ux on ux.user_id = u.id
                left join xp_events xe on xe.user_id = u.id and xe.created_at >= ?
                where u.status = 'ACTIVA'
                  and u.show_in_ranking = true
                group by u.id, u.username, u.first_name, u.last_name, u.profile_photo_url, ux.current_level
                having coalesce(sum(xe.xp_awarded), 0) > 0
                order by period_xp desc
                limit ?
                """;
        return jdbcTemplate.query(sql, this::mapRow, since, limit);
    }

    /**
     * Devuelve la posición y XP histórica de un usuario específico.
     *
     * @param userId identificador del usuario.
     * @return posición 1-indexada y XP; {@code null} si el usuario no
     *         cumple los criterios para aparecer.
     */
    public RankingPosition positionAllTime(Long userId) {
        String sql = """
                with ranked as (
                    select u.id,
                           ux.total_xp,
                           ux.current_level,
                           rank() over (order by ux.total_xp desc, ux.updated_at asc) as position
                    from users u
                    join user_xp ux on ux.user_id = u.id
                    where u.status = 'ACTIVA'
                      and u.show_in_ranking = true
                )
                select position, total_xp, current_level
                from ranked
                where id = ?
                """;
        List<RankingPosition> rows = jdbcTemplate.query(sql,
                (rs, i) -> new RankingPosition(rs.getLong("position"),
                        rs.getInt("total_xp"),
                        rs.getInt("current_level")),
                userId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Devuelve la posición y XP acumulada de un usuario dentro de un
     * periodo específico.
     *
     * @param userId identificador del usuario.
     * @param since  fecha desde la cual se acumula la XP.
     * @return posición 1-indexada y XP del periodo; {@code null} si el
     *         usuario no cumple los criterios o no obtuvo XP.
     */
    public RankingPosition positionSince(Long userId, OffsetDateTime since) {
        String sql = """
                with periodo as (
                    select u.id,
                           coalesce(sum(xe.xp_awarded), 0) as period_xp,
                           ux.current_level
                    from users u
                    join user_xp ux on ux.user_id = u.id
                    left join xp_events xe on xe.user_id = u.id and xe.created_at >= ?
                    where u.status = 'ACTIVA'
                      and u.show_in_ranking = true
                    group by u.id, ux.current_level
                    having coalesce(sum(xe.xp_awarded), 0) > 0
                ),
                ranked as (
                    select id, period_xp, current_level,
                           rank() over (order by period_xp desc) as position
                    from periodo
                )
                select position, period_xp, current_level
                from ranked
                where id = ?
                """;
        List<RankingPosition> rows = jdbcTemplate.query(sql,
                (rs, i) -> new RankingPosition(rs.getLong("position"),
                        rs.getInt("period_xp"),
                        rs.getInt("current_level")),
                since, userId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Mapea una fila SQL genérica del ranking, aceptando tanto el
     * agregado histórico ({@code total_xp}) como el del periodo
     * ({@code period_xp}).
     */
    private RankingRow mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        String xpColumn = hasColumn(rs, "total_xp") ? "total_xp" : "period_xp";
        return new RankingRow(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("profile_photo_url"),
                rs.getInt(xpColumn),
                rs.getInt("current_level")
        );
    }

    /**
     * Verifica si el {@link java.sql.ResultSet} incluye una columna con
     * el nombre indicado. Se utiliza para reutilizar el mapeo entre las
     * consultas de todos los tiempos y las de periodo.
     */
    private boolean hasColumn(java.sql.ResultSet rs, String columnName) throws java.sql.SQLException {
        java.sql.ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fila plana devuelta por las consultas del ranking. Se convierte a
     * DTO público en el servicio.
     *
     * @param userId          identificador del usuario.
     * @param username        nombre de usuario público.
     * @param firstName       nombre.
     * @param lastName        apellido.
     * @param profilePhotoUrl URL de la foto de perfil.
     * @param xp              XP considerada para el ranking (histórica o del periodo).
     * @param currentLevel    nivel actual del usuario.
     */
    public record RankingRow(
            Long userId,
            String username,
            String firstName,
            String lastName,
            String profilePhotoUrl,
            int xp,
            int currentLevel
    ) {
    }

    /**
     * Posición y XP de un usuario específico dentro del ranking.
     *
     * @param position     posición 1-indexada.
     * @param xp           XP considerada para el ranking.
     * @param currentLevel nivel actual del usuario.
     */
    public record RankingPosition(
            long position,
            int xp,
            int currentLevel
    ) {
    }
}

package tech.impulso.statistics.dto;

import java.time.LocalDate;

/**
 * Punto de una serie temporal diaria utilizado en las estadísticas
 * agregadas (RF-058).
 *
 * @param date  día al que corresponde el valor.
 * @param count cantidad de eventos registrados en ese día.
 */
public record TimeSeriesPoint(LocalDate date, long count) {
}

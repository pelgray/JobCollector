package com.pelgray.domain.location;

/**
 * Станция метро
 */
public class MetroStation {
    /**
     * Идентификатор станции метро
     */
    String station_id;
    /**
     * Название станции метро
     */
    String station_name;
    /**
     * Идентификатор линии метро, на которой находится станция
     */
    String line_id;
    /**
     * Название линии метро, на которой находится станция
     */
    String line_name;
    /**
     * Географическая широта станции метро
     */
    double lat;
    /**
     * Географическая долгота станции метро
     */
    double lng;

    @Override
    public String toString() {
        return station_name;
    }
}

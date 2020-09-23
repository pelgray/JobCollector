package com.pelgray.domain.location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Адрес вакансии
 */
public class Address {
    /**
     * Город
     */
    String city;
    /**
     * Улица
     */
    String street;
    /**
     * Номер дома
     */
    String building;
    /**
     * Дополнительная информация об адресе
     */
    String description;
    /**
     * Географическая широта
     */
    double lat;
    /**
     * Географическая долгота
     */
    double lng;
    /**
     * Список станций метро, может быть пустым
     */
    List<MetroStation> metro_stations;

    @Override
    public String toString() {
        ArrayList<String> data = new ArrayList<>(Arrays.asList(city, street, building));
        if (metro_stations != null && !metro_stations.isEmpty()) {
            data.add("метро " +
                    metro_stations.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }
        data.removeAll(Arrays.asList(null, ""));
        String result = String.join(", ", data);
        if (description != null) {
            if (!data.isEmpty()) {
                result += "\nОписание: ";
            }
            result += description;
        }
        return result;
    }
}

package com.pelgray.domain.employer;

/**
 * Специализации
 */
public class Specialization {
    /**
     * Идентификатор специализации
     */
    String id;
    /**
     * Название специализации
     */
    String name;
    /**
     * Идентификатор профессиональной области, в которую входит специализация
     */
    String profarea_id;
    /**
     * Название профессиональной области, в которую входит специализация
     */
    String profarea_name;

    @Override
    public String toString() {
        return String.format("%s (%s)", name, profarea_name);
    }
}

package com.pelgray.domain.requirements;

/**
 * Требуемый опыт работы
 */
public class Experience {
    /**
     * Идентификатор требуемого опыта работы
     */
    String id;
    /**
     * Название требуемого опыта работы
     */
    String name;

    @Override
    public String toString() {
        return name;
    }
}

package com.pelgray.domain.requirements;

/**
 * Ключевой навык
 */
public class KeySkill {
    /**
     * Название ключевого навыка
     */
    String name;

    public KeySkill(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

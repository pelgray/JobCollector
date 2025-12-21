package com.pelgray.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для создания заголовков в Google таблице
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SheetColumn {
    /**
     * Имя заголовка в таблице.
     */
    String name();

    /**
     * Тип значения в таблице.
     * <br>
     * См. подробнее {@link com.google.api.services.sheets.v4.model.ExtendedValue}
     */
    SheetColumnType type();
}

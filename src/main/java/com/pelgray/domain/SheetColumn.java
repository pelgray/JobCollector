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
     * Возможные значения: `boolValue`, `errorValue`, `formulaValue`, `numberValue`, `stringValue`.
     * <p>
     * См. подробнее {@link com.google.api.services.sheets.v4.model.ExtendedValue}
     */
    String type();

    /**
     * Применить для поля функцию "Вставить флажок". Для всех типов, кроме {@code boolean}, игнорируется.
     */
    boolean checkBox() default false;
}

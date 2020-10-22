package com.pelgray.domain;

/**
 * Тип значения в таблице.
 * <br>
 * Возможные значения задаются классом {@link com.google.api.services.sheets.v4.model.ExtendedValue}
 */
public enum SheetColumnType {
    BOOLEAN("boolValue"),
    ERROR("errorValue"),
    FORMULA("formulaValue"),
    NUMBER("numberValue"),
    STRING("stringValue");

    private final String typeName;

    SheetColumnType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}

package com.ps.restaurant.model;

public enum TableShape {
    RECTANGULAR("rect"),
    CIRCULAR("round");

    private final String value;

    TableShape(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
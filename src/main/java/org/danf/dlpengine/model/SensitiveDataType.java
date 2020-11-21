package org.danf.dlpengine.model;

public enum  SensitiveDataType {

    SSN("Social Security Number"),
    IBAN("IBAN Bank Account Code");

    private final String name;

    SensitiveDataType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}

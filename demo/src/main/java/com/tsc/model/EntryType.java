package com.tsc.model;

// EntryType Enum
public enum EntryType {
    REGISTERED("Registered"),
    USA("USA"),
    NON_REGISTERED("Non-registered"),
    FAMILY_AREA("Family Area"),
    SNP("SNP"),
    NORMAL_LOT("Normal Lot");

    private final String displayName;

    EntryType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
package com.eventbooking.domain.enums;

public enum StatutEvent {
    BROUILLON("Brouillon", "#9E9E9E", true, true),
    PUBLIE("Publié", "#4CAF50", true, false),
    ANNULE("Annulé", "#F44336", false, false),
    TERMINE("Terminé", "#607D8B", false, false);

    private final String label;
    private final String color;
    private final boolean canModify;
    private final boolean canDelete;

    StatutEvent(String label, String color, boolean canModify, boolean canDelete) {
        this.label = label;
        this.color = color;
        this.canModify = canModify;
        this.canDelete = canDelete;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }

    public boolean canModify() {
        return canModify;
    }

    public boolean canDelete() {
        return canDelete;
    }
}

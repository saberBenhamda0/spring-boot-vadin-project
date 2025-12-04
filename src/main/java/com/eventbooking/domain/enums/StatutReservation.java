package com.eventbooking.domain.enums;

public enum StatutReservation {
    EN_ATTENTE("En attente", "#FF9800", true),
    CONFIRMEE("Confirmée", "#4CAF50", true),
    ANNULEE("Annulée", "#F44336", false);

    private final String label;
    private final String color;
    private final boolean canCancel;

    StatutReservation(String label, String color, boolean canCancel) {
        this.label = label;
        this.color = color;
        this.canCancel = canCancel;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }

    public boolean canCancel() {
        return canCancel;
    }
}

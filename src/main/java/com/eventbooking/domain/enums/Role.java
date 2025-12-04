package com.eventbooking.domain.enums;

public enum Role {
    ADMIN("Administrateur", "#FF5722"),
    ORGANIZER("Organisateur", "#2196F3"),
    CLIENT("Client", "#4CAF50");

    private final String label;
    private final String color;

    Role(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
}

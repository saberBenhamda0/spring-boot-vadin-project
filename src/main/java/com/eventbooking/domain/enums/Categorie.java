package com.eventbooking.domain.enums;

public enum Categorie {
    CONCERT("Concert", "🎵", "#E91E63"),
    THEATRE("Théâtre", "🎭", "#9C27B0"),
    CONFERENCE("Conférence", "🎤", "#3F51B5"),
    SPORT("Sport", "⚽", "#FF9800"),
    AUTRE("Autre", "📌", "#607D8B");

    private final String label;
    private final String icon;
    private final String color;

    Categorie(String label, String icon, String color) {
        this.label = label;
        this.icon = icon;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }
}

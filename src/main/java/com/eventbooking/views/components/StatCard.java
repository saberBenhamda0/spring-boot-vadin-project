package com.eventbooking.views.components;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Reusable statistics card component
 */
public class StatCard extends VerticalLayout {

    private final H2 valueLabel;
    private final Paragraph titleLabel;
    private final Icon icon;

    public StatCard(String title, String value, VaadinIcon vaadinIcon, String color) {
        setWidth("250px");
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("border-left", "4px solid " + color);

        icon = vaadinIcon.create();
        icon.setSize("32px");
        icon.setColor(color);

        titleLabel = new Paragraph(title);
        titleLabel.getStyle()
                .set("margin", "5px 0")
                .set("color", "#666")
                .set("font-size", "0.9em");

        valueLabel = new H2(value);
        valueLabel.getStyle()
                .set("margin", "0")
                .set("color", color)
                .set("font-weight", "bold");

        add(icon, titleLabel, valueLabel);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}

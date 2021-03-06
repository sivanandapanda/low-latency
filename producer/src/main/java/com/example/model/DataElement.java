package com.example.model;

public class DataElement {

    private final Element element;
    private final double value;

    public DataElement(Element element, double value) {
        this.element = element;
        this.value = value;
    }

    public String asJson() {
        return "{ element : \"" + element + "\", value : " + value + " } ";
    }
}

package com.example.model;

import java.util.Date;

public class DataElement {

    private final Element element;
    private final double value;
    private final long publishedTime;

    public DataElement(Element element, double value) {
        this.element = element;
        this.value = value;
        this.publishedTime = new Date().getTime();
    }

    public String asJson() {
        return "{element:\"" + element + "\",value:" + value + ",publishedTime:" + publishedTime + "}";
    }
}

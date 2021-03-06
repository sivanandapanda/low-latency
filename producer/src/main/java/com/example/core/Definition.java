package com.example.core;

import com.example.model.Element;

import java.util.*;

public class Definition {

    private static final List<Element> elements = new ArrayList<>();
    private static final Map<Element, Integer> elementPublishFrequencyPerSec = new HashMap<>();

    public Definition(Element[] elementsArr) {
        Random random = new Random();
        Arrays.stream(elementsArr).forEach(e -> {
            elements.add(e);
            elementPublishFrequencyPerSec.put(e, random.nextInt(10) + 1);
        });
    }

    public int getFrequency(Element element) {
        return elementPublishFrequencyPerSec.get(element);
    }

    public Map<Element, Integer> getElementPublishFrequencyPerSec() {
        return Collections.unmodifiableMap(elementPublishFrequencyPerSec);
    }

    public List<Element> getElements() {
        return elements;
    }
}

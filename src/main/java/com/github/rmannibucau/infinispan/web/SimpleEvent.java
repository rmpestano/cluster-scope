package com.github.rmannibucau.infinispan.web;

/**
 * Created by rmpestano on 9/21/14.
 */
public class SimpleEvent {

    String name;

    public SimpleEvent(String name) {
        this.name = name;
    }

    public Person getPerson() {
        return new Person(name+"_event");
    }
}

package com.gkcontas.archsample.domain.model;

import org.springframework.stereotype.Component;

/**
 * A domain class that reached for a framework annotation — exactly the violation the real
 * rules forbid, written on purpose so the rules can be shown to catch it.
 *
 * <p>Note the package: {@code com.gkcontas.archsample}, deliberately outside
 * {@code com.gkcontas.hexagonal}. A {@code @Component} living under the application's base
 * package would be picked up by the component scan of every {@code @SpringBootTest} in the
 * suite, and Spring would then try to build it — failing on the {@code String} constructor
 * parameter, in test classes that have nothing to do with it.
 */
@Component
public class LeakyRoom {

    private final String name;

    public LeakyRoom(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}

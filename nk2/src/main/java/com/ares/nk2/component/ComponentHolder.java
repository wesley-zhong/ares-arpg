package com.ares.nk2.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentHolder.class);

    private BaseComponent component = null;

    public ComponentHolder(BaseComponent component) {
        this.component = component;
    }

    public BaseComponent getComponent() {
        return component;
    }

    public void setComponent(BaseComponent component) {
        this.component = component;
    }
}

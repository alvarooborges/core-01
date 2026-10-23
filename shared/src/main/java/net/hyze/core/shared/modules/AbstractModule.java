package net.hyze.core.shared.modules;

import net.hyze.core.shared.apps.App;

import java.util.List;

public abstract class AbstractModule {

    abstract public App getApp();

    abstract public List<String> getEchoChannels();

    public void onPrepared() throws IllegalStateException {

    }
}

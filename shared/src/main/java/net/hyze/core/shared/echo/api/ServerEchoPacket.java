package net.hyze.core.shared.echo.api;

import dev.utils.echo.packet.AbstractEchoPacket;
import net.hyze.core.shared.CoreProvider;

import java.util.Objects;

public abstract class ServerEchoPacket extends AbstractEchoPacket {

    @Override
    public String getSuffix() {
        return CoreProvider.getApp().getServer().getId();
    }

    public boolean isLocal() {
        return Objects.equals(handleHeader.getSender(), CoreProvider.getApp().getId());
    }
}

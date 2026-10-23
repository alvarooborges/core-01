package net.hyze.core.shared.echo.packets.user.connect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ConnectReason {
    JOIN, WARP, TPA(false), HOME, PLUGIN, RESPAWN, RECONNECT;

    private boolean allowSplit = true;
}

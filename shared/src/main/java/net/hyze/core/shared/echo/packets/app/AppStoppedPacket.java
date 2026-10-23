package net.hyze.core.shared.echo.packets.app;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hyze.core.shared.apps.App;

@Getter
@NoArgsConstructor
public class AppStoppedPacket extends AbstractAppPacket {

    public AppStoppedPacket(App app) {
        super(app);
    }

}

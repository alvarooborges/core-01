package net.hyze.core.shared.user.modules;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.App;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserBaseStatus {

    private Integer userId;

    private String nick;

    private String proxyAppId;

    private String serverAppId;

    public UserBaseStatus(Integer userId, String nick, String proxyAppId) {
        this(userId, nick, proxyAppId, null);
    }

    public App getProxyApp() {
        return CoreProvider.Cache.Local.APPS.provide().get(proxyAppId);
    }

    public App getApp() {
        return CoreProvider.Cache.Local.APPS.provide().get(serverAppId);
    }
}

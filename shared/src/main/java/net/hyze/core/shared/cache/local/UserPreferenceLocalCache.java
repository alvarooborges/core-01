package net.hyze.core.shared.cache.local;

import com.google.common.collect.Maps;
import java.util.Map;
import lombok.NonNull;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.user.User;
import net.hyze.core.shared.user.preferences.UserPreference;

public class UserPreferenceLocalCache implements LocalCache {

    private final Map<Integer, UserPreference> CACHE = Maps.newConcurrentMap();

    public UserPreference get(@NonNull User user) {
        UserPreference userPreference = this.CACHE.get(user.getId());

        if (userPreference == null) {
            userPreference = refresh(user.getId());
        }

        return userPreference;
    }

    public void remove(@NonNull User user) {
        this.CACHE.remove(user.getId());
    }

    public UserPreference refresh(@NonNull Integer userId) {
        UserPreference preferences = CoreProvider.Repositories.USERS_PREFERENCES.provide().fetchUserPreference(userId);

        this.CACHE.remove(userId);
        this.CACHE.put(userId, preferences);

        return preferences;
    }

}

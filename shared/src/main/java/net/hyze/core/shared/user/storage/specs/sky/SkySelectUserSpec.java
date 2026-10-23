package net.hyze.core.shared.user.storage.specs.sky;

import net.hyze.core.shared.storage.repositories.specs.SelectSqlSpec;
import net.hyze.core.shared.user.User;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.util.Date;

public abstract class SkySelectUserSpec extends SelectSqlSpec<User> {

    @Override
    public ResultSetExtractor<User> getResultSetExtractor() {
        return (ResultSet result) -> {

            if (result.next()) {
                int id = result.getInt("id");
                String nick = result.getString("nick");
                String password = result.getString("password");
                String email = result.getString("email");
                long createdAt = result.getLong("created_at");

                return new User(
                        id,
                        nick,
                        null,
                        password,
                        email,
                        null,
                        new Date(createdAt)
                );
            }

            return null;
        };
    }
}

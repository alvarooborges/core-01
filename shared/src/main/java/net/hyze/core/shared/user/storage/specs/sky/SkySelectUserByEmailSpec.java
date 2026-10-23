package net.hyze.core.shared.user.storage.specs.sky;

import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.user.storage.specs.hyze.SelectUserSpec;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.PreparedStatement;

@RequiredArgsConstructor
public class SkySelectUserByEmailSpec extends SkySelectUserSpec {

    private final String email;

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {

        return connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM `sky_users` WHERE `email` = ? LIMIT 1;"
            );

            statement.setString(1, this.email);

            return statement;
        };

    }
}

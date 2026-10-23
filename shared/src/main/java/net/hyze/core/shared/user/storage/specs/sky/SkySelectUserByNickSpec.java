package net.hyze.core.shared.user.storage.specs.sky;

import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.user.storage.specs.hyze.SelectUserSpec;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.PreparedStatement;

@RequiredArgsConstructor
public class SkySelectUserByNickSpec extends SkySelectUserSpec {

    private final String nick;

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM `sky_users` WHERE `nick` = ? LIMIT 1;"
            );

            statement.setString(1, this.nick);

            return statement;
        };
    }
}

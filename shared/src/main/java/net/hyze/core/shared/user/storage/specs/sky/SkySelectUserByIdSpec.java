package net.hyze.core.shared.user.storage.specs.sky;

import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.user.storage.specs.hyze.SelectUserSpec;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.PreparedStatement;

@RequiredArgsConstructor
public class SkySelectUserByIdSpec extends SkySelectUserSpec {

    private final Integer id;

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM `sky_users` WHERE `id` = ? LIMIT 1;"
            );

            statement.setInt(1, this.id);

            return statement;
        };
    }
}

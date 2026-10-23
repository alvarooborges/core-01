package net.hyze.core.shared.user.storage.specs.sky;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.storage.repositories.specs.SelectSqlSpec;
import net.hyze.core.shared.user.User;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

@RequiredArgsConstructor
public class SkySelectLastLoginByUserSpec extends SelectSqlSpec<Long> {

    private final User user;

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT `last_login` FROM `sky_users_profiles` WHERE `user` = ? LIMIT 1;"
            );

            statement.setInt(1, user.getId());

            return statement;
        };
    }

    @Override
    public ResultSetExtractor<Long> getResultSetExtractor() {
        return (ResultSet result) -> {

            if (result.next()) {
                return result.getLong("last_login");
            }

            return null;
        };

    }
}

package net.hyze.core.shared.user.storage.specs.sky.cash;

import java.sql.PreparedStatement;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.storage.repositories.specs.SelectSqlSpec;
import net.hyze.core.shared.user.User;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

@RequiredArgsConstructor
public class SkySelectCashByUserSpec extends SelectSqlSpec<Integer> {

    private final User user;

    @Override
    public ResultSetExtractor<Integer> getResultSetExtractor() {
        return result -> {
            if (result.next()) {
                return result.getInt("cash");
            }

            return 0;
        };
    }

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            String query = "SELECT `cash` FROM `sky_users_profiles` WHERE `user` = ? AND `network` = 'HYZE' LIMIT 1;";

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, user.getId());

            return statement;
        };
    }

}

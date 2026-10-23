package net.hyze.core.shared.user.storage.specs.sky.cash;

import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.storage.repositories.specs.UpdateSqlSpec;
import net.hyze.core.shared.user.User;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.PreparedStatement;

@RequiredArgsConstructor
public class SkyIncrementCashByUserSpec extends UpdateSqlSpec<Boolean> {

    private final User user;
    private final int amount;

    @Override
    public Boolean parser(int affectedRows) {
        return affectedRows > 0;
    }

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            String query = "UPDATE `sky_users_profiles` " +
                    "SET `cash` = `cash` + ? " +
                    "WHERE `user` = ? " +
                    "AND `network` = 'HYZE' " +
                    "LIMIT 1;";

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, amount);
            statement.setInt(2, user.getId());

            return statement;
        };
    }
}

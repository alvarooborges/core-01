package net.hyze.core.shared.log.cash.storage.specs;

import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.storage.repositories.specs.UpdateSqlSpec;
import net.hyze.core.shared.user.User;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.PreparedStatement;
import java.sql.Timestamp;

@RequiredArgsConstructor
public class InsertCashLogSpec extends UpdateSqlSpec<Boolean> {

    private final User user;
    private final String serverId;
    private final String description;
    private final int postingValue;
    private final int previousBalance;
    private final int subsequentBalance;

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            PreparedStatement statement;

            statement = connection.prepareStatement(
                    String.format(
                            "INSERT INTO `%s` (`user_id`, `user_group`, `server_id`, `description`, `posting_value`, `previous_balance`, `subsequent_balance`, `created_at`) "
                                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?) ",
                            CoreConstants.Databases.Mysql.Tables.CASH_LOG
                    )
            );

            statement.setInt(1, this.user.getId());
            statement.setString(2, this.user.getHighestGroup().name());
            statement.setString(3, this.serverId);
            statement.setString(4, this.description);
            statement.setInt(5, this.postingValue);
            statement.setInt(6, this.previousBalance);
            statement.setInt(7, this.subsequentBalance);
            statement.setTimestamp(8, new Timestamp(System.currentTimeMillis()));

            return statement;
        };
    }

    @Override
    public Boolean parser(int affectedRows) {
        return affectedRows > 0;
    }
}

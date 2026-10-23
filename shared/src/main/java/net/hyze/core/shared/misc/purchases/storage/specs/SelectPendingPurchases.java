package net.hyze.core.shared.misc.purchases.storage.specs;

import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.misc.purchases.Purchase;
import net.hyze.core.shared.misc.purchases.PurchaseState;
import net.hyze.core.shared.misc.purchases.PurchaseType;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.storage.repositories.specs.SelectSqlSpec;
import net.hyze.core.shared.user.User;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class SelectPendingPurchases extends SelectSqlSpec<List<Purchase>> {

    private final User user;
    private final String pendingField;

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            String query = String.format(
                    "SELECT * FROM `purchases` WHERE `receiver_user_id` = ? AND `%s` = 'PENDING';",
                    pendingField
            );

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, user.getId());

            return statement;
        };
    }

    @Override
    public ResultSetExtractor<List<Purchase>> getResultSetExtractor() {
        return resultSet -> {
            List<Purchase> out = new ArrayList<>();

            while (resultSet.next()) {
                out.add(new Purchase(
                        resultSet.getInt("id"),
                        resultSet.getString("platform"),
                        resultSet.getString("platform_id"),
                        resultSet.getString("gateway"),
                        resultSet.getString("gateway_id"),
                        resultSet.getInt("buyer_user_id"),
                        resultSet.getInt("receiver_user_id"),
                        PurchaseType.valueOf(resultSet.getString("type")),
                        Server.getById(resultSet.getString("server_id")).orElse(null),
                        resultSet.getString("value"),
                        resultSet.getInt("cycle"),
                        resultSet.getDouble("total"),
                        resultSet.getTimestamp("created_at"),
                        PurchaseState.valueOf(resultSet.getString("activation_state")),
                        PurchaseState.valueOf(resultSet.getString("activation_state"))
                ));
            }

            return out;
        };
    }
}

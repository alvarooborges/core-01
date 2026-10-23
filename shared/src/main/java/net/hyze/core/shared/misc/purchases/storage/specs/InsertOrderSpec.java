package net.hyze.core.shared.misc.purchases.storage.specs;

import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.storage.repositories.specs.UpdateSqlSpec;
import net.hyze.core.shared.misc.purchases.Purchase;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.PreparedStatement;
import java.sql.Types;

@RequiredArgsConstructor
public class InsertOrderSpec extends UpdateSqlSpec<Boolean> {

    private final Purchase order;

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {

            String query = "INSERT INTO `purchases` (`platform`, `platform_id`, `gateway`, `gateway_id`, " +
                    "`buyer_user_id`, `receiver_user_id`, `type`, `value`, `cycle`, `total`, `server_id`) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, order.getPlatform());
            statement.setString(2, order.getPlatformId());
            statement.setString(3, order.getGateway());
            statement.setString(4, order.getGatewayId());
            statement.setInt(5, order.getBuyerId());
            statement.setInt(6, order.getReceiverId());
            statement.setString(7, order.getType().name());
            statement.setString(8, order.getValue());
            statement.setInt(9, order.getCycle());
            statement.setDouble(10, order.getTotal());

            if (order.getServer() != null) {
                statement.setString(11, order.getServer().getId());
            } else {
                statement.setNull(11, Types.VARCHAR);
            }

            return statement;
        };
    }

    @Override
    public Boolean parser(int affectedRows) {
        return affectedRows > 0;
    }
}

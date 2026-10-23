package net.hyze.core.shared.misc.purchases.storage.specs;

import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.misc.purchases.Purchase;
import net.hyze.core.shared.misc.purchases.PurchaseState;
import net.hyze.core.shared.storage.repositories.specs.UpdateSqlSpec;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.PreparedStatement;

@RequiredArgsConstructor
public class UpdateOrderStateSpec extends UpdateSqlSpec<Void> {

    private final Purchase purchase;
    private final String field;
    private final PurchaseState state;

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            String query = String.format(
                    "UPDATE `purchases` SET `%s` = ? WHERE `id`= ? LIMIT 1;",
                    field
            );

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, state.name());
            statement.setInt(2, purchase.getId());

            return statement;
        };
    }

    @Override
    public Void parser(int affectedRows) {
        return null;
    }
}

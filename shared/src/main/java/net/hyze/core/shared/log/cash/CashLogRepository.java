package net.hyze.core.shared.log.cash;

import net.hyze.core.shared.log.cash.storage.specs.InsertCashLogSpec;
import net.hyze.core.shared.providers.MysqlDatabaseProvider;
import net.hyze.core.shared.storage.repositories.MysqlRepository;
import net.hyze.core.shared.user.User;

public class CashLogRepository extends MysqlRepository {

    public CashLogRepository(MysqlDatabaseProvider databaseProvider) {
        super(databaseProvider);
    }

    public Boolean insertLog(User user, String serverId, String productId, int productPrice, int previousBalance, int subsequentBalance) {
        return query(new InsertCashLogSpec(user, serverId, productId, productPrice, previousBalance, subsequentBalance));
    }

}

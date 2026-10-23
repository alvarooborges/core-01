package net.hyze.core.shared.misc.purchases.storage;

import net.hyze.core.shared.misc.purchases.Purchase;
import net.hyze.core.shared.misc.purchases.PurchaseState;
import net.hyze.core.shared.misc.purchases.storage.specs.InsertOrderSpec;
import net.hyze.core.shared.misc.purchases.storage.specs.SelectPendingPurchases;
import net.hyze.core.shared.misc.purchases.storage.specs.UpdateOrderStateSpec;
import net.hyze.core.shared.providers.MysqlDatabaseProvider;
import net.hyze.core.shared.storage.repositories.MysqlRepository;
import net.hyze.core.shared.user.User;

import java.util.List;

public class PurchaseRepository extends MysqlRepository {

    public PurchaseRepository(MysqlDatabaseProvider databaseProvider) {
        super(databaseProvider);
    }

    public boolean createPurchase(Purchase order) {
        return query(new InsertOrderSpec(order));
    }

    public List<Purchase> fetchPendingActivationPurchases(User user) {
        return query(new SelectPendingPurchases(user, "activation_state"));
    }

    public void updateActivationState(Purchase purchase, PurchaseState state) {
        query(new UpdateOrderStateSpec(purchase, "activation_state", state));
    }

    public List<Purchase> fetchPendingAnnouncementPurchases(User user) {
        return query(new SelectPendingPurchases(user, "announcement_state"));
    }

    public void updateAnnouncementState(Purchase purchase, PurchaseState state) {
        query(new UpdateOrderStateSpec(purchase, "announcement_state", state));
    }
}

package net.hyze.core.shared.misc.purchases;

import java.util.List;

public interface PurchaseCreator<T extends PlatformOrder> {

    List<Purchase> createPurchases(T order);
}

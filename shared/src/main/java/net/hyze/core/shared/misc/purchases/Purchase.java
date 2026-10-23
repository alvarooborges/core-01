package net.hyze.core.shared.misc.purchases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.hyze.core.shared.servers.Server;

import java.util.Date;

@Getter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class Purchase {

    private int id;
    private final String platform;
    private final String platformId;
    private final String gateway;
    private final String gatewayId;
    private final int buyerId;
    private final int receiverId;
    private final PurchaseType type;
    private final Server server;
    private final String value;
    private final int cycle;
    private final double total;
    private final Date createdAt;

    private PurchaseState activationState;
    private PurchaseState announcementState;
}

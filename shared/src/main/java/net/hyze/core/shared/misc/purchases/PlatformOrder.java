package net.hyze.core.shared.misc.purchases;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PlatformOrder {

    private final String platform;
    private final String platformId;
    private final String gateway;
    private final String gatewayId;
}

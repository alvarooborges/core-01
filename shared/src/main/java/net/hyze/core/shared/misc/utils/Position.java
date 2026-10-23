package net.hyze.core.shared.misc.utils;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Position {

    protected double x, y, z;

    protected float yaw, pitch;

    public Position(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    @Override
    public Position clone() {
        return new Position(
                x, y, z,
                yaw, pitch
        );
    }
}

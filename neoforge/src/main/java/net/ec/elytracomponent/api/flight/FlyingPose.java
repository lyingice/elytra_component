package net.ec.elytracomponent.api.flight;

import net.minecraft.world.entity.LivingEntity;

public enum FlyingPose {
    SUPERMAN,
    UPRIGHT;

    public static FlyingPose get(LivingEntity entity) {
        return entity.isFallFlying() ? SUPERMAN : UPRIGHT;
    }
}

package net.ec.elytracomponent.api.flight;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface IJetpack {
    class Context {
        public final IJetpack jetpack;
        public final LivingEntity entity;
        public final Level world;
        public final FlyingPose pose;
        public final ISource source;

        public Context(IJetpack jetpack, LivingEntity entity, Level world, FlyingPose pose, ISource source) {
            this.jetpack = jetpack;
            this.entity = entity;
            this.world = world;
            this.pose = pose;
            this.source = source;
        }

        public static Builder builder(LivingEntity entity, Level world, FlyingPose pose, ISource source) {
            return new Builder(entity, world, pose, source);
        }

        public static class Builder {
            private final LivingEntity entity;
            private final Level world;
            private final FlyingPose pose;
            private final ISource source;

            Builder(LivingEntity entity, Level world, FlyingPose pose, ISource source) {
                this.entity = entity;
                this.world = world;
                this.pose = pose;
                this.source = source;
            }

            public Context build(IJetpack jetpack) {
                return new Context(jetpack, entity, world, pose, source);
            }
        }
    }

    default ControlType activeType(Context context) {
        return ControlType.ALWAYS;
    }

    double horizontalSpeed(Context context);

    double verticalSpeed(Context context);

    double acceleration(Context context);

    ControlType hoverType(Context context);

    double hoverSpeed(Context context);

    default double hoverVerticalSpeed(Context context) {
        return verticalSpeed(context) * 0.8;
    }

    default double hoverHorizontalSpeed(Context context) {
        return horizontalSpeed(context) * 0.8;
    }

    double swimModifier(Context context);

    @Deprecated
    default boolean boostsElytra() {
        return elytraBoost() > 0.0;
    }

    default double elytraBoost() {
        return 1.25;
    }

    boolean isValid(Context context);

    boolean isUsable(Context context);

    default void onUse(Context context) {}

    List<Vec3> getThrusters(Context context);

    ParticleOptions createParticles();

    default boolean isHovering(Context context) {
        return IFlightApi.INSTANCE.isActive(hoverType(context), FlightKey.TOGGLE_HOVER, context.entity);
    }

    default boolean isThrusting(Context context) {
        var entity = context.entity;
        if (entity.getVehicle() != null) return false;
        if (!IFlightApi.INSTANCE.isActive(
                context.jetpack.activeType(context),
                FlightKey.TOGGLE_ACTIVE,
                entity
        )) {
            return false;
        }
        if (context.pose == FlyingPose.SUPERMAN && entity.getDeltaMovement().length() > 0.1) return true;
        if (isHovering(context) && !entity.onGround()) return true;
        return FlightKey.UP.isPressed(entity);
    }
}

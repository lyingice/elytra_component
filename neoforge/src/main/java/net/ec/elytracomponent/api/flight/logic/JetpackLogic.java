package net.ec.elytracomponent.api.flight.logic;

import net.ec.elytracomponent.api.flight.*;
import net.ec.elytracomponent.api.flight.IJetpack.Context;
import net.ec.elytracomponent.api.flight.platform.Services;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class JetpackLogic {
    private static final List<KeyDirection> DIRECTIONS = List.of(
            new KeyDirection(FlightKey.BACKWARD, new Vec3(0.0, 0.0, -1.0).scale(0.8)),
            new KeyDirection(FlightKey.FORWARD, new Vec3(0.0, 0.0, 1.0).scale(1.2)),
            new KeyDirection(FlightKey.LEFT, new Vec3(1.0, 0.0, 0.0)),
            new KeyDirection(FlightKey.RIGHT, new Vec3(-1.0, 0.0, 0.0))
    );

    private static final ResourceLocation ATTRIBUTE_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "boost");

    private record KeyDirection(FlightKey key, Vec3 vector) {}

    private static void handleSwimModifier(LivingEntity entity, Context context) {
        var attribute = Services.REGISTRIES.getSwimSpeed();
        if (attribute == null) return;
        var attr = entity.getAttribute(attribute);
        if (attr == null) return;

        boolean hasModifier = attr.getModifier(ATTRIBUTE_ID) != null;
        boolean shouldHaveModifier = context != null && context.pose == FlyingPose.SUPERMAN && entity.isUnderWater();

        if (!shouldHaveModifier && hasModifier) {
            attr.removeModifier(ATTRIBUTE_ID);
        } else if (shouldHaveModifier && !hasModifier) {
            double modifier = context.jetpack.swimModifier(context);
            if (modifier > 0) {
                attr.addPermanentModifier(
                        new AttributeModifier(
                                ATTRIBUTE_ID,
                                modifier,
                                Operation.ADD_MULTIPLIED_TOTAL
                        )
                );
            }
        }
    }

    public static void onTick(LivingEntity entity) {
        var context = IFlightApi.INSTANCE.findActiveJetpack(entity);
        handleSwimModifier(entity, context);

        if (context == null) return;

        boolean isUsed = switch (context.pose) {
            case SUPERMAN -> elytraBoost(context);
            case UPRIGHT -> uprightMovement(context);
        };

        if (isUsed && context.jetpack.isThrusting(context)) {
            spawnParticles(context);
            playSound(context);
            context.jetpack.onUse(context);
        }
    }

    private static void playSound(Context context) {
        var pos = context.entity.blockPosition();

        float volume = FlightKey.UP.isPressed(context.entity) ? 2F : 1F;
        float pitch = context.world.random.nextFloat() * 0.4F + 1F;

        if (context.entity.isUnderWater()) {
            if (context.world.getGameTime() % 10 != 0) return;

            var soundData = switch (context.pose) {
                case SUPERMAN -> {
                    yield new SoundData(SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, -0.5F);
                }
                default -> {
                    yield new SoundData(SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, 0F);
                }
            };

            context.world.playSound(
                    null, pos,
                    soundData.sound(),
                    SoundSource.PLAYERS,
                    volume + soundData.volumeModifier(),
                    pitch - 0.5F
            );
        } else {
            if (context.world.getGameTime() % 5 != 0) return;
            SoundEvent whooshSound = Services.REGISTRIES.registerSound("whoosh");
            context.world.playSound(
                    null, pos,
                    whooshSound,
                    SoundSource.PLAYERS,
                    volume, pitch
            );
        }
    }

    private record SoundData(SoundEvent sound, float volumeModifier) {}

    private static boolean elytraBoost(Context ctx) {
        double boost = ctx.jetpack.elytraBoost();
        if (boost <= 0.0) return false;

        var entity = ctx.entity;
        if (!entity.isFallFlying()) return true;
        if (!(entity instanceof Player) || !FlightKey.UP.isPressed(entity)) return false;

        if (entity.level().getGameTime() % 15 == 0) {
            var look = entity.getLookAngle();
            entity.setDeltaMovement(
                    entity.getDeltaMovement().add(
                            look.x * 0.1 + (look.x * boost - look.x) * 0.5,
                            look.y * 0.1 + (look.y * boost - look.y) * 0.5,
                            look.z * 0.1 + (look.z * boost - look.z) * 0.5
                    )
            );
        }

        return true;
    }

    private static boolean uprightMovement(Context ctx) {
        var entity = ctx.entity;
        boolean buttonUp = FlightKey.UP.isPressed(entity);
        boolean buttonDown = entity.isShiftKeyDown();
        boolean hovering = IFlightApi.INSTANCE.isActive(
                ctx.jetpack.hoverType(ctx),
                FlightKey.TOGGLE_HOVER,
                entity
        );

        if (entity.getVehicle() != null) return false;
        if (entity.onGround() && !buttonUp) return false;

        double verticalSpeed = hovering ? ctx.jetpack.hoverVerticalSpeed(ctx) : ctx.jetpack.verticalSpeed(ctx);
        double horizontalSpeed = hovering
                ? (entity.isUnderWater() ? 0.0 : ctx.jetpack.hoverHorizontalSpeed(ctx))
                : ctx.jetpack.horizontalSpeed(ctx);
        double acceleration = ctx.jetpack.acceleration(ctx);

        Double speed;
        if (buttonUp && !buttonDown) {
            speed = verticalSpeed;
        } else if (buttonDown && !buttonUp) {
            speed = -verticalSpeed;
        } else if (hovering && entity.isUnderWater()) {
            speed = 0.0;
        } else if (hovering) {
            speed = ctx.jetpack.hoverSpeed(ctx);
        } else {
            speed = null;
        }

        if (speed != null) {
            if (entity instanceof Player) {
                for (var dir : DIRECTIONS) {
                    if (dir.key().isPressed(entity)) {
                        var vec = new Vec3(dir.vector().x, 0.0, dir.vector().z).scale(horizontalSpeed);
                        entity.moveRelative(1F, vec);
                    }
                }
            }

            var motion = entity.getDeltaMovement();
            double motionY;
            if (speed <= 0) {
                motionY = Math.max(motion.y, speed);
            } else {
                motionY = Math.min(motion.y + acceleration, speed);
            }

            entity.setDeltaMovement(motion.x, motionY, motion.z);

            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.fallDistance = 0F;
                // Reset above ground tick count via mixin accessor
                ((net.ec.elytracomponent.api.flight.mixin.ServerGamePacketListenerImplAccessor)
                        serverPlayer.connection).setAboveGroundTickCount(0);
            }
        }

        return true;
    }

    private static void spawnParticles(Context context) {
        var world = context.world;
        if (!world.isClientSide()) return;

        var thrusters = context.jetpack.getThrusters(context);
        if (thrusters == null) return;

        float yaw = (context.entity.yBodyRot / 180 * (float) -Math.PI);
        float pitch = (context.entity.getXRot() / 180 * (float) -Math.PI);
        float xRot = switch (context.pose) {
            case SUPERMAN -> pitch;
            case UPRIGHT -> 0F;
        };

        for (var pos : thrusters) {
            var rotated = pos.xRot(xRot).yRot(yaw).scale(context.entity.getScale());

            var particle = context.entity.isUnderWater()
                    ? ParticleTypes.BUBBLE
                    : context.jetpack.createParticles();

            world.addParticle(
                    particle,
                    context.entity.getX() + rotated.x,
                    context.entity.getY() + rotated.y,
                    context.entity.getZ() + rotated.z,
                    0.0, -1.0, 0.0
            );
        }
    }
}

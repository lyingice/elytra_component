package net.ec.elytracomponent.api.flight;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public enum FlightKey implements StringRepresentable {
    UP(null, false, false),
    DOWN(null, false, false),
    FORWARD(null, false, false),
    BACKWARD(null, false, false),
    LEFT(null, false, false),
    RIGHT(null, false, false),
    TOGGLE_ACTIVE(null, true, true),
    TOGGLE_HOVER(null, true, true);

    public final Integer defaultKey;
    public final boolean toggle;
    public final boolean default_;

    FlightKey(@Nullable Integer defaultKey, boolean toggle, boolean default_) {
        this.defaultKey = defaultKey;
        this.toggle = toggle;
        this.default_ = default_;
    }

    public boolean getDefault() {
        return default_;
    }

    // KeyMapping binding, set during registration
    public Optional<net.minecraft.client.KeyMapping> binding = Optional.empty();

    public boolean isPressed(LivingEntity entity) {
        return IFlightApi.INSTANCE.isPressed(this, entity);
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }

    public static final Codec<FlightKey> CODEC = StringRepresentable.fromEnum(FlightKey::values);
}

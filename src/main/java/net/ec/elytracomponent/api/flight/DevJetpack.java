package net.ec.elytracomponent.api.flight;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DevJetpack implements IJetpack {
    private static final List<Vec3> THRUSTERS = List.of(new Vec3(0.0, 0.8, -0.25));

    @Override
    public double horizontalSpeed(IJetpack.Context context) {
        return 0.02;
    }

    @Override
    public double verticalSpeed(IJetpack.Context context) {
        return 0.4;
    }

    @Override
    public double acceleration(IJetpack.Context context) {
        return 0.6;
    }

    @Override
    public ControlType hoverType(IJetpack.Context context) {
        return ControlType.TOGGLE;
    }

    @Override
    public double hoverSpeed(IJetpack.Context context) {
        return 0.0;
    }

    @Override
    public double swimModifier(IJetpack.Context context) {
        return 1.8;
    }

    @Override
    public boolean isValid(IJetpack.Context context) {
        return true;
    }

    @Override
    public boolean isUsable(IJetpack.Context context) {
        return true;
    }

    @Override
    public List<Vec3> getThrusters(IJetpack.Context context) {
        return THRUSTERS;
    }

    @Override
    public ParticleOptions createParticles() {
        return ParticleTypes.FLAME;
    }
}

package net.ec.elytracomponent.api.flight.platform;

import net.ec.elytracomponent.api.flight.Constants;
import net.ec.elytracomponent.api.flight.platform.services.IRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ForgeRegistries implements IRegistries {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, Constants.MOD_ID);

    private static Supplier<SoundEvent> whooshSound;

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);

        // Pre-register the whoosh sound
        whooshSound = SOUNDS.register("whoosh", () ->
                SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "whoosh")
                )
        );
    }

    @Override
    public Holder<Attribute> getSwimSpeed() {
        return NeoForgeMod.SWIM_SPEED;
    }

    @Override
    public SoundEvent registerSound(String name) {
        // For the "whoosh" sound which is pre-registered
        if ("whoosh".equals(name) && whooshSound != null) {
            return whooshSound.get();
        }
        // For other sounds, create inline (not recommended, but kept for API compatibility)
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name)
        );
    }
}

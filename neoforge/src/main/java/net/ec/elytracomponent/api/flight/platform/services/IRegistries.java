package net.ec.elytracomponent.api.flight.platform.services;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.Attribute;

public interface IRegistries {
    Holder<Attribute> getSwimSpeed();

    SoundEvent registerSound(String name);
}

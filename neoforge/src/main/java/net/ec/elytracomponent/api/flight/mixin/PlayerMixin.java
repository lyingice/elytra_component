package net.ec.elytracomponent.api.flight.mixin;

import net.ec.elytracomponent.api.flight.FlightKey;
import net.ec.elytracomponent.api.flight.logic.ISettingsStorage;
import net.ec.elytracomponent.api.flight.platform.ForgeDataAttachment;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

@Mixin(Player.class)
public class PlayerMixin implements ISettingsStorage {

    @Override
    public void flightlib$set(Map<FlightKey, Boolean> settings) {
        var self = (Player) (Object) this;
        self.setData(ForgeDataAttachment.SETTINGS_ATTACHMENT.get(), settings);
    }

    @Override
    public Map<FlightKey, Boolean> flightlib$get() {
        var self = (Player) (Object) this;
        return self.getData(ForgeDataAttachment.SETTINGS_ATTACHMENT.get());
    }
}

package net.ec.elytracomponent.api.flight.platform;

import net.ec.elytracomponent.api.flight.Constants;
import net.ec.elytracomponent.api.flight.FlightKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ForgeDataAttachment {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID);

    public static final Supplier<AttachmentType<Map<FlightKey, Boolean>>> SETTINGS_ATTACHMENT =
            ATTACHMENTS.register("jetpack_settings", () ->
                    AttachmentType.<Map<FlightKey, Boolean>>builder(() -> {
                        Map<FlightKey, Boolean> defaults = new HashMap<>();
                        defaults.put(FlightKey.TOGGLE_ACTIVE, true);
                        defaults.put(FlightKey.TOGGLE_HOVER, true);
                        return defaults;
                    }).build()
            );

    public static void register(net.neoforged.bus.api.IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}

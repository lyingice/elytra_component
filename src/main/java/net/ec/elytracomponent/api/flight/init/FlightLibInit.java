package net.ec.elytracomponent.api.flight.init;

import net.ec.elytracomponent.api.flight.*;
import net.ec.elytracomponent.api.flight.network.FlightLibNetwork;
import net.ec.elytracomponent.api.flight.platform.ForgeDataAttachment;
import net.ec.elytracomponent.api.flight.platform.ForgeRegistries;
import net.ec.elytracomponent.api.flight.platform.Services;
import net.ec.elytracomponent.api.flight.sources.EntitySource;
import net.ec.elytracomponent.api.flight.sources.EquipmentSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.List;

public class FlightLibInit {

    public static void init(IEventBus modEventBus) {
        // Register data attachments (must be done during mod construction)
        ForgeDataAttachment.register(modEventBus);

        // Register sounds (must be done during mod construction)
        ForgeRegistries.register(modEventBus);

        // Register network packets (uses ElytraComponentMod.addNetworkMessage, must be done before RegisterPayloadHandlersEvent)
        FlightLibNetwork.register();

        // Common setup (after registries are ready)
        modEventBus.addListener((FMLCommonSetupEvent event) -> {
            event.enqueueWork(FlightLibInit::registerSources);
        });
    }

    private static void registerSources() {
        // Equipment source provider: check all equipment slots
        IFlightApi.INSTANCE.addSourceProvider(entity -> {
            return List.of(EquipmentSlot.values()).stream()
                    .map(slot -> {
                        ItemStack stack = entity.getItemBySlot(slot);
                        return ISource.Pair.of((Object) stack, (ISource) new EquipmentSource(slot, stack));
                    })
                    .toList();
        });

        // Entity source provider
        IFlightApi.INSTANCE.addSourceProvider(entity -> {
            return List.of(ISource.Pair.of((Object) entity, (ISource) EntitySource.INSTANCE));
        });

        // Development caster for testing
        if (Services.PLATFORM.isDevelopmentEnvironment()) {
            IFlightApi.INSTANCE.addSourceCaster(value -> {
                if (value instanceof ItemStack stack) {
                    if (stack.is(Items.DIAMOND_CHESTPLATE) || stack.is(Items.SHIELD)) {
                        return List.of(() -> new DevJetpack());
                    }
                }
                return List.of();
            });
        }
    }
}

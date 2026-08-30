package net.ec.elytracomponent;

import net.ec.elytracomponent.command.ElytraComponentCommand;
import net.ec.elytracomponent.data.ElytraComponentReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod("elytra_component")
public class ElytraComponentMod {
    public static final Logger LOGGER = LogManager.getLogger(ElytraComponentMod.class);
    public static final String MODID = "elytra_component";

    public ElytraComponentMod() {
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.SPEC);

        // 注册配方序列化器
        net.ec.elytracomponent.recipe.ModRecipes.register();
        // 注册内置飞行能力
        net.ec.elytracomponent.api.ability.ElytraAbilityRegistry.register(
                net.ec.elytracomponent.ability.JumpThrustAbility.ID,
                net.ec.elytracomponent.ability.JumpThrustAbility::new);
        // 注册 jump_thrust 推进心跳（客户端 → 服务端）
        addNetworkMessage(net.ec.elytracomponent.network.ThrustMessage.class,
                net.ec.elytracomponent.network.ThrustMessage::encode,
                net.ec.elytracomponent.network.ThrustMessage::decode,
                net.ec.elytracomponent.network.ThrustMessage::handle);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    @SubscribeEvent
    public void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new ElytraComponentReloadListener());
        LOGGER.info("Registered Elytra Component reload listener");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ElytraComponentCommand.register(event.getDispatcher());
        LOGGER.info("Registered Elytra Component commands");
    }

    // ========== 网络包 ==========
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, MODID),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int messageID = 0;

    public static <T> void addNetworkMessage(Class<T> messageType,
                                              BiConsumer<T, FriendlyByteBuf> encoder,
                                              Function<FriendlyByteBuf, T> decoder,
                                              BiConsumer<T, Supplier<net.minecraftforge.network.NetworkEvent.Context>> messageConsumer) {
        PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
        messageID++;
    }

    // ========== 服务端队列 ==========
    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

    public static void queueServerWork(int tick, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
            workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
            workQueue.forEach(work -> {
                work.setValue(work.getValue() - 1);
                if (work.getValue() == 0) actions.add(work);
            });
            actions.forEach(e -> e.getKey().run());
            workQueue.removeAll(actions);
        }
    }

    // ========== Curios 兼容 ==========
}



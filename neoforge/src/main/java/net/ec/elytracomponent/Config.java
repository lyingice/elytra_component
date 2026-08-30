package net.ec.elytracomponent;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * NeoForge 配置。生成 elytra_component.toml，允许玩家自定义消耗速率。
 */
@EventBusSubscriber(modid = ElytraComponentMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.DoubleValue CONSUMPTION_RATE = BUILDER
            .comment("耐久消耗速率", "1.0 = 每 10 tick 消耗 1 点", "0.5 = 每 20 tick 消耗 1 点", "2.0 = 每 5 tick 消耗 1 点")
            .defineInRange("durability.consumption_rate", 1.0, 0.0, 100.0);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static double consumptionRate = 1.0;

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        consumptionRate = CONSUMPTION_RATE.get();
    }

    /** 当前耐久消耗速率 */
    public static double consumptionRate() {
        return consumptionRate;
    }
}

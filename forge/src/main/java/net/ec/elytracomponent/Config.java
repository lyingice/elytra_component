package net.ec.elytracomponent;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Forge 配置。生成 elytra_component.toml，允许玩家自定义消耗速率。
 */
@Mod.EventBusSubscriber(modid = ElytraComponentMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.DoubleValue CONSUMPTION_RATE = BUILDER
            .comment("耐久消耗速率", "1.0 = 每 10 tick 消耗 1 点", "0.5 = 每 20 tick 消耗 1 点", "2.0 = 每 5 tick 消耗 1 点")
            .defineInRange("durability.consumption_rate", 1.0, 0.0, 100.0);

    static final ForgeConfigSpec SPEC = BUILDER.build();

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

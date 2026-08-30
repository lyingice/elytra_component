package net.ec.elytracomponent.handler;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.data.ElytraComponentDefinition;
import net.ec.elytracomponent.data.ElytraComponentReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动扫描注册：数据包 reload 完成后扫描物品注册表，
 * 为所有"鞘翅类物品"自动生成组件定义（数据包定义优先，只补空位）。
 */
public class ElytraAutoScan {

    private static final Logger LOGGER = LogManager.getLogger(ElytraAutoScan.class);

    /** 数据包可扩展的手动补充入口：塞进该 tag 的物品也会被当作鞘翅扫描 */
    private static final TagKey<Item> AUTO_SCAN_TAG =
            TagKey.create(Registries.ITEM, new ResourceLocation(ElytraComponentMod.MODID, "auto_scan"));

    public static void scanAndRegister() {
        int registered = 0;
        List<ResourceLocation> scanned = new ArrayList<>();
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            Item item = entry.getValue();
            if (!isElytraLike(item)) continue;
            // 数据包/代码定义优先
            if (ElytraComponentReloadListener.isRegisteredElytra(item)) continue;
            // 排除鞘翅胸甲（自带鞘翅的胸甲，如 mut 的 DragonChestplateElytraItem）
            if (isChestArmor(item)) continue;

            ResourceLocation itemId = entry.getKey().location();
            ElytraComponentDefinition def = new ElytraComponentDefinition(
                    "auto/" + itemId.getNamespace() + "." + itemId.getPath(),
                    itemId,
                    null, // 纹理留空，客户端渲染时逐级探测
                    new ElytraComponentDefinition.DurabilityInfo(Math.max(0, new ItemStack(item).getMaxDamage()), 1.0F, 0),
                    new ElytraComponentDefinition.RenderInfo(null, false, null),
                    new ElytraComponentDefinition.CompatibilityInfo(null, null),
                    List.of("auto_scan"),
                    null
            );
            if (ElytraComponentReloadListener.registerAuto(def)) {
                registered++;
                scanned.add(itemId);
            }
        }
        if (registered > 0) {
            LOGGER.info("Auto-registered {} elytra component definitions from item scan: {}", registered, scanned);
        }
    }

    /** 判定是否是鞘翅类物品：原版/模组鞘翅实例，或数据包手动补充 tag */
    private static boolean isElytraLike(Item item) {
        if (item instanceof ElytraItem) return true;
        return item.builtInRegistryHolder().is(AUTO_SCAN_TAG);
    }

    /** 排除"鞘翅胸甲"：既是胸甲又是鞘翅的物品不自动注册 */
    private static boolean isChestArmor(Item item) {
        if (item instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.CHEST) return true;
        return item.builtInRegistryHolder().is(TagKey.create(Registries.ITEM,
                new ResourceLocation("minecraft", "chest_armor")));
    }

    private ElytraAutoScan() {}

    /** 供命令/调试列出自动注册的定义 */
    public static boolean isAutoDefinition(ElytraComponentDefinition def) {
        return def.tags().contains("auto_scan");
    }

    /** 测试某物品扫描后能否注册（不实际注册） */
    public static boolean wouldAutoRegister(Item item) {
        return isElytraLike(item)
                && !ElytraComponentReloadListener.isRegisteredElytra(item)
                && !isChestArmor(item);
    }
}

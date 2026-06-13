package net.ec.elytracomponent.handler;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.api.ElytraComponentAPI;
import net.ec.elytracomponent.component.ElytraComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/**
 * 胸甲 Tooltip 渲染：
 * 当胸甲有 ElytraComponent 时，在物品提示中添加组件信息。
 */
@EventBusSubscriber(modid = ElytraComponentMod.MODID, value = Dist.CLIENT)
public class ElytraTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!ElytraComponentAPI.hasComponent(stack)) return;

        ElytraComponent component = ElytraComponentAPI.getComponent(stack);
        if (component == null) return;

        List<Component> tooltip = event.getToolTip();

        // 添加空行分隔
        // tooltip.add(Component.empty());

        // 1. 已安装鞘翅组件标识
        tooltip.add(Component.translatable("tooltip.elytra_component.elytra_installed"));

        // 2. 鞘翅耐久条
        MutableComponent durabilityText = Component.translatable(
                "tooltip.elytra_component.durability",
                component.currentDurability(),
                component.maxDurability()
        );
        tooltip.add(durabilityText);

        // 3. 来源信息
        MutableComponent sourceText = Component.translatable(
                "tooltip.elytra_component.source",
                component.sourceNamespace().toString()
        );
        tooltip.add(sourceText);
    }
}

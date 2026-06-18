package net.sc.elytracomponent.handler;

import net.sc.elytracomponent.ElytraComponentMod;
import net.sc.elytracomponent.api.ElytraComponentAPI;
import net.sc.elytracomponent.api.ElytraTooltipAPI;
import net.sc.elytracomponent.api.ability.ElytraAbilityRegistry;
import net.sc.elytracomponent.api.ability.IElytraAbility;
import net.sc.elytracomponent.component.ElytraComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.List;

@EventBusSubscriber(modid = ElytraComponentMod.MODID, value = Dist.CLIENT)
public class ElytraTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!ElytraComponentAPI.hasComponent(stack)) return;

        ElytraComponent component = ElytraComponentAPI.getComponent(stack);
        if (component == null) return;

        List<Component> tooltip = event.getToolTip();

        // 检查是否有能力接管
        CompoundTag abilityConfig = component.abilityConfig();
        boolean hasAbility = abilityConfig != null && abilityConfig.contains("type");

        if (hasAbility) {
            // 有能力：让能力自己提供 tooltip
            String type = abilityConfig.getString("type");
            IElytraAbility ability = ElytraAbilityRegistry.create(type);
            if (ability != null) {
                ability.addTooltip(stack, component, tooltip);
            }
        } else {
            // 无能力：默认鞘翅组件 tooltip
            tooltip.add(Component.translatable("tooltip.elytra_component.elytra_installed"));
            tooltip.add(Component.translatable(
                    "tooltip.elytra_component.durability",
                    component.currentDurability(),
                    component.maxDurability()
            ));
        }

        // 来源信息
        tooltip.add(Component.translatable(
                "tooltip.elytra_component.source",
                component.sourceNamespace().toString()
        ));

        // API 入口
        ElytraTooltipAPI.addCustomTooltip(stack, component, tooltip);
    }
}

package net.ec.elytracomponent.handler;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.api.ElytraComponentAPI;
import net.ec.elytracomponent.api.ability.ElytraAbilityRegistry;
import net.ec.elytracomponent.api.ability.IElytraAbility;
import net.ec.elytracomponent.component.ElytraComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

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
    }
}
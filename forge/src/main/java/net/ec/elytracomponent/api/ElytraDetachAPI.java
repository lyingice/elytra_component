package net.ec.elytracomponent.api;

import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ElytraDetachAPI {

    public static boolean canDetach(ItemStack stack) {
        return ModComponents.hasComponent(stack);
    }

    public static boolean hasAbilityOverride(ElytraComponent component) {
        return component.abilityConfig() != null && component.abilityConfig().contains("type");
    }

    public static void onBeforeDetach(Player player, ItemStack stack, ElytraComponent component) {}
    public static void onAfterDetach(Player player, ItemStack result) {}
}

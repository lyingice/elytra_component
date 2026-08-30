package net.ec.elytracomponent.api;

import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.ec.elytracomponent.data.ElytraComponentDefinition;
import net.ec.elytracomponent.data.ElytraComponentReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ElytraAttachAPI {

    public static boolean canAttach(Player player, ItemStack mainHand, ItemStack offHand, ItemStack chestplate) {
        return ElytraComponentReloadListener.isRegisteredElytra(mainHand.getItem())
                && offHand.is(Items.SLIME_BALL) && !chestplate.isEmpty();
    }

    public static boolean hasExistingComponent(ItemStack chestplate) {
        ElytraComponent c = ModComponents.getComponent(chestplate);
        return c != null && c.currentDurability() > 0;
    }

    public static ElytraComponentDefinition getDefinition(ItemStack elytra) {
        return ElytraComponentReloadListener.findByItem(elytra.getItem());
    }

    public static void onBeforeAttach(Player player, ItemStack chestplate, ItemStack elytra) {}
    public static void onAfterAttach(Player player, ItemStack chestplate, ElytraComponent component) {}

    public static void consumeItem(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) stack.shrink(1);
    }
}

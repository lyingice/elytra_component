package net.ec.elytracomponent.api;

import net.ec.elytracomponent.component.ElytraComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ElytraRepairAPI {

    public static final int DEFAULT_AMOUNT = 108;

    public static boolean canRepair(ItemStack stack) { return stack.is(Items.PHANTOM_MEMBRANE); }

    public static boolean needsRepair(ElytraComponent component) {
        // maxDurability<=0 视为无耐久限制，无需修复
        return component != null && component.maxDurability() > 0
                && component.currentDurability() < component.maxDurability();
    }

    public static boolean skipDefaultRepair(ElytraComponent component) {
        CompoundTag c = component.abilityConfig();
        return c != null && c.contains("type");
    }

    public static int getRepairAmount(ElytraComponent component) {
        return Math.min(DEFAULT_AMOUNT, component.maxDurability() - component.currentDurability());
    }

    public static void onRepaired(Player player, ItemStack chestplate, ElytraComponent component) {}
}

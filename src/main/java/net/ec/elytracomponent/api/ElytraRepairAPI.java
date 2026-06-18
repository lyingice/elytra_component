// api/ElytraRepairAPI.java
package net.ec.elytracomponent.api;

import net.ec.elytracomponent.component.ElytraComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 修复相关 API。
 */
public class ElytraRepairAPI {

    public static final int DEFAULT_AMOUNT = 108;

    /** 是否可修复 */
    public static boolean canRepair(ItemStack stack) {
        return stack.is(Items.PHANTOM_MEMBRANE);
    }

    /** 是否需要修复 */
    public static boolean needsRepair(ElytraComponent component) {
        return component != null && component.currentDurability() < component.maxDurability();
    }

    /** 是否跳过默认修复（有能力接管时） */
    public static boolean skipDefaultRepair(ElytraComponent component) {
        CompoundTag c = component.abilityConfig();
        return c != null && c.contains("type");
    }

    /** 修复量 */
    public static int getRepairAmount(ElytraComponent component) {
        return Math.min(DEFAULT_AMOUNT, component.maxDurability() - component.currentDurability());
    }

    /** 执行修复后 */
    public static void onRepaired(Player player, ItemStack chestplate, ElytraComponent component) {}
}
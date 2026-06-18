// api/ElytraDetachAPI.java
package net.ec.elytracomponent.api;

import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 拆卸相关 API。
 */
public class ElytraDetachAPI {

    /** 检查是否可拆卸 */
    public static boolean canDetach(ItemStack stack) {
        return stack.has(ModComponents.ELYTRA_COMPONENT.get());
    }

    /** 是否有能力接管拆卸 */
    public static boolean hasAbilityOverride(ElytraComponent component) {
        return component.abilityConfig() != null && component.abilityConfig().contains("type");
    }

    /** 拆卸前 */
    public static void onBeforeDetach(Player player, ItemStack stack, ElytraComponent component) {}

    /** 拆卸后 */
    public static void onAfterDetach(Player player, ItemStack result) {}
}
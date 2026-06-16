package net.ec.elytracomponent.api.ability;

import net.ec.elytracomponent.component.ElytraComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IElytraAbility {
    /** 装备时触发 */
    void onEquip(Player player, ItemStack chestplate, ElytraComponent component);

    /** 卸下时触发 */
    void onUnequip(Player player, ItemStack chestplate, ElytraComponent component);

    /** 每 tick 触发，返回 true 表示能力接管了飞行逻辑 */
    boolean onTick(Player player, ItemStack chestplate, ElytraComponent component);
}

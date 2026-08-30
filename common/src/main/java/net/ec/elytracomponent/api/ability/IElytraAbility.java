package net.ec.elytracomponent.api.ability;

import net.ec.elytracomponent.component.ElytraComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 鞘翅能力接口。附属模组实现此接口以定制飞行行为。
 */
public interface IElytraAbility {
    /**
     * @return true 表示能力已处理飞行 tick，不再走默认逻辑
     */
    boolean onFlightTick(Player player, ItemStack chestStack, ElytraComponent component);
}

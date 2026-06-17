package net.ec.elytracomponent.api.ability;

import net.ec.elytracomponent.component.ElytraComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

/**
 * 鞘翅组件能力接口
 * 附属模组实现此接口来添加自定义飞行行为（背罐、喷气背包等）
 */
public interface IElytraAbility {

    /**
     * 每 tick 调用
     * @return true 表示接管了飞行逻辑，主模组跳过默认耐久消耗
     */
    boolean onFlightTick(Player player, ItemStack chestplate, ElytraComponent component);

    /**
     * 装备时触发（安装组件到胸甲后）
     */
    void onEquip(Player player, ItemStack chestplate, ElytraComponent component);

    /**
     * 卸下时触发（拆卸组件前）
     */
    void onUnequip(Player player, ItemStack chestplate, ElytraComponent component);

    /**
     * 能力唯一标识符，对应数据包 JSON 中 ability.type
     */
    String getAbilityId();
    /**
     * 添加自定义 tooltip
     */
    default void addTooltip(ItemStack stack, ElytraComponent component, List<Component> tooltip) {
        // 默认实现：显示类型名称
        tooltip.add(Component.translatable("tooltip.elytra_component.elytra_installed"));
    }
}
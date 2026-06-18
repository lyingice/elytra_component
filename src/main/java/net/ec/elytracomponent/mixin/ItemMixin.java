package net.ec.elytracomponent.mixin;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class ItemMixin implements IItemExtension {

/**
 * 检查实体是否可以使用鞘翅飞行
 * @param stack 鞘翅物品堆
 * @param entity 拥有该鞘翅的实体
 * @return 如果鞘翅存在且耐久度大于0则返回true，表示可以飞行；否则返回false
 */
@Override
public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
    // 获取物品堆中的鞘翅组件
    ElytraComponent component = stack.get(ModComponents.ELYTRA_COMPONENT.get());
    // 检查组件是否存在且当前耐久度大于0
    if (component != null && component.currentDurability() > 0) {
        // 获取能力配置
        CompoundTag config = component.abilityConfig();
        // 如果配置存在且包含"type"键，表示有其他能力接管，不能使用鞘翅飞行
        if (config != null && config.contains("type")) {
            return false; // 有能力接管，不走鞘翅飞行
        }
        // 否则可以使用鞘翅飞行
        return true;
    }
    // 鞘翅不存在或耐久度小于等于0，不能飞行
    return false;
}

/**
 * 重写 elytraFlightTick 方法，用于实现鞘翅的特殊飞行逻辑
 * @param stack 鞘翅物品堆栈
 * @param entity 实体LivingEntity
 * @param flightTicks 已飞行 ticks 数
 * @return boolean 是否允许继续飞行
 */
    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
    // 获取鞘翅组件
        ElytraComponent component = stack.get(ModComponents.ELYTRA_COMPONENT.get());
    // 检查组件是否存在且当前耐久度大于0
        if (component != null && component.currentDurability() > 0) {
            // 只维持飞行，不消耗耐久（耐久由 ElytraFlightHandler 统一处理）
            return true;
        }
        return false;
    }
}
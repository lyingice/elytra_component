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
 * @return maxDurability<=0 视为无耐久限制，直接可飞；否则要求当前耐久>0
 */
@Override
public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
    ElytraComponent component = stack.get(ModComponents.ELYTRA_COMPONENT.get());
    if (component == null) return false;
    return component.maxDurability() <= 0 || component.currentDurability() > 0;
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
        ElytraComponent component = stack.get(ModComponents.ELYTRA_COMPONENT.get());
        if (component == null) return false;
        // 只维持飞行，不消耗耐久（耐久由 ElytraFlightHandler/能力统一处理）
        return component.maxDurability() <= 0 || component.currentDurability() > 0;
    }
}
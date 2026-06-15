package net.sc.elytracomponent.mixin;

import net.sc.elytracomponent.ElytraComponentMod;
import net.sc.elytracomponent.component.ElytraComponent;
import net.sc.elytracomponent.component.ModComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class ItemMixin implements IForgeItem {

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        // 检查是否有我们的组件
        ElytraComponent component = ModComponents.getComponent(stack);
        if (component != null && component.currentDurability() > 0) {
            return true;
        }
        // 没有组件时，返回 false，让原版鞘翅类自己处理
        return false;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        ElytraComponent component = ModComponents.getComponent(stack);
        if (component != null && component.currentDurability() > 0) {
            // 只维持飞行，不消耗耐久（耐久由 ElytraFlightHandler 统一处理）
            return true;
        }
        return false;
    }
}

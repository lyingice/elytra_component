package net.sc.elytracomponent.component;

import net.sc.elytracomponent.ElytraComponentMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * 使用 NBT 在 ItemStack 上存储组件数据。
 * 1.20.1 没有 DataComponentType 系统，因此通过 NBT keys 模拟。
 */
public class ModComponents {

    /** NBT key：存储在胸甲上的鞘翅组件数据 */
    public static final String KEY_ELYTRA_COMPONENT = ElytraComponentMod.MODID + ":elytra_component";

    /** NBT key：标记当前胸甲是否可以飞行 */
    public static final String KEY_CAN_ELYTRA_FLY = ElytraComponentMod.MODID + ":can_elytra_fly";

    // ==================== ELYTRA_COMPONENT ====================

    /**
     * 从 ItemStack 获取 ElytraComponent
     */
    @Nullable
    public static ElytraComponent getComponent(ItemStack stack) {
        if (stack.isEmpty()) return null;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(KEY_ELYTRA_COMPONENT)) return null;
        return ElytraComponent.fromNBT(tag.getCompound(KEY_ELYTRA_COMPONENT));
    }

    /**
     * 检查 ItemStack 是否有 ElytraComponent
     */
    public static boolean hasComponent(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(KEY_ELYTRA_COMPONENT);
    }

    /**
     * 设置 ElytraComponent 到 ItemStack
     */
    public static void setComponent(ItemStack stack, ElytraComponent component) {
        stack.getOrCreateTag().put(KEY_ELYTRA_COMPONENT, component.toNBT());
    }

    /**
     * 移除 ElytraComponent
     */
    public static void removeComponent(ItemStack stack) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(KEY_ELYTRA_COMPONENT);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }

    // ==================== CAN_ELYTRA_FLY ====================

    public static boolean canFly(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(KEY_CAN_ELYTRA_FLY) && tag.getBoolean(KEY_CAN_ELYTRA_FLY);
    }

    public static void setCanFly(ItemStack stack, boolean canFly) {
        stack.getOrCreateTag().putBoolean(KEY_CAN_ELYTRA_FLY, canFly);
    }

    public static void removeCanFly(ItemStack stack) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(KEY_CAN_ELYTRA_FLY);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }
}

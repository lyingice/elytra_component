package net.ec.elytracomponent.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Forge 1.20.1 平台：使用 NBT 在 ItemStack 上存储组件数据。
 */
public class ModComponents {

    public static final String KEY_ELYTRA_COMPONENT = "elytra_component:elytra_component";
    public static final String KEY_CAN_ELYTRA_FLY = "elytra_component:can_elytra_fly";

    @Nullable
    public static ElytraComponent getComponent(ItemStack stack) {
        if (stack.isEmpty()) return null;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(KEY_ELYTRA_COMPONENT)) return null;
        return ElytraComponentNbtHelper.fromNBT(tag.getCompound(KEY_ELYTRA_COMPONENT));
    }

    public static boolean hasComponent(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(KEY_ELYTRA_COMPONENT);
    }

    public static void setComponent(ItemStack stack, ElytraComponent component) {
        stack.getOrCreateTag().put(KEY_ELYTRA_COMPONENT, ElytraComponentNbtHelper.toNBT(component));
    }

    public static void removeComponent(ItemStack stack) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(KEY_ELYTRA_COMPONENT);
            if (tag.isEmpty()) stack.setTag(null);
        }
    }

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
            if (tag.isEmpty()) stack.setTag(null);
        }
    }
}

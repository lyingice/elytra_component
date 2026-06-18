package net.sc.elytracomponent.api;

import net.sc.elytracomponent.component.ElytraComponent;
import net.sc.elytracomponent.component.ModComponents;
import net.sc.elytracomponent.data.ElytraComponentDefinition;
import net.sc.elytracomponent.data.ElytraComponentReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;

public class ElytraComponentAPI {

    public static void register(String componentId, ElytraComponentDefinition def) {
        ElytraComponentReloadListener.registerDirectly(componentId, def);
    }

    public static boolean isRegisteredElytra(ItemStack stack) {
        return ElytraComponentReloadListener.isRegisteredElytra(stack.getItem());
    }

    @Nullable
    public static ElytraComponent getComponent(ItemStack chestplate) {
        return ModComponents.getComponent(chestplate);
    }

    public static boolean hasComponent(ItemStack chestplate) {
        return ModComponents.hasComponent(chestplate);
    }

    public static ElytraComponent createComponent(ItemStack elytraStack, ElytraComponentDefinition def,
                                                  @Nullable CompoundTag originalChestAttrs) {
        int maxDurability = def.durability().calculateDurability(elytraStack.getMaxDamage());
        int currentDurability = maxDurability - elytraStack.getDamageValue();
        if (currentDurability < 0) currentDurability = 0;

        ResourceLocation textureOverride = null;
        if (def.texture() != null && def.texture().elytraLayer() != null) {
            textureOverride = def.texture().elytraLayer();
        }

        CompoundTag elytraTag = elytraStack.getTag();

        return new ElytraComponent(
                def.getSourceNamespace(),
                def.elytraItem(),
                null,                    // 不保存原始 NBT
                currentDurability,
                maxDurability,
                textureOverride,
                null,                    // extraData
                originalChestAttrs,      // 原始胸甲属性
                null,                    // abilityConfig（未来扩展）
                null                     // particleConfig（未来扩展）
        );
    }

    public static ElytraComponent createComponent(ItemStack elytraStack, ElytraComponentDefinition def) {
        return createComponent(elytraStack, def, null);
    }

    public static ItemStack restoreElytra(ElytraComponent component) {
        var item = BuiltInRegistries.ITEM.get(component.originalElytraId());
        if (item == Items.AIR) return ItemStack.EMPTY;

        ItemStack elytra = new ItemStack(item, 1);

        if (elytra.isDamageableItem()) {
            int maxDamage = elytra.getMaxDamage();
            float ratio = (float) component.currentDurability() / component.maxDurability();
            int damage = maxDamage - Math.round(maxDamage * ratio);
            elytra.setDamageValue(Math.max(0, Math.min(damage, maxDamage)));
        }

        return elytra;
    }

    public static void setComponent(ItemStack chestplate, ElytraComponent component) {
        ModComponents.setComponent(chestplate, component);
        ModComponents.setCanFly(chestplate, true);
    }

    public static void removeComponent(ItemStack chestplate) {
        ModComponents.removeComponent(chestplate);
        ModComponents.removeCanFly(chestplate);
    }
}

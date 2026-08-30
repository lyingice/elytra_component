package net.ec.elytracomponent.api;

import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.ec.elytracomponent.data.ElytraComponentDefinition;
import net.ec.elytracomponent.data.ElytraComponentReloadListener;
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

        return new ElytraComponent(new ElytraComponent.SourceInfo(def.getSourceNamespace(), def.elytraItem(), elytraStack.getTag()), // 保存原始 NBT（含附魔、属性等）
                currentDurability, maxDurability, textureOverride, null, originalChestAttrs,
                buildAbilityConfig(def), null);
    }

    /**
     * 把数据包定义的 abilities 打包为组件 abilityConfig：
     * 顶层 "type" 取第一个能力（兼容旧消费方），完整列表存 "abilities" 子标签。
     */
    @Nullable
    private static CompoundTag buildAbilityConfig(ElytraComponentDefinition def) {
        if (def.abilities().isEmpty()) return null;
        CompoundTag tag = def.abilities().get(0).copy();
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        list.addAll(def.abilities());
        tag.put("abilities", list);
        return tag;
    }

    public static ElytraComponent createComponent(ItemStack elytraStack, ElytraComponentDefinition def) {
        return createComponent(elytraStack, def, null);
    }

    public static ItemStack restoreElytra(ElytraComponent component) {
        var item = BuiltInRegistries.ITEM.get(component.source().originalElytraId());
        if (item == Items.AIR) return ItemStack.EMPTY;

        ItemStack elytra = new ItemStack(item, 1);

        // 还原原始 NBT（附魔、名称等）
        if (component.source().originalElytraTag() != null) {
            elytra.setTag(component.source().originalElytraTag().copy());
        }

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


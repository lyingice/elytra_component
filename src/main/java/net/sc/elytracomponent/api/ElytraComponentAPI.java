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

/**
 * 公共 API，供其他模组调用。
 * 提供注册、查询、创建/还原鞘翅组件的静态方法。
 */
public class ElytraComponentAPI {

    /**
     * 代码注册鞘翅组件定义（优先级高于数据包 JSON）
     */
    public static void register(String componentId, ElytraComponentDefinition def) {
        ElytraComponentReloadListener.registerDirectly(componentId, def);
    }

    /**
     * 检查某个物品是否是已注册的鞘翅组件源
     */
    public static boolean isRegisteredElytra(ItemStack stack) {
        return ElytraComponentReloadListener.isRegisteredElytra(stack.getItem());
    }

    /**
     * 获取某个胸甲当前安装的鞘翅组件
     */
    @Nullable
    public static ElytraComponent getComponent(ItemStack chestplate) {
        return ModComponents.getComponent(chestplate);
    }

    /**
     * 检查胸甲是否已安装鞘翅组件
     */
    public static boolean hasComponent(ItemStack chestplate) {
        return ModComponents.hasComponent(chestplate);
    }

    /**
     * 从鞘翅物品和定义创建组件实例
     *
     * @param elytraStack        鞘翅物品
     * @param def                组件定义
     * @param originalChestAttrs 原始胸甲属性（安装时保存），可为 null
     */
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
                elytraTag,
                currentDurability,
                maxDurability,
                textureOverride,
                null,                    // extraData
                originalChestAttrs,      // 原始胸甲属性
                null,                    // abilityConfig（未来扩展）
                null                     // particleConfig（未来扩展）
        );
    }

    /**
     * 从鞘翅物品和定义创建组件实例（无原始属性）
     */
    public static ElytraComponent createComponent(ItemStack elytraStack, ElytraComponentDefinition def) {
        return createComponent(elytraStack, def, null);
    }

    /**
     * 从组件还原鞘翅物品（用于剪刀拆卸）
     */
    public static ItemStack restoreElytra(ElytraComponent component) {
        var item = BuiltInRegistries.ITEM.get(component.originalElytraId());

        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        ItemStack elytra = new ItemStack(item, 1);

        if (component.originalElytraTag() != null) {
            elytra.setTag(component.originalElytraTag().copy());
        }

        if (elytra.isDamageableItem()) {
            int maxDamage = elytra.getMaxDamage();
            float ratio = (float) component.currentDurability() / component.maxDurability();
            int damage = maxDamage - Math.round(maxDamage * ratio);
            elytra.setDamageValue(Math.max(0, Math.min(damage, maxDamage)));
        }

        return elytra;
    }

    /**
     * 设置胸甲的鞘翅组件
     */
    public static void setComponent(ItemStack chestplate, ElytraComponent component) {
        ModComponents.setComponent(chestplate, component);
        ModComponents.setCanFly(chestplate, true);
    }

    /**
     * 移除胸甲的鞘翅组件
     */
    public static void removeComponent(ItemStack chestplate) {
        ModComponents.removeComponent(chestplate);
        ModComponents.removeCanFly(chestplate);
    }
}
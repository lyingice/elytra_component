package net.ec.elytracomponent.api;

import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.ec.elytracomponent.data.ElytraComponentDefinition;
import net.ec.elytracomponent.data.ElytraComponentReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
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
     *
     * @param componentId 唯一标识符
     * @param def         组件定义
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
        return chestplate.get(ModComponents.ELYTRA_COMPONENT.get());
    }

    /**
     * 检查胸甲是否已安装鞘翅组件
     */
    public static boolean hasComponent(ItemStack chestplate) {
        return chestplate.has(ModComponents.ELYTRA_COMPONENT.get());
    }

    /**
     * 从鞘翅物品和定义创建组件实例（在锻造配方中使用）
     *
     * @param elytraStack 鞘翅物品（包含耐久、附魔等数据）
     * @param def         组件定义
     * @return 组件实例
     */
    public static ElytraComponent createComponent(ItemStack elytraStack, ElytraComponentDefinition def) {
        int maxDurability = def.durability().calculateDurability(elytraStack.getMaxDamage());
        int currentDurability = maxDurability - elytraStack.getDamageValue();
        if (currentDurability < 0) currentDurability = 0;

        // 纹理路径：优先使用定义中指定的，否则从原物品自动推算
        ResourceLocation textureOverride = null;
        if (def.texture() != null && def.texture().elytraLayer() != null) {
            textureOverride = def.texture().elytraLayer();
        }

        return new ElytraComponent(
                def.getSourceNamespace(),
                def.elytraItem(),
                elytraStack.getComponents(),
                currentDurability,
                maxDurability,
                textureOverride,
                null
        );
    }

    /**
     * 从组件还原鞘翅物品（用于剪刀拆卸）
     *
     * @param component 组件实例
     * @return 还原的鞘翅 ItemStack
     */
    public static ItemStack restoreElytra(ElytraComponent component) {
        var item = BuiltInRegistries.ITEM.get(component.originalElytraId());

        // 如果物品不存在（模组未加载），返回空气
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        ItemStack elytra = new ItemStack(item, 1);
        elytra.applyComponents(component.originalElytraComponents());

        // 按组件耐久比例设置还原鞘翅的耐久
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
        chestplate.set(ModComponents.ELYTRA_COMPONENT.get(), component);
        chestplate.set(ModComponents.CAN_ELYTRA_FLY.get(), true);
    }

    /**
     * 移除胸甲的鞘翅组件
     */
    public static void removeComponent(ItemStack chestplate) {
        chestplate.remove(ModComponents.ELYTRA_COMPONENT.get());
        chestplate.remove(ModComponents.CAN_ELYTRA_FLY.get());
    }
}

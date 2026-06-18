// api/ElytraAttachAPI.java
package net.sc.elytracomponent.api;

import net.sc.elytracomponent.component.ElytraComponent;
import net.sc.elytracomponent.component.ModComponents;
import net.sc.elytracomponent.data.ElytraComponentDefinition;
import net.sc.elytracomponent.data.ElytraComponentReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 安装相关 API。
 * 附属模组覆盖这些方法来接管安装逻辑。
 */
public class ElytraAttachAPI {

    // ==================== 可覆盖 ====================

    /** 检查是否能安装 */
    public static boolean canAttach(Player player, ItemStack mainHand, ItemStack offHand, ItemStack chestplate) {
        return ElytraComponentReloadListener.isRegisteredElytra(mainHand.getItem())
                && offHand.is(Items.SLIME_BALL)
                && !chestplate.isEmpty();
    }

    /** 检查胸甲是否已有组件 */
    public static boolean hasExistingComponent(ItemStack chestplate) {
        ElytraComponent c = ModComponents.getComponent(chestplate);
        return c != null && c.currentDurability() > 0;
    }

    /** 获取定义 */
    public static ElytraComponentDefinition getDefinition(ItemStack elytra) {
        return ElytraComponentReloadListener.findByItem(elytra.getItem());
    }

    /** 安装前 */
    public static void onBeforeAttach(Player player, ItemStack chestplate, ItemStack elytra) {}

    /** 安装后 */
    public static void onAfterAttach(Player player, ItemStack chestplate, ElytraComponent component) {}

    // ==================== 不可覆盖（工具方法） ====================

    public static void consumeItem(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) stack.shrink(1);
    }
}

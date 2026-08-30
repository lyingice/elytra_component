package net.ec.elytracomponent.recipe;

import net.ec.elytracomponent.api.ElytraComponentAPI;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.ec.elytracomponent.handler.ElytraDetachHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * 拆卸配方（工作台）：带鞘翅组件的胸甲 → 恢复的原鞘翅 + 还原的胸甲。
 * 胸甲通过 getRemainingItems 返回（合成台单结果限制）。
 * 数据包 JSON：{ "type": "elytra_component:detach", "category": "misc" }
 */
public class DetachElytraRecipe extends CustomRecipe {

    public DetachElytraRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int filled = 0;
        boolean hasComponent = false;
        for (ItemStack stack : container.getItems()) {
            if (stack.isEmpty()) continue;
            filled++;
            if (filled > 1) return false;
            if (ModComponents.hasComponent(stack)) {
                hasComponent = true;
            }
        }
        return filled == 1 && hasComponent;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        for (ItemStack stack : container.getItems()) {
            if (stack.isEmpty()) continue;
            ElytraComponent component = ModComponents.getComponent(stack);
            if (component != null) {
                return ElytraComponentAPI.restoreElytra(component);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            ElytraComponent component = ModComponents.getComponent(stack);
            if (component != null) {
                // 还原胸甲：恢复原始属性并移除组件
                ItemStack chestplate = stack.copy();
                chestplate.setCount(1);
                ElytraDetachHandler.restoreOriginalAttributes(chestplate, component);
                ElytraComponentAPI.removeComponent(chestplate);
                remaining.set(i, chestplate);
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.DETACH_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }
}

package net.ec.elytracomponent.recipe;

import net.ec.elytracomponent.api.ElytraComponentAPI;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.ec.elytracomponent.handler.ElytraDetachHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * 拆卸配方（工作台）：带鞘翅组件的胸甲 → 恢复的原鞘翅 + 还原的胸甲。
 * 胸甲通过 getRemainingItems 返回（合成台单结果限制）。
 * 数据包 JSON：{ "type": "elytra_component:detach", "category": "misc" }
 */
public class DetachElytraRecipe implements CraftingRecipe {

    private final CraftingBookCategory category;

    public DetachElytraRecipe(CraftingBookCategory category) {
        this.category = category;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int filled = 0;
        boolean hasComponent = false;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            filled++;
            if (filled > 1) return false;
            if (stack.has(ModComponents.ELYTRA_COMPONENT.get())) {
                hasComponent = true;
            }
        }
        return filled == 1 && hasComponent;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            ElytraComponent component = stack.get(ModComponents.ELYTRA_COMPONENT.get());
            if (component != null) {
                return ElytraComponentAPI.restoreElytra(component);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            ElytraComponent component = stack.get(ModComponents.ELYTRA_COMPONENT.get());
            if (component != null) {
                // 还原胸甲：恢复原始属性并移除组件
                ItemStack chestplate = stack.copyWithCount(1);
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
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(Items.ELYTRA);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.DETACH_SERIALIZER.get();
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }
}

package net.ec.elytracomponent.recipe;

import com.google.gson.JsonObject;
import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.api.ElytraComponentAPI;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.ec.elytracomponent.data.ElytraComponentReloadListener;
import net.ec.elytracomponent.handler.ElytraAttachHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * 安装配方（工作台）：兼容胸甲 + 已注册鞘翅 + 触媒（默认粘液球，JSON 可改）
 * → 带鞘翅组件的胸甲。与右键安装共用组件构建逻辑（ElytraComponentAPI.createComponent）。
 * 数据包 JSON 示例：
 * { "type": "elytra_component:attach", "category": "equipment",
 *   "catalyst": {"item": "minecraft:slime_ball"} }
 */
public class AttachElytraRecipe extends CustomRecipe {

    public static final TagKey<net.minecraft.world.item.Item> CHESTPLATES_TAG =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, new ResourceLocation(ElytraComponentMod.MODID, "elytra_compatible_chestplates"));

    private final Ingredient catalyst;

    public AttachElytraRecipe(ResourceLocation id, CraftingBookCategory category, Ingredient catalyst) {
        super(id, category);
        this.catalyst = catalyst;
    }

    public Ingredient getCatalyst() {
        return catalyst;
    }

    /** 兼容胸甲判定：所有盔甲类胸甲，或数据包 tag 手动补充的物品 */
    public static boolean isCompatibleChestplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.CHEST) {
            return true;
        }
        return stack.is(CHESTPLATES_TAG);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean hasChest = false, hasElytra = false, hasCatalyst = false;
        for (ItemStack stack : container.getItems()) {
            if (stack.isEmpty()) continue;
            if (isCompatibleChestplate(stack)) {
                if (hasChest) return false;
                // 胸甲上已有完好的组件时不可再安装
                ElytraComponent existing = ModComponents.getComponent(stack);
                if (existing != null && existing.maxDurability() > 0 && existing.currentDurability() > 0) {
                    return false;
                }
                hasChest = true;
            } else if (ElytraComponentReloadListener.isRegisteredElytra(stack.getItem())) {
                if (hasElytra) return false;
                hasElytra = true;
            } else if (catalyst.test(stack)) {
                if (hasCatalyst) return false;
                hasCatalyst = true;
            } else {
                return false;
            }
        }
        return hasChest && hasElytra && hasCatalyst;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        ItemStack chestplate = ItemStack.EMPTY, elytra = ItemStack.EMPTY;
        for (ItemStack stack : container.getItems()) {
            if (stack.isEmpty()) continue;
            if (isCompatibleChestplate(stack)) chestplate = stack;
            else if (ElytraComponentReloadListener.isRegisteredElytra(stack.getItem())) elytra = stack;
        }
        if (chestplate.isEmpty() || elytra.isEmpty()) return ItemStack.EMPTY;

        var def = ElytraComponentReloadListener.findByItem(elytra.getItem());
        if (def == null) return ItemStack.EMPTY;

        ItemStack result = chestplate.copy();
        result.setCount(1);

        // 保存胸甲原始属性，再合并鞘翅属性（与右键安装一致）
        CompoundTag originalAttrs = ElytraAttachHandler.saveForgeAttributes(result, elytra);
        ElytraComponent component = ElytraComponentAPI.createComponent(elytra, def, originalAttrs);
        ElytraComponentAPI.setComponent(result, component);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ATTACH_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        // 供 JEI/配方书展示
        return NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(CHESTPLATES_TAG),
                Ingredient.of(TagKey.create(net.minecraft.core.registries.Registries.ITEM, new ResourceLocation(ElytraComponentMod.MODID, "elytra_components"))),
                catalyst);
    }

    // ==================== 序列化器（支持自定义触媒） ====================

    public static class Serializer implements RecipeSerializer<AttachElytraRecipe> {
        @Override
        public AttachElytraRecipe fromJson(ResourceLocation id, JsonObject json) {
            CraftingBookCategory category;
            try {
                category = CraftingBookCategory.valueOf(
                        GsonHelper.getAsString(json, "category", "misc").toUpperCase());
            } catch (IllegalArgumentException ex) {
                category = CraftingBookCategory.MISC;
            }
            Ingredient catalyst = json.has("catalyst")
                    ? Ingredient.fromJson(json.get("catalyst"))
                    : Ingredient.of(Items.SLIME_BALL);
            return new AttachElytraRecipe(id, category, catalyst);
        }

        @Override
        public AttachElytraRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
            Ingredient catalyst = Ingredient.fromNetwork(buf);
            return new AttachElytraRecipe(id, category, catalyst);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, AttachElytraRecipe recipe) {
            buf.writeEnum(recipe.category());
            recipe.catalyst.toNetwork(buf);
        }
    }
}

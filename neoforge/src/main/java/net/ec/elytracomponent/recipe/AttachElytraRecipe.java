package net.ec.elytracomponent.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.api.ElytraComponentAPI;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.ec.elytracomponent.data.ElytraComponentReloadListener;
import net.ec.elytracomponent.handler.ElytraAttachHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * 安装配方（工作台）：兼容胸甲 + 已注册鞘翅 + 触媒（默认粘液球，JSON 可改）
 * → 带鞘翅组件的胸甲。与右键安装共用组件构建逻辑（ElytraComponentAPI.createComponent）。
 * 数据包 JSON 示例：
 * { "type": "elytra_component:attach", "catalyst": {"item": "minecraft:slime_ball"} }
 */
public class AttachElytraRecipe implements CraftingRecipe {

    public static final TagKey<net.minecraft.world.item.Item> CHESTPLATES_TAG =
            TagKey.create(Registries.ITEM, ElytraComponentMod.id("elytra_compatible_chestplates"));

    private final CraftingBookCategory category;
    private final Ingredient catalyst;

    public AttachElytraRecipe(CraftingBookCategory category, Ingredient catalyst) {
        this.category = category;
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
    public boolean matches(CraftingInput input, Level level) {
        boolean hasChest = false, hasElytra = false, hasCatalyst = false;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            if (isCompatibleChestplate(stack)) {
                if (hasChest) return false;
                // 胸甲上已有完好的组件时不可再安装
                ElytraComponent existing = stack.get(ModComponents.ELYTRA_COMPONENT.get());
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
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack chestplate = ItemStack.EMPTY, elytra = ItemStack.EMPTY;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            if (isCompatibleChestplate(stack)) chestplate = stack;
            else if (ElytraComponentReloadListener.isRegisteredElytra(stack.getItem())) elytra = stack;
        }
        if (chestplate.isEmpty() || elytra.isEmpty()) return ItemStack.EMPTY;

        var def = ElytraComponentReloadListener.findByItem(elytra.getItem());
        if (def == null) return ItemStack.EMPTY;

        ItemStack result = chestplate.copyWithCount(1);

        // 保存胸甲原始属性，再合并鞘翅属性（与右键安装一致）
        CompoundTag originalAttrs = ElytraAttachHandler.serializeModifiers(result.getAttributeModifiers());
        ItemAttributeModifiers elytraModifiers = elytra.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (elytraModifiers != null && !elytraModifiers.modifiers().isEmpty()) {
            List<ItemAttributeModifiers.Entry> merged = new ArrayList<>(result.getAttributeModifiers().modifiers());
            merged.addAll(elytraModifiers.modifiers());
            result.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(merged, true));
        }

        ElytraComponent component = ElytraComponentAPI.createComponent(elytra, def, originalAttrs);
        ElytraComponentAPI.setComponent(result, component);
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        // 供 JEI/配方书展示
        return NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(CHESTPLATES_TAG),
                Ingredient.of(TagKey.create(Registries.ITEM, ElytraComponentMod.id("elytra_components"))),
                catalyst);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(Items.IRON_CHESTPLATE);
    }

    @Override
    public boolean isSpecial() {
        return true;
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
    public CraftingBookCategory category() {
        return category;
    }

    // ==================== 序列化器（支持自定义触媒） ====================

    public static class Serializer implements RecipeSerializer<AttachElytraRecipe> {
        private static final MapCodec<AttachElytraRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC)
                                .forGetter(r -> r.category),
                        Ingredient.CODEC.optionalFieldOf("catalyst", Ingredient.of(Items.SLIME_BALL))
                                .forGetter(r -> r.catalyst)
                ).apply(instance, AttachElytraRecipe::new));

        private static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, AttachElytraRecipe> STREAM_CODEC =
                net.minecraft.network.codec.StreamCodec.composite(
                        CraftingBookCategory.STREAM_CODEC, r -> r.category,
                        Ingredient.CONTENTS_STREAM_CODEC, r -> r.catalyst,
                        AttachElytraRecipe::new);

        @Override
        public MapCodec<AttachElytraRecipe> codec() {
            return CODEC;
        }

        @Override
        public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, AttachElytraRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

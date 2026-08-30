package net.ec.elytracomponent.recipe;

import net.ec.elytracomponent.ElytraComponentMod;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 配方序列化器注册。
 * attach：自定义序列化器（数据包可改触媒 catalyst）
 * detach：简单序列化器
 */
public class ModRecipes {

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ElytraComponentMod.MODID);


    public static final net.minecraftforge.registries.RegistryObject<RecipeSerializer<AttachElytraRecipe>> ATTACH_SERIALIZER =
            SERIALIZERS.register("attach", AttachElytraRecipe.Serializer::new);

    public static final net.minecraftforge.registries.RegistryObject<RecipeSerializer<DetachElytraRecipe>> DETACH_SERIALIZER =
            SERIALIZERS.register("detach", () -> new SimpleCraftingRecipeSerializer<>(DetachElytraRecipe::new));

    public static void register() {
        SERIALIZERS.register(MinecraftForge.EVENT_BUS);
    }
}

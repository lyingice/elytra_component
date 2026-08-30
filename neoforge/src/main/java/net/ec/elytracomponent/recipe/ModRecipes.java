package net.ec.elytracomponent.recipe;

import net.ec.elytracomponent.ElytraComponentMod;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 配方序列化器注册。
 * attach：自定义序列化器（数据包可改触媒 catalyst）
 * detach：简单序列化器
 */
public class ModRecipes {

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.RECIPE_SERIALIZER, ElytraComponentMod.MODID);

    public static final Supplier<RecipeSerializer<AttachElytraRecipe>> ATTACH_SERIALIZER =
            SERIALIZERS.register("attach", AttachElytraRecipe.Serializer::new);

    public static final Supplier<RecipeSerializer<DetachElytraRecipe>> DETACH_SERIALIZER =
            SERIALIZERS.register("detach", () -> new SimpleCraftingRecipeSerializer<>(DetachElytraRecipe::new));

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
    }

    private ModRecipes() {}
}

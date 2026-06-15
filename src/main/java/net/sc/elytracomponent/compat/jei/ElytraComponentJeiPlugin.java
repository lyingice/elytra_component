package net.sc.elytracomponent.compat.jei;

import net.sc.elytracomponent.ElytraComponentMod;
import net.sc.elytracomponent.data.ElytraComponentDefinition;
import net.sc.elytracomponent.data.ElytraComponentReloadListener;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class ElytraComponentJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID =
            new ResourceLocation(ElytraComponentMod.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<ElytraComponentDefinition> allDefs = new ArrayList<>(ElytraComponentReloadListener.getAll());

        if (allDefs.isEmpty()) {
            ElytraComponentMod.LOGGER.info("No elytra components registered, skipping JEI info registration");
            return;
        }

        // 过滤：只显示已加载模组的物品 + 物品实际存在
        List<ItemStack> elytraItems = new ArrayList<>();
        List<ElytraComponentDefinition> validDefs = new ArrayList<>();

        for (ElytraComponentDefinition def : allDefs) {
            // 检查 required_mods
            boolean modsLoaded = true;
            for (String modId : def.compatibility().requiredMods()) {
                if (!ModList.get().isLoaded(modId)) {
                    modsLoaded = false;
                    break;
                }
            }
            if (!modsLoaded) continue;

            // 检查物品是否在注册表中存在
            var item = BuiltInRegistries.ITEM.get(def.elytraItem());
            if (item == Items.AIR) continue;

            elytraItems.add(new ItemStack(item));
            validDefs.add(def);
        }

        if (elytraItems.isEmpty()) {
            ElytraComponentMod.LOGGER.info("No valid elytra components for currently loaded mods");
            return;
        }

        Component description = buildDescription(validDefs);
        Component shortTitle = Component.translatable(
                "jei.elytra_component.info.title", validDefs.size());

        registration.addItemStackInfo(elytraItems, shortTitle, description);

        ElytraComponentMod.LOGGER.info("Registered {} elytra components in JEI info display", validDefs.size());
    }

    private static Component buildDescription(List<ElytraComponentDefinition> defs) {
        Component root = Component.literal("");

        root = root.copy().append(Component.translatable("jei.elytra_component.info.header",
                defs.size()).withStyle(ChatFormatting.GOLD));
        root = root.copy().append(Component.literal("\n\n"));

        for (int i = 0; i < defs.size(); i++) {
            ElytraComponentDefinition def = defs.get(i);

            if (i > 0) {
                root = root.copy().append(Component.literal("\n"));
            }

            root = root.copy().append(Component.literal("■ ")
                    .withStyle(ChatFormatting.YELLOW));
            root = root.copy().append(Component.literal(def.componentId())
                    .withStyle(ChatFormatting.WHITE));

            root = root.copy().append(Component.literal("\n  §7物品: §f" + def.elytraItem()));

            StringBuilder durabilitySb = new StringBuilder();
            durabilitySb.append("\n  §7耐久: §e").append(def.durability().base());
            if (def.durability().multiplier() != 1.0f) {
                durabilitySb.append(" §7x§e").append(def.durability().multiplier());
            }
            if (def.durability().maxDurability() < Integer.MAX_VALUE) {
                durabilitySb.append(" §7(max: §e").append(def.durability().maxDurability()).append("§7)");
            }
            root = root.copy().append(Component.literal(durabilitySb.toString()));

            if (def.texture() != null && def.texture().elytraLayer() != null) {
                root = root.copy().append(Component.literal("\n  §7纹理: §f" + def.texture().elytraLayer()));
            }

            if (!def.tags().isEmpty()) {
                root = root.copy().append(Component.literal("\n  §7标签: §b" + String.join("§7, §b", def.tags())));
            }

            if (!def.compatibility().requiredMods().isEmpty()) {
                root = root.copy().append(Component.literal("\n  §7依赖: §d" + String.join("§7, §d", def.compatibility().requiredMods())));
            }
        }

        root = root.copy().append(Component.literal("\n\n"));
        root = root.copy().append(Component.translatable("jei.elytra_component.info.footer")
                .withStyle(ChatFormatting.BLUE));

        return root;
    }
}
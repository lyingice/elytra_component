package net.ec.elytracomponent.compat.jei;

import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.data.ElytraComponentDefinition;
import net.ec.elytracomponent.data.ElytraComponentReloadListener;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI 兼容插件。
 *
 * 在 JEI 的信息显示中，展示所有数据包配置的鞘翅组件列表。
 * 玩家可以在 JEI 中搜索 "Elytra Component" 或对应鞘翅物品，
 * 查看哪些鞘翅可以安装到胸甲上，以及各自的耐久信息。
 *
 * 使用 addItemStackInfo(List<ItemStack>, Component...) 
 * 自动处理多物品轮播显示。
 */
@JeiPlugin
public class ElytraComponentJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID =
            ResourceLocation.fromNamespaceAndPath(ElytraComponentMod.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // 获取所有已注册的鞘翅组件定义
        List<ElytraComponentDefinition> allDefs = new ArrayList<>(ElytraComponentReloadListener.getAll());

        if (allDefs.isEmpty()) {
            ElytraComponentMod.LOGGER.info("No elytra components registered, skipping JEI info registration");
            return;
        }

        // 收集所有鞘翅物品
        List<ItemStack> elytraItems = new ArrayList<>();
        for (ElytraComponentDefinition def : allDefs) {
            var item = BuiltInRegistries.ITEM.get(def.elytraItem());
            if (item != Items.AIR) {
                elytraItems.add(new ItemStack(item));
            }
        }

        if (elytraItems.isEmpty()) {
            ElytraComponentMod.LOGGER.warn("All registered elytra components have invalid items");
            return;
        }

        // 构建描述文本
        Component description = buildDescription(allDefs);
        Component shortTitle = Component.translatable(
                "jei.elytra_component.info.title",
                allDefs.size()
        );

        // 注册信息 — 使用 addItemStackInfo 自动处理轮播
        registration.addItemStackInfo(elytraItems, shortTitle, description);

        ElytraComponentMod.LOGGER.info("Registered {} elytra components in JEI info display", allDefs.size());
    }

    /**
     * 构建完整的鞘翅组件描述信息。
     * 包含每个注册鞘翅的组件ID、耐久、标签等。
     */
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

            // 组件ID
            root = root.copy().append(Component.literal("■ ")
                    .withStyle(ChatFormatting.YELLOW));
            root = root.copy().append(Component.literal(def.componentId())
                    .withStyle(ChatFormatting.WHITE));

            // 来源物品ID
            root = root.copy().append(Component.literal("\n  §7物品: §f" + def.elytraItem()));

            // 耐久信息
            StringBuilder durabilitySb = new StringBuilder();
            durabilitySb.append("\n  §7耐久: §e").append(def.durability().base());
            if (def.durability().multiplier() != 1.0f) {
                durabilitySb.append(" §7x§e").append(def.durability().multiplier());
            }
            if (def.durability().maxDurability() < Integer.MAX_VALUE) {
                durabilitySb.append(" §7(max: §e").append(def.durability().maxDurability()).append("§7)");
            }
            root = root.copy().append(Component.literal(durabilitySb.toString()));

            // 纹理信息
            if (def.texture() != null && def.texture().elytraLayer() != null) {
                root = root.copy().append(Component.literal("\n  §7纹理: §f" + def.texture().elytraLayer()));
            }

            // 标签
            if (!def.tags().isEmpty()) {
                root = root.copy().append(Component.literal("\n  §7标签: §b" + String.join("§7, §b", def.tags())));
            }

            // 依赖模组
            if (!def.compatibility().requiredMods().isEmpty()) {
                root = root.copy().append(Component.literal("\n  §7依赖: §d" + String.join("§7, §d", def.compatibility().requiredMods())));
            }
        }

        root = root.copy().append(Component.literal("\n\n"));
        root = root.copy().append(Component.translatable("jei.elytra_component.info.footer")
                .withStyle(ChatFormatting.GRAY));

        return root;
    }
}

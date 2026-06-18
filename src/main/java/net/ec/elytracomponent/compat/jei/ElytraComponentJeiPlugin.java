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
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class ElytraComponentJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID =
            ResourceLocation.fromNamespaceAndPath(ElytraComponentMod.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    /**
     * 注册JEI（Just Enough Items）配方信息的方法
     * @param registration 用于注册JEI信息的对象
     */
    public void registerRecipes(IRecipeRegistration registration) {
        // 从组件重新加载监听器中获取所有组件定义并转换为列表
        List<ElytraComponentDefinition> allDefs = new ArrayList<>(ElytraComponentReloadListener.getAll());

        // 如果没有注册的鞘翅组件，则跳过JEI信息注册
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

/**
 * 构建一个描述Elytra组件的文本组件
 * @param defs Elytra组件定义列表
 * @return 构建好的文本组件
 */
    private static Component buildDescription(List<ElytraComponentDefinition> defs) {
    // 创建一个空的文本组件作为根组件
        Component root = Component.literal("");

    // 添加标题行，显示组件数量，使用金色样式
        root = root.copy().append(Component.translatable("jei.elytra_component.info.header",
                defs.size()).withStyle(ChatFormatting.BLUE));
    // 添加两个换行符
        root = root.copy().append(Component.literal("\n\n"));

        root = root.copy().append(Component.translatable("jei.elytra_component.info.footer")
                .withStyle(ChatFormatting.BLUE));
        root = root.copy().append(Component.literal("\n\n"));

    // 遍历所有组件定义
        for (int i = 0; i < defs.size(); i++) {
            ElytraComponentDefinition def = defs.get(i);

        // 如果不是第一个组件，添加一个换行符
            if (i > 0) {
                root = root.copy().append(Component.literal("\n"));
            }

        // 添加组件标识符（黄色方块）和组件ID（白色文本）
            root = root.copy().append(Component.literal("■ ")
                    .withStyle(ChatFormatting.WHITE));
            root = root.copy().append(Component.literal(def.componentId())
                    .withStyle(ChatFormatting.WHITE));

        // 添加耐久度信息
            root = root.copy().append(Component.literal("\n  §7物品: §f" + def.elytraItem()));

        // 构建耐久度描述字符串
            StringBuilder durabilitySb = new StringBuilder();
            durabilitySb.append("\n  §7耐久: §e").append(def.durability().base());
        // 如果有耐久度倍数，添加倍数信息
            if (def.durability().multiplier() != 1.0f) {
                durabilitySb.append(" §7x§e").append(def.durability().multiplier());
            }
        // 如果有最大耐久度限制，添加最大耐久度信息
            if (def.durability().maxDurability() < Integer.MAX_VALUE) {
                durabilitySb.append(" §7(max: §e").append(def.durability().maxDurability()).append("§7)");
            }
            root = root.copy().append(Component.literal(durabilitySb.toString()));

        // 如果有纹理信息，添加纹理描述
            if (def.texture() != null && def.texture().elytraLayer() != null) {
                root = root.copy().append(Component.literal("\n  §7纹理: §f" + def.texture().elytraLayer()));
            }

        // 如果有标签，添加标签信息
            if (!def.tags().isEmpty()) {
                root = root.copy().append(Component.literal("\n  §7标签: §b" + String.join("§7, §b", def.tags())));
            }

        // 如果有依赖模组，添加依赖信息
            if (!def.compatibility().requiredMods().isEmpty()) {
                root = root.copy().append(Component.literal("\n  §7依赖: §d" + String.join("§7, §d", def.compatibility().requiredMods())));
            }
        }

        return root;
    }
}
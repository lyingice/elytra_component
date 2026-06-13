package net.ec.elytracomponent.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.ec.elytracomponent.ElytraComponentMod;
import net.ec.elytracomponent.api.ElytraComponentAPI;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.ec.elytracomponent.data.ElytraComponentDefinition;
import net.ec.elytracomponent.data.ElytraComponentReloadListener;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

/**
 * 鞘翅组件调试命令系统。
 *
 * 用法：
 *   /ecs attach <elytra_id> [player]       - 安装鞘翅到目标胸甲
 *   /ecs detach [player]                   - 从胸甲拆卸鞘翅
 *   /ecs info [player]                     - 查看胸甲上的组件信息
 *   /ecs durability <amount> [player]      - 设置组件耐久值
 *   /ecs give <componentId> [player]       - 给予一个注册组件的测试鞘翅
 *   /ecs list                              - 列出所有已注册的鞘翅组件
 *   /ecs reload                            - 重新加载数据包配置
 */
public class ElytraComponentCommand {

    private static final SimpleCommandExceptionType NO_CHESTPLATE = new SimpleCommandExceptionType(
            Component.translatable("command.elytra_component.error.no_chestplate")
    );
    private static final SimpleCommandExceptionType NO_COMPONENT = new SimpleCommandExceptionType(
            Component.translatable("command.elytra_component.error.no_component")
    );
    private static final SimpleCommandExceptionType INVALID_ELYTRA = new SimpleCommandExceptionType(
            Component.translatable("command.elytra_component.error.invalid_elytra")
    );

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ELYTRA_ITEMS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    ElytraComponentReloadListener.getAll().stream()
                            .map(def -> def.elytraItem().toString()),
                    builder
            );

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_COMPONENT_IDS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    ElytraComponentReloadListener.getAll().stream()
                            .map(ElytraComponentDefinition::componentId),
                    builder
            );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ecs")
                        .requires(source -> source.hasPermission(2))

                        // ========== attach ==========
                        .then(Commands.literal("attach")
                                .then(Commands.argument("elytra_id", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_ELYTRA_ITEMS)
                                        .executes(ctx -> attach(ctx,
                                                ctx.getSource().getPlayerOrException(),
                                                ResourceLocationArgument.getId(ctx, "elytra_id")))
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(ctx -> attach(ctx,
                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                        ResourceLocationArgument.getId(ctx, "elytra_id")))
                                        )
                                )
                        )

                        // ========== detach ==========
                        .then(Commands.literal("detach")
                                .executes(ctx -> detach(ctx, java.util.List.of(ctx.getSource().getPlayerOrException())))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ctx -> detach(ctx, EntityArgument.getPlayers(ctx, "targets")))
                                )
                        )

                        // ========== info ==========
                        .then(Commands.literal("info")
                                .executes(ctx -> info(ctx, java.util.List.of(ctx.getSource().getPlayerOrException())))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ctx -> info(ctx, EntityArgument.getPlayers(ctx, "targets")))
                                )
                        )

                        // ========== durability ==========
                        .then(Commands.literal("durability")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> setDurability(ctx,
                                                java.util.List.of(ctx.getSource().getPlayerOrException()),
                                                IntegerArgumentType.getInteger(ctx, "amount")))
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(ctx -> setDurability(ctx,
                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                        IntegerArgumentType.getInteger(ctx, "amount")))
                                        )
                                )
                        )

                        // ========== give ==========
                        .then(Commands.literal("give")
                                .then(Commands.argument("component_id", StringArgumentType.word())
                                        .suggests(SUGGEST_COMPONENT_IDS)
                                        .executes(ctx -> give(ctx,
                                                java.util.List.of(ctx.getSource().getPlayerOrException()),
                                                StringArgumentType.getString(ctx, "component_id")))
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(ctx -> give(ctx,
                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                        StringArgumentType.getString(ctx, "component_id")))
                                        )
                                )
                        )

                        // ========== list ==========
                        .then(Commands.literal("list")
                                .executes(ElytraComponentCommand::list)
                        )

                        // ========== reload ==========
                        .then(Commands.literal("reload")
                                .executes(ElytraComponentCommand::reload)
                        )
        );
    }

    // ==================== attach ====================

    private static int attach(CommandContext<CommandSourceStack> ctx, Player player, ResourceLocation elytraId) throws CommandSyntaxException {
        return attach(ctx, java.util.List.of(player), elytraId);
    }

    private static int attach(CommandContext<CommandSourceStack> ctx, Collection<? extends Player> targets, ResourceLocation elytraId) throws CommandSyntaxException {
        if (!ElytraComponentReloadListener.isRegisteredElytra(elytraId)) {
            throw INVALID_ELYTRA.create();
        }

        ElytraComponentDefinition def = ElytraComponentReloadListener.findByItemId(elytraId);
        if (def == null) {
            throw INVALID_ELYTRA.create();
        }

        ItemStack elytraStack = new ItemStack(BuiltInRegistries.ITEM.get(elytraId), 1);

        int count = 0;
        for (Player player : targets) {
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chestplate.isEmpty()) {
                ctx.getSource().sendFailure(Component.translatable("command.elytra_component.error.no_chestplate", player.getName()));
                continue;
            }

            ElytraComponent component = ElytraComponentAPI.createComponent(elytraStack, def);
            ElytraComponentAPI.setComponent(chestplate, component);
            player.setItemSlot(EquipmentSlot.CHEST, chestplate);
            count++;

            ctx.getSource().sendSuccess(() ->
                    Component.translatable("command.elytra_component.attach.success", player.getName(), elytraStack.getHoverName()), true);
        }
        return count;
    }

    // ==================== detach ====================

    private static int detach(CommandContext<CommandSourceStack> ctx, Collection<? extends Player> targets) throws CommandSyntaxException {
        int count = 0;
        for (Player player : targets) {
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chestplate.isEmpty() || !chestplate.has(ModComponents.ELYTRA_COMPONENT.get())) {
                ctx.getSource().sendFailure(Component.translatable("command.elytra_component.error.no_component", player.getName()));
                continue;
            }

            ElytraComponent component = ElytraComponentAPI.getComponent(chestplate);
            ItemStack restored = ElytraComponentAPI.restoreElytra(component);
            ElytraComponentAPI.removeComponent(chestplate);
            player.setItemSlot(EquipmentSlot.CHEST, chestplate);

            boolean given = player.getInventory().add(restored);
            if (!given) {
                player.drop(restored, false);
            }
            count++;

            ctx.getSource().sendSuccess(() ->
                    Component.translatable("command.elytra_component.detach.success", player.getName(), restored.getHoverName()), true);
        }
        return count;
    }

    // ==================== info ====================

    private static int info(CommandContext<CommandSourceStack> ctx, Collection<? extends Player> targets) {
        int count = 0;
        for (Player player : targets) {
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            ctx.getSource().sendSuccess(() -> Component.literal("§6=== 鞘翅组件信息 - ").append(player.getName()).append(" §6==="), false);

            if (chestplate.isEmpty()) {
                ctx.getSource().sendSuccess(() -> Component.literal("§c胸甲槽位为空"), false);
                continue;
            }

            ctx.getSource().sendSuccess(() ->
                    Component.literal("§7胸甲: §f").append(chestplate.getHoverName()), false);

            if (!chestplate.has(ModComponents.ELYTRA_COMPONENT.get())) {
                ctx.getSource().sendSuccess(() -> Component.literal("§c未安装鞘翅组件"), false);
                continue;
            }

            ElytraComponent component = chestplate.get(ModComponents.ELYTRA_COMPONENT.get());
            if (component == null) {
                ctx.getSource().sendSuccess(() -> Component.literal("§c组件数据为空"), false);
                continue;
            }

            ctx.getSource().sendSuccess(() ->
                    Component.literal("§7来源模组: §f" + component.sourceNamespace()), false);
            ctx.getSource().sendSuccess(() ->
                    Component.literal("§7原鞘翅ID: §f" + component.originalElytraId()), false);
            ctx.getSource().sendSuccess(() ->
                    Component.literal("§7耐久: §e" + component.currentDurability() + " §7/ §e" + component.maxDurability()), false);
            ctx.getSource().sendSuccess(() ->
                    Component.literal("§7耐久百分比: §6" + String.format("%.1f%%", (float) component.currentDurability() / component.maxDurability() * 100)), false);

            boolean canFly = chestplate.has(ModComponents.CAN_ELYTRA_FLY.get());
            ctx.getSource().sendSuccess(() ->
                    Component.literal("§7可飞行: " + (canFly ? "§a✔" : "§c✘")), false);

            if (component.textureOverride() != null) {
                ctx.getSource().sendSuccess(() ->
                        Component.literal("§7纹理覆盖: §f" + component.textureOverride()), false);
            }

            int dataCount = component.originalElytraComponents().size();
            ctx.getSource().sendSuccess(() ->
                    Component.literal("§7保留的原组件数据: §f" + dataCount + " 个"), false);
            count++;
        }
        return count;
    }

    // ==================== setDurability ====================

    private static int setDurability(CommandContext<CommandSourceStack> ctx, Collection<? extends Player> targets, int amount) throws CommandSyntaxException {
        int count = 0;
        for (Player player : targets) {
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chestplate.isEmpty() || !chestplate.has(ModComponents.ELYTRA_COMPONENT.get())) {
                ctx.getSource().sendFailure(Component.translatable("command.elytra_component.error.no_component", player.getName()));
                continue;
            }

            ElytraComponent old = chestplate.get(ModComponents.ELYTRA_COMPONENT.get());
            if (old == null) continue;

            int newDurability = Math.min(amount, old.maxDurability());
            ElytraComponent updated = new ElytraComponent(
                    old.sourceNamespace(),
                    old.originalElytraId(),
                    old.originalElytraComponents(),
                    newDurability,
                    old.maxDurability(),
                    old.textureOverride(),
                    old.extraData()
            );
            chestplate.set(ModComponents.ELYTRA_COMPONENT.get(), updated);
            player.setItemSlot(EquipmentSlot.CHEST, chestplate);
            count++;

            ctx.getSource().sendSuccess(() ->
                    Component.translatable("command.elytra_component.durability.success",
                            player.getName(), newDurability, old.maxDurability()), true);
        }
        return count;
    }

    // ==================== give ====================

    private static int give(CommandContext<CommandSourceStack> ctx, Collection<? extends Player> targets, String componentId) throws CommandSyntaxException {
        ElytraComponentDefinition def = ElytraComponentReloadListener.getDefinition(componentId);
        if (def == null) {
            ctx.getSource().sendFailure(Component.translatable("command.elytra_component.error.unknown_component", componentId));
            return 0;
        }

        ItemStack elytraStack = new ItemStack(BuiltInRegistries.ITEM.get(def.elytraItem()), 1);
        if (elytraStack.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("command.elytra_component.error.invalid_elytra"));
            return 0;
        }

        int count = 0;
        for (Player player : targets) {
            boolean given = player.getInventory().add(elytraStack.copy());
            if (!given) {
                player.drop(elytraStack.copy(), false);
            }
            count++;
            ctx.getSource().sendSuccess(() ->
                    Component.translatable("command.elytra_component.give.success", player.getName(), elytraStack.getHoverName()), true);
        }
        return count;
    }

    // ==================== list ====================

    private static int list(CommandContext<CommandSourceStack> ctx) {
        Collection<ElytraComponentDefinition> all = ElytraComponentReloadListener.getAll();

        if (all.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("§c没有已注册的鞘翅组件定义"), false);
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("§6=== 已注册的鞘翅组件 (" + all.size() + ") ==="), false);
        for (ElytraComponentDefinition def : all) {
            String durabilityStr = def.durability().base()
                    + (def.durability().multiplier() != 1.0f ? " x" + def.durability().multiplier() : "")
                    + (def.durability().maxDurability() < Integer.MAX_VALUE ? " (max: " + def.durability().maxDurability() + ")" : "");
            ctx.getSource().sendSuccess(() ->
                    Component.literal("§7- §e" + def.componentId()
                            + " §7→ §f" + def.elytraItem()
                            + " §7耐久: " + durabilityStr), false);
        }
        return all.size();
    }

    // ==================== reload ====================

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() ->
                Component.literal("§a请使用 /reload 命令重新加载数据包以更新鞘翅组件定义"), true);
        return 1;
    }
}

package net.ec.elytracomponent.client;

import net.ec.elytracomponent.ElytraComponentMod;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 客户端模组事件：注册自定义渲染层。
 */
@EventBusSubscriber(modid = ElytraComponentMod.MODID,value = Dist.CLIENT)
public class ClientModEvents {

    /**
     * 注册鞘翅渲染层到所有玩家实体渲染器
     */
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        // 为默认（宽）玩家模型添加组件鞘翅层
        PlayerRenderer defaultSkin = event.getSkin(PlayerSkin.Model.WIDE);
        if (defaultSkin != null) {
            defaultSkin.addLayer(new ComponentElytraLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>(
                    defaultSkin,
                    event.getEntityModels()
            ));
        }

        // 为纤细模型（slim arms）添加
        PlayerRenderer slimSkin = event.getSkin(PlayerSkin.Model.SLIM);
        if (slimSkin != null) {
            slimSkin.addLayer(new ComponentElytraLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>(
                    slimSkin,
                    event.getEntityModels()
            ));
        }
        ArmorStandRenderer armorStandRenderer = event.getRenderer(EntityType.ARMOR_STAND);
        if (armorStandRenderer != null) {
            armorStandRenderer.addLayer(new ComponentElytraLayer<>(armorStandRenderer, event.getEntityModels()));
        }
    }
}

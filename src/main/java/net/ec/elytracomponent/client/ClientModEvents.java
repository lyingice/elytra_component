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
@EventBusSubscriber(modid = ElytraComponentMod.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        // 初始化纹饰纹理
        ElytraTrimTextures.init();

        // 玩家 - 宽模型
        PlayerRenderer defaultSkin = event.getSkin(PlayerSkin.Model.WIDE);
        if (defaultSkin != null) {
            defaultSkin.addLayer(new ComponentElytraLayer<>(defaultSkin, event.getEntityModels()));
        }

        // 玩家 - 纤细模型
        PlayerRenderer slimSkin = event.getSkin(PlayerSkin.Model.SLIM);
        if (slimSkin != null) {
            slimSkin.addLayer(new ComponentElytraLayer<>(slimSkin, event.getEntityModels()));
        }

        // 盔甲架
        ArmorStandRenderer armorStandRenderer = event.getRenderer(EntityType.ARMOR_STAND);
        if (armorStandRenderer != null) {
            armorStandRenderer.addLayer(new ComponentElytraLayer<>(armorStandRenderer, event.getEntityModels()));
        }
    }
}

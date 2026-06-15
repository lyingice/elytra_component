package net.sc.elytracomponent.client;

import net.sc.elytracomponent.ElytraComponentMod;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ElytraComponentMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        ElytraTrimTextures.init();

        PlayerRenderer defaultSkin = event.getSkin("default");
        if (defaultSkin != null) {
            defaultSkin.addLayer(new ComponentElytraLayer<>(defaultSkin, event.getEntityModels()));
        }

        PlayerRenderer slimSkin = event.getSkin("slim");
        if (slimSkin != null) {
            slimSkin.addLayer(new ComponentElytraLayer<>(slimSkin, event.getEntityModels()));
        }

        ArmorStandRenderer armorStandRenderer = event.getRenderer(EntityType.ARMOR_STAND);
        if (armorStandRenderer != null) {
            armorStandRenderer.addLayer(new ComponentElytraLayer<>(armorStandRenderer, event.getEntityModels()));
        }
    }
}
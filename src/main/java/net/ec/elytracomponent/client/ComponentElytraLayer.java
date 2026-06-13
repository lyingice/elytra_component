package net.ec.elytracomponent.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ec.elytracomponent.component.ElytraComponent;
import net.ec.elytracomponent.component.ModComponents;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 自定义鞘翅渲染层：
 * 当胸甲安装了 ElytraComponent 时，渲染对应的鞘翅纹理。
 * 通过重写 shouldRender 和 getElytraTexture 实现组件驱动的渲染。
 */
@OnlyIn(Dist.CLIENT)
public class ComponentElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M> {

    private static final ResourceLocation DEFAULT_WINGS = ResourceLocation.withDefaultNamespace("textures/entity/elytra.png");
    private final ElytraModel<T> elytraModel;

    public ComponentElytraLayer(RenderLayerParent<T, M> renderer, EntityModelSet models) {
        super(renderer, models);
        this.elytraModel = new ElytraModel<>(models.bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);

        // 检查是否应该渲染
        if (!shouldRender(chestStack, entity)) return;

        // 获取纹理
        ResourceLocation texture = getElytraTexture(chestStack, entity);

        // 渲染鞘翅模型
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);
        this.getParentModel().copyPropertiesTo(this.elytraModel);
        this.elytraModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
                buffer, RenderType.armorCutoutNoCull(texture), chestStack.hasFoil()
        );
        this.elytraModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(ItemStack stack, T entity) {
        // 如果胸甲有 ElytraComponent，则渲染鞘翅纹理
        if (stack.has(ModComponents.ELYTRA_COMPONENT.get())) {
            return true;
        }
        // 回退到原版逻辑：物品本身就是鞘翅
        return super.shouldRender(stack, entity);
    }

    @Override
    public ResourceLocation getElytraTexture(ItemStack stack, T entity) {
        // 1. 检查是否有 ElytraComponent 及其纹理覆盖
        if (stack.has(ModComponents.ELYTRA_COMPONENT.get())) {
            ElytraComponent component = stack.get(ModComponents.ELYTRA_COMPONENT.get());
            if (component != null && component.textureOverride() != null) {
                return component.textureOverride();
            }

            // 2. 尝试自动推算纹理路径
            if (component != null) {
                ResourceLocation originalId = component.originalElytraId();
                // 推算路径：namespace:textures/item/name.png
                ResourceLocation guessedItem = ResourceLocation.fromNamespaceAndPath(
                        originalId.getNamespace(),
                        "textures/item/" + originalId.getPath() + ".png"
                );
                // 注意：这里返回推算路径，由 Minecraft 资源系统自动处理缺失情况
                return guessedItem;
            }
        }

        // 3. 玩家皮肤中的 Elytra 纹理
        if (entity instanceof AbstractClientPlayer player) {
            PlayerSkin skin = player.getSkin();
            if (skin.elytraTexture() != null) {
                return skin.elytraTexture();
            }
            if (skin.capeTexture() != null && player.isModelPartShown(PlayerModelPart.CAPE)) {
                return skin.capeTexture();
            }
        }

        // 4. 默认纹理
        return DEFAULT_WINGS;
    }
}

package net.sc.elytracomponent.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.sc.elytracomponent.component.ElytraComponent;
import net.sc.elytracomponent.component.ModComponents;
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
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ComponentElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M> {

    private static final ResourceLocation DEFAULT_WINGS = new ResourceLocation("textures/entity/elytra.png");
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

        if (!shouldRender(chestStack, entity)) return;

        ResourceLocation texture = getElytraTexture(chestStack, entity);

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);
        this.getParentModel().copyPropertiesTo(this.elytraModel);
        this.elytraModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // 渲染鞘翅主体
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
                buffer, RenderType.armorCutoutNoCull(texture), false, chestStack.hasFoil()
        );
        this.elytraModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        // 渲染纹饰层
        renderTrim(poseStack, buffer, packedLight, entity, chestStack);

        poseStack.popPose();
    }

    /**
     * 渲染鞘翅纹饰
     */
    private void renderTrim(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, ItemStack chestStack) {
        // 1.20.1: ArmorTrim.getTrim 需要 RegistryAccess，返回 Optional
        var trimOpt = ArmorTrim.getTrim(entity.level().registryAccess(), chestStack);
        if (trimOpt.isEmpty()) return;
        ArmorTrim trim = trimOpt.get();

        ResourceLocation patternId = trim.pattern().value().assetId();
        ResourceLocation trimTexture = ElytraTrimTextures.getTexture(patternId);
        if (trimTexture == null) return;

        // 设置颜色
        int color = getTrimColorInt(trim);

        RenderType renderType = RenderType.armorCutoutNoCull(trimTexture);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

        // 手动渲染模型，每个面使用指定颜色
        poseStack.pushPose();
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = ((color >> 24) & 0xFF) / 255.0F;
        this.elytraModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, a);
        poseStack.popPose();
    }

    private int getTrimColorInt(ArmorTrim trim) {
        Style style = trim.material().value().description().getStyle();
        TextColor textColor = style.getColor();
        if (textColor != null) {
            return textColor.getValue() | 0xFF000000; // 添加不透明 Alpha
        }
        return 0xFFFFFFFF; // 白色
    }

    @Override
    public boolean shouldRender(ItemStack stack, T entity) {
        if (ModComponents.hasComponent(stack)) {
            return true;
        }
        return super.shouldRender(stack, entity);
    }

    @Override
    public ResourceLocation getElytraTexture(ItemStack stack, T entity) {
        if (ModComponents.hasComponent(stack)) {
            ElytraComponent component = ModComponents.getComponent(stack);
            if (component != null && component.textureOverride() != null) {
                return component.textureOverride();
            }
            if (component != null) {
                return new ResourceLocation(
                        component.originalElytraId().getNamespace(),
                        "textures/item/" + component.originalElytraId().getPath() + ".png"
                );
            }
        }

        if (entity instanceof AbstractClientPlayer player) {
            ResourceLocation elytraTex = player.getElytraTextureLocation();
            if (elytraTex != null) {
                return elytraTex;
            }
        }

        return DEFAULT_WINGS;
    }
}

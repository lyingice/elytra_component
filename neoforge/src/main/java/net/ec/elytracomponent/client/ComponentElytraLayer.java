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
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

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

        if (!shouldRender(chestStack, entity)) return;
        ResourceLocation texture = getElytraTexture(chestStack, entity);

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);
        this.getParentModel().copyPropertiesTo(this.elytraModel);
        this.elytraModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // 渲染鞘翅主体
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
                buffer, RenderType.armorCutoutNoCull(texture), chestStack.hasFoil()
        );
        this.elytraModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        // 渲染纹饰层
        renderTrim(poseStack, buffer, packedLight, chestStack);

        poseStack.popPose();
    }

    /**
     * 渲染鞘翅纹饰
     */
    private void renderTrim(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ItemStack chestStack) {
        ArmorTrim trim = chestStack.get(DataComponents.TRIM);
        if (trim == null) return;

        ResourceLocation patternId = trim.pattern().value().assetId();
        ResourceLocation trimTexture = ElytraTrimTextures.getTexture(patternId);
        if (trimTexture == null) return;

        // 用原版 ArmorTrim 的内建纹理获取方式
        // trim 有 innerTexture 和 outerTexture 方法，用 outer
        ResourceLocation actualTexture = trimTexture;

        // 设置颜色
        int color = getTrimColorInt(trim);

        RenderType renderType = RenderType.armorCutoutNoCull(actualTexture);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

        // 手动渲染模型，每个面使用指定颜色
        poseStack.pushPose();
        this.elytraModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color);
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

    /**
     * 从纹饰材料获取颜色
     */
    private float[] getTrimColor(ArmorTrim trim) {
        // 从 TrimMaterial.description 的 Style 中提取颜色
        Style style = trim.material().value().description().getStyle();
        TextColor textColor = style.getColor();

        if (textColor != null) {
            int color = textColor.getValue();
            return new float[]{
                    ((color >> 16) & 0xFF) / 255.0F,
                    ((color >> 8) & 0xFF) / 255.0F,
                    (color & 0xFF) / 255.0F
            };
        }

        // 默认白色
        return new float[]{1.0F, 1.0F, 1.0F};
    }

    @Override
    public boolean shouldRender(ItemStack stack, T entity) {
        if (stack.has(ModComponents.ELYTRA_COMPONENT.get())) {
            return true;
        }
        return super.shouldRender(stack, entity);
    }

    @Override
    public ResourceLocation getElytraTexture(ItemStack stack, T entity) {
        if (stack.has(ModComponents.ELYTRA_COMPONENT.get())) {
            ElytraComponent component = stack.get(ModComponents.ELYTRA_COMPONENT.get());
            if (component != null) {
                // 数据包 texture.elytra_layer 优先
                if (component.textureOverride() != null) {
                    return component.textureOverride();
                }
                // 自动探测：原鞘翅命名空间的常见鞘翅纹理位置，逐级尝试
                ResourceLocation probed = probeTexture(component);
                if (probed != null) return probed;
                return DEFAULT_WINGS;
            }
        }

        if (entity instanceof AbstractClientPlayer player) {
            PlayerSkin skin = player.getSkin();
            if (skin.elytraTexture() != null) {
                return skin.elytraTexture();
            }
        }

        return DEFAULT_WINGS;
    }

    /**
     * 客户端纹理逐级探测（带缓存）：
     * 1. {原鞘翅namespace}:textures/entity/elytra.png（绝大多数模组鞘翅的标准位置）
     * 2. {原鞘翅namespace}:textures/models/elytra/{物品路径}.png
     * 3. {原鞘翅namespace}:textures/item/{物品路径}.png
     * 都找不到 → null，回退原版鞘翅纹理；数据包仍可通过 texture.elytra_layer 强制指定。
     */
    @org.jetbrains.annotations.Nullable
    private ResourceLocation probeTexture(ElytraComponent component) {
        ResourceLocation elytraId = component.source().originalElytraId();
        String ns = elytraId.getNamespace();
        String path = elytraId.getPath();
        ResourceLocation entityTex = ResourceLocation.fromNamespaceAndPath(ns, "textures/entity/elytra.png");
        ResourceLocation modelTex = ResourceLocation.fromNamespaceAndPath(ns, "textures/models/elytra/" + path + ".png");
        ResourceLocation itemTex = ResourceLocation.fromNamespaceAndPath(ns, "textures/item/" + path + ".png");
        if (resourceExists(entityTex)) return entityTex;
        if (resourceExists(modelTex)) return modelTex;
        if (resourceExists(itemTex)) return itemTex;
        return null;
    }

    private static final java.util.Map<ResourceLocation, Boolean> TEXTURE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private static boolean resourceExists(ResourceLocation id) {
        return TEXTURE_CACHE.computeIfAbsent(id, rl ->
                net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(rl).isPresent());
    }
}

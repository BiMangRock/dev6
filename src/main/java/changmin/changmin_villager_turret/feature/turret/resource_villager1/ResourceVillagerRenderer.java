package changmin.changmin_villager_turret.feature.turret.resource_villager1;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class ResourceVillagerRenderer extends MobRenderer<ResourceVillagerEntity, VillagerModel<ResourceVillagerEntity>> {
    // 💡 경고(deprecation)를 방지하기 위해 fromNamespaceAndPath를 사용하도록 수정했습니다.
    private static final ResourceLocation VILLAGER_BASE_SKIN = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/villager/villager.png");

    public ResourceVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        this.addLayer(new VillagerProfessionLayer<>(this, context.getResourceManager(), "villager"));
    }

    @Override
    public ResourceLocation getTextureLocation(ResourceVillagerEntity entity) {
        return VILLAGER_BASE_SKIN;
    }

    @Override
    protected boolean shouldShowName(ResourceVillagerEntity entity) {
        return true;
    }

    @Override
    protected void renderNameTag(ResourceVillagerEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        if (d0 <= 4096.0D) {
            float heightOffset = entity.getBbHeight() + 0.5F;
            poseStack.pushPose();
            poseStack.translate(0.0D, (double)heightOffset, 0.0D);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();

            Font font = this.getFont();

            // 1. 레벨 정보 텍스트 (Y축: -22.0F)
            String levelText = String.format("Lv. %d", entity.getTurretLevel());
            Component textComponent = new TextComponent(levelText);
            float textWidth = (float)font.width(textComponent);
            float textX = -textWidth / 2.0F;
            float lvlTextY = -22.0F;

            font.drawInBatch(textComponent, textX, lvlTextY, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 2. 자원 수확 남은시간 쿨타임 정보 텍스트 (Y축: -12.0F)
            int secondsLeft = entity.getResourceTimer() / 20;
            String cooldownText = String.format("Next Drop: %ds", secondsLeft);
            Component cdComponent = new TextComponent(cooldownText);
            float cdTextWidth = (float)font.width(cdComponent);
            float cdTextX = -cdTextWidth / 2.0F;
            float cdTextY = -12.0F;

            font.drawInBatch(cdComponent, cdTextX, cdTextY, 0xFFFFAA00, false, matrix4f, buffer, false, 0, packedLight);

            // 가로 50px 고정형 그래픽 바 위치 및 크기 선언
            float barWidth = 50.0F;
            float barHeight = 3.0F;
            float barX = -barWidth / 2.0F;

            // ==========================================
            // 🟩 3. HP 바 및 우측 체력 수치 렌더링 (위치 Y: 2.0F)
            // ==========================================
            float hpBarY = 2.0F;
            float hpRatio = (float)entity.getHealth() / (float)entity.getMaxHealth();
            if (hpRatio > 1.0F) hpRatio = 1.0F;
            float currentHpWidth = barWidth * hpRatio;

            // HP 바 배경
            drawSolidQuad(matrix4f, buffer, barX, hpBarY, barX + barWidth, hpBarY + barHeight, 0x80505050);

            // HP 바 색상 결정
            int healthColor = 0xFF00FF00;
            if (hpRatio < 0.25F) {
                healthColor = 0xFFFF0000;
            } else if (hpRatio < 0.5F) {
                healthColor = 0xFFFFFF00;
            }
            drawSolidQuad(matrix4f, buffer, barX, hpBarY, barX + currentHpWidth, hpBarY + barHeight, healthColor);

            // HP 바 우측 수치 텍스트 (X축 오프셋 적용)
            String hpTextVal = String.format("%d/%d", (int)entity.getHealth(), (int)entity.getMaxHealth());
            float hpTextX = barX + barWidth + 4.0F;
            float hpTextY = hpBarY - 2.5F; // 폰트 중심 정렬을 위한 조정
            font.drawInBatch(new TextComponent(hpTextVal), hpTextX, hpTextY, -1, false, matrix4f, buffer, false, 0, packedLight);

            // ==========================================
            // 🟦 4. XP 바 및 우측 경험치 수치 렌더링 (위치 Y: 7.0F)
            // ==========================================
            float xpBarY = 7.0F;
            float xpRatio = (float)entity.getXp() / (float)entity.getNeededXp();
            if (xpRatio > 1.0F) xpRatio = 1.0F;
            float currentXpWidth = barWidth * xpRatio;

            // XP 바 배경
            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + barWidth, xpBarY + barHeight, 0x80505050);

            // XP 바 전경
            int xpColor = 0xFF33CCFF;
            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + currentXpWidth, xpBarY + barHeight, xpColor);

            // XP 바 우측 수치 텍스트 (X축 오프셋 적용)
            String xpTextVal = String.format("%d/%d", entity.getXp(), entity.getNeededXp());
            float xpTextX = barX + barWidth + 4.0F;
            float xpTextY = xpBarY - 2.5F; // 폰트 중심 정렬을 위한 조정
            font.drawInBatch(new TextComponent(xpTextVal), xpTextX, xpTextY, 0xFF33CCFF, false, matrix4f, buffer, false, 0, packedLight);

            poseStack.popPose();
        }
    }

    private void drawSolidQuad(Matrix4f matrix, MultiBufferSource buffer, float minX, float minY, float maxX, float maxY, int color) {
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;

        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        consumer.vertex(matrix, minX, maxY, 0.0F).color(red, green, blue, alpha).endVertex();
        consumer.vertex(matrix, maxX, maxY, 0.0F).color(red, green, blue, alpha).endVertex();
        consumer.vertex(matrix, maxX, minY, 0.0F).color(red, green, blue, alpha).endVertex();
        consumer.vertex(matrix, minX, minY, 0.0F).color(red, green, blue, alpha).endVertex();
    }
}
package changmin.myMod.feature.turret.villager_turret;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class VillagerTurretRenderer extends MobRenderer<VillagerTurretEntity, VillagerModel<VillagerTurretEntity>> {
    private static final ResourceLocation VILLAGER_LOCATION =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/villager/villager.png");

    public VillagerTurretRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(VillagerTurretEntity entity) {
        return VILLAGER_LOCATION;
    }

    @Override
    protected boolean shouldShowName(VillagerTurretEntity entity) {
        return true;
    }

    @Override
    protected void renderNameTag(VillagerTurretEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        if (d0 <= 4096.0D) {
            float heightOffset = entity.getBbHeight() + 0.5F;
            poseStack.pushPose();
            poseStack.translate(0.0D, (double)heightOffset, 0.0D);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();

            // 1. 레벨 정보 표시 (getTurretLevel 적용)
            Font font = this.getFont();
            String infoText = String.format("Lv. %d (%d/%d)", entity.getTurretLevel(), (int)entity.getHealth(), (int)entity.getMaxHealth());
            Component textComponent = new TextComponent(infoText);
            float textWidth = (float)font.width(textComponent);
            float textX = -textWidth / 2.0F;
            float textY = -12.0F;

            font.drawInBatch(textComponent, textX, textY, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 2. 가로 50px 고정형 그래픽 체력 바 비율 연산
            float barWidth = 50.0F;
            float barHeight = 3.0F;
            float barX = -barWidth / 2.0F;
            float barY = 2.0F;

            float hpRatio = (float)entity.getHealth() / (float)entity.getMaxHealth();
            float currentHpWidth = barWidth * hpRatio;

            // 배경 반투명 회색 바 그리기 (RenderType.lightning으로 맵핑 버전 충돌 해결)
            drawSolidQuad(matrix4f, buffer, barX, barY, barX + barWidth, barY + barHeight, 0x80505050);

            // 남은 체력 비율에 따른 동적 색상 변화
            int healthColor = 0xFF00FF00;
            if (hpRatio < 0.25F) {
                healthColor = 0xFFFF0000;
            } else if (hpRatio < 0.5F) {
                healthColor = 0xFFFFFF00;
            }

            // 현재 체력 전경 색상바 그리기
            drawSolidQuad(matrix4f, buffer, barX, barY, barX + currentHpWidth, barY + barHeight, healthColor);

            poseStack.popPose();
        }
    }

    private void drawSolidQuad(Matrix4f matrix, MultiBufferSource buffer, float minX, float minY, float maxX, float maxY, int color) {
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;

        // textBackground() 대신 안전한 lightning() 단색 버퍼 유형 적용
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        consumer.vertex(matrix, minX, maxY, 0.0F).color(red, green, blue, alpha).endVertex();
        consumer.vertex(matrix, maxX, maxY, 0.0F).color(red, green, blue, alpha).endVertex();
        consumer.vertex(matrix, maxX, minY, 0.0F).color(red, green, blue, alpha).endVertex();
        consumer.vertex(matrix, minX, minY, 0.0F).color(red, green, blue, alpha).endVertex();
    }
}
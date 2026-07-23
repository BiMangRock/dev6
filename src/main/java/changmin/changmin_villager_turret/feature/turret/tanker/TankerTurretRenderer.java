package changmin.changmin_villager_turret.feature.turret.tanker;

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

public class TankerTurretRenderer extends MobRenderer<TankerTurretEntity, VillagerModel<TankerTurretEntity>> {
    private static final ResourceLocation TANKER_LOCATION =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/villager/villager.png");

    public TankerTurretRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(TankerTurretEntity entity) {
        return TANKER_LOCATION;
    }

    @Override
    protected boolean shouldShowName(TankerTurretEntity entity) {
        return true;
    }

    @Override
    protected void renderNameTag(TankerTurretEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        if (d0 <= 4096.0D) {
            float heightOffset = entity.getBbHeight() + 0.5F;
            poseStack.pushPose();
            poseStack.translate(0.0D, (double)heightOffset, 0.0D);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();

            Font font = this.getFont();

            // 🆕 0. 영문 이름표 추가 (Y축: -32.0F)
            String nameText = "Tanker Turret";
            Component nameComponent = new TextComponent(nameText);
            float nameWidth = (float)font.width(nameComponent);
            font.drawInBatch(nameComponent, -nameWidth / 2.0F, -32.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 1. 레벨 정보 텍스트
            String infoText = String.format("Lv. %d (%d/%d)", entity.getTurretLevel(), (int)entity.getHealth(), (int)entity.getMaxHealth());
            Component textComponent = new TextComponent(infoText);
            float textWidth = (float)font.width(textComponent);
            font.drawInBatch(textComponent, -textWidth / 2.0F, -22.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 2. 경험치 텍스트
            String xpText = String.format("XP: %d/%d", entity.getXp(), entity.getNeededXp());
            Component xpComponent = new TextComponent(xpText);
            float xpTextWidth = (float)font.width(xpComponent);
            font.drawInBatch(xpComponent, -xpTextWidth / 2.0F, -12.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

            float barWidth = 50.0F;
            float barHeight = 3.0F;
            float barX = -barWidth / 2.0F;

            // ==========================================
            // 🟩 3. 가변형 HP 바 렌더링 (도발 시 노란색 쉴드로 물듬!)
            // ==========================================
            float hpBarY = 2.0F;
            float hpRatio = (float)entity.getHealth() / (float)entity.getMaxHealth();
            if (hpRatio > 1.0F) hpRatio = 1.0F;
            float currentHpWidth = barWidth * hpRatio;

            drawSolidQuad(matrix4f, buffer, barX, hpBarY, barX + barWidth, hpBarY + barHeight, 0x80505050);

            // [기획 적용] 도발 버프 활성화 중일 때는 단단한 메탈릭 황금색으로 물듦!
            int healthColor = 0xFF00FF00; // 녹색
            if (entity.getIsTaunting()) {
                healthColor = 0xFFFFBB00; // 도발 보호막 황금 주황색
            } else if (hpRatio < 0.25F) {
                healthColor = 0xFFFF0000; // 딸피 빨간색
            } else if (hpRatio < 0.5F) {
                healthColor = 0xFFFFFF00; // 경고 노란색
            }
            drawSolidQuad(matrix4f, buffer, barX, hpBarY, barX + currentHpWidth, hpBarY + barHeight, healthColor);

            // ==========================================
            // 🟦 4. XP 바 렌더링 (위치 Y: 7.0F)
            // ==========================================
            float xpBarY = 7.0F;
            float xpRatio = (float)entity.getXp() / (float)entity.getNeededXp();
            if (xpRatio > 1.0F) xpRatio = 1.0F;
            float currentXpWidth = barWidth * xpRatio;

            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + barWidth, xpBarY + barHeight, 0x80505050);

            int xpColor = 0xFF8822FF; // 단단한 탱커 느낌의 자주빛/보라색 XP바 설정
            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + currentXpWidth, xpBarY + barHeight, xpColor);

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
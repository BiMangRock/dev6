package changmin.myMod.feature.turret.lightning_wizard;

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

public class LightningWizardRenderer extends MobRenderer<LightningWizardEntity, VillagerModel<LightningWizardEntity>> {
    private static final ResourceLocation WIZARD_LOCATION =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/villager/villager.png");

    public LightningWizardRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(LightningWizardEntity entity) {
        return WIZARD_LOCATION;
    }

    @Override
    protected boolean shouldShowName(LightningWizardEntity entity) {
        return true;
    }

    @Override
    protected void renderNameTag(LightningWizardEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        if (d0 <= 4096.0D) {
            float heightOffset = entity.getBbHeight() + 0.5F;
            poseStack.pushPose();
            poseStack.translate(0.0D, (double)heightOffset, 0.0D);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();

            Font font = this.getFont();

            // 1. 레벨 정보 및 체력 정수 출력 (Y: -22.0F)
            String infoText = String.format("Lv. %d (%d/%d)", entity.getTurretLevel(), (int)entity.getHealth(), (int)entity.getMaxHealth());
            Component textComponent = new TextComponent(infoText);
            float textWidth = (float)font.width(textComponent);
            font.drawInBatch(textComponent, -textWidth / 2.0F, -22.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 2. 경험치 출력 (Y: -12.0F)
            String xpText = String.format("XP: %d/%d", entity.getXp(), entity.getNeededXp());
            Component xpComponent = new TextComponent(xpText);
            float xpTextWidth = (float)font.width(xpComponent);
            font.drawInBatch(xpComponent, -xpTextWidth / 2.0F, -12.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

            float barWidth = 50.0F;
            float barHeight = 3.0F;
            float barX = -barWidth / 2.0F;

            // ==========================================
            // 🟩 3. HP 바 그리기 (위치 Y: 2.0F)
            // ==========================================
            float hpBarY = 2.0F;
            float hpRatio = (float)entity.getHealth() / (float)entity.getMaxHealth();
            if (hpRatio > 1.0F) hpRatio = 1.0F;
            float currentHpWidth = barWidth * hpRatio;

            drawSolidQuad(matrix4f, buffer, barX, hpBarY, barX + barWidth, hpBarY + barHeight, 0x80505050);

            int healthColor = 0xFF00FF00;
            if (hpRatio < 0.25F) {
                healthColor = 0xFFFF0000;
            } else if (hpRatio < 0.5F) {
                healthColor = 0xFFFFFF00;
            }
            drawSolidQuad(matrix4f, buffer, barX, hpBarY, barX + currentHpWidth, hpBarY + barHeight, healthColor);

            // ==========================================
            // 🟦 4. XP 바 그리기 (위치 Y: 7.0F)
            // ==========================================
            float xpBarY = 7.0F;
            float xpRatio = (float)entity.getXp() / (float)entity.getNeededXp();
            if (xpRatio > 1.0F) xpRatio = 1.0F;
            float currentXpWidth = barWidth * xpRatio;

            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + barWidth, xpBarY + barHeight, 0x80505050);

            int xpColor = 0xFF7E00FF; // 신비로운 자주색
            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + currentXpWidth, xpBarY + barHeight, xpColor);

            // ==========================================
            // 🍊 5. 궁극기(ULT) 게이지 바 그리기 (위치 Y: 12.0F)
            // ==========================================
            float ultBarY = 12.0F;
            float ultRatio = 0.0F;
            int ultColor = 0xFFFFAA00; // 궁극기 충전 중 (주황/노랑)

            if (entity.getUltActiveTime() > 0) {
                // 궁극기 활성화(시전) 상태인 경우 역방향 하늘색 바 렌더링
                ultRatio = (float) entity.getUltActiveTime() / (float) (60 + entity.getUltDurationLevel() * 20);
                ultColor = 0xFF00E1FF; // 하늘색 전류 에너지
            } else if (entity.getUltCooldown() > 0) {
                // 쿨타임 충전 비율
                ultRatio = 1.0F - ((float) entity.getUltCooldown() / 1200.0F);
            } else {
                ultRatio = 1.0F; // 충전 완료
            }

            float currentUltWidth = barWidth * ultRatio;

            drawSolidQuad(matrix4f, buffer, barX, ultBarY, barX + barWidth, ultBarY + barHeight, 0x80505050);
            drawSolidQuad(matrix4f, buffer, barX, ultBarY, barX + currentUltWidth, ultBarY + barHeight, ultColor);

            // 궁극기 우측 텍스트 상태 정보 표기
            String ultState = entity.getUltActiveTime() > 0 ? "STORM" : (entity.getUltCooldown() <= 0 ? "READY" : (entity.getUltCooldown() / 20) + "s");
            font.drawInBatch(new TextComponent("ULT: " + ultState), barX + barWidth + 4.0F, ultBarY - 2.5F, ultColor, false, matrix4f, buffer, false, 0, packedLight);

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
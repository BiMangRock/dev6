package changmin.myMod.feature.turret.plasma_wizard;

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

public class PlasmaWizardRenderer extends MobRenderer<PlasmaWizardEntity, VillagerModel<PlasmaWizardEntity>> {
    private static final ResourceLocation LOCATION =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/villager/villager.png");

    public PlasmaWizardRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(PlasmaWizardEntity entity) {
        return LOCATION;
    }

    @Override
    protected boolean shouldShowName(PlasmaWizardEntity entity) {
        return true;
    }

    @Override
    protected void renderNameTag(PlasmaWizardEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        if (d0 <= 4096.0D) {
            float heightOffset = entity.getBbHeight() + 0.5F;
            poseStack.pushPose();
            poseStack.translate(0.0D, (double)heightOffset, 0.0D);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();

            Font font = this.getFont();

            // 1. 등급 및 HP 정보
            String infoText = String.format("Lv. %d (HP: %d/%d)", entity.getTurretLevel(), (int)entity.getHealth(), (int)entity.getMaxHealth());
            Component textComponent = new TextComponent(infoText);
            float textWidth = (float)font.width(textComponent);
            font.drawInBatch(textComponent, -textWidth / 2.0F, -27.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 2. 공격 성공 누적치 기반 성장 경험치 정보
            String xpText = String.format("Overload XP: %d/%d", entity.getXp(), entity.getNeededXp());
            Component xpComponent = new TextComponent(xpText);
            float xpTextWidth = (float)font.width(xpComponent);
            font.drawInBatch(xpComponent, -xpTextWidth / 2.0F, -17.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 3. 쿨타임 남은 시간 정보
            float currentCd = entity.getCurrentCooldown();
            String cdText = currentCd > 0 ? String.format("Overheating: %.1fs", currentCd / 20.0F) : "System Ready!";
            Component cdComponent = new TextComponent(cdText);
            float cdTextWidth = (float)font.width(cdComponent);
            font.drawInBatch(cdComponent, -cdTextWidth / 2.0F, -7.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

            float barWidth = 50.0F;
            float barHeight = 2.0F;
            float barX = -barWidth / 2.0F;

            // 4. HP바 그리기
            float hpBarY = 5.0F;
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

            // 5. 경험치바 그리기
            float xpBarY = 10.0F;
            float xpRatio = (float)entity.getXp() / (float)entity.getNeededXp();
            if (xpRatio > 1.0F) xpRatio = 1.0F;
            float currentXpWidth = barWidth * xpRatio;
            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + barWidth, xpBarY + barHeight, 0x80505050);
            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + currentXpWidth, xpBarY + barHeight, 0xFF55FF55);

            // 6. 충전 대기 시간바 그리기 (따뜻한 오렌지 계열)
            float cdBarY = 15.0F;
            float maxCd = entity.getCalculatedCooldown();
            float cdRatio = maxCd > 0 ? (float)(maxCd - currentCd) / maxCd : 1.0F;
            if (cdRatio > 1.0F) cdRatio = 1.0F;
            float currentCdWidth = barWidth * cdRatio;
            drawSolidQuad(matrix4f, buffer, barX, cdBarY, barX + barWidth, cdBarY + barHeight, 0x80505050);
            drawSolidQuad(matrix4f, buffer, barX, cdBarY, barX + currentCdWidth, cdBarY + barHeight, 0xFFFF9933);

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
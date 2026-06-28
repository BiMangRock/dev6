package changmin.myMod.feature.zombie.zombie1;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class ZombieBossRenderer extends GeoEntityRenderer<ZombieBossEntity> {

    public ZombieBossRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieBossModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public ResourceLocation getTextureLocation(ZombieBossEntity instance) {
        return ResourceLocation.fromNamespaceAndPath("mymod", "textures/entity/zombie1.png");
    }

    // 🆕 접근 제어자를 protected에서 public으로 수정하여 컴파일 충돌을 해결했습니다.
    @Override
    public boolean shouldShowName(ZombieBossEntity entity) {
        return true;
    }

    @Override
    protected void renderNameTag(ZombieBossEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        if (d0 <= 4096.0D) {
            float heightOffset = entity.getBbHeight() + 0.5F;
            poseStack.pushPose();
            poseStack.translate(0.0D, (double) heightOffset, 0.0D);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();

            Font font = this.getFont();

            // 1. 레벨 및 체력 정보 텍스트 (Y축: -22.0F)
            String infoText = String.format("Lv. %d (%d/%d)", entity.getBossLevel(), (int) entity.getHealth(), (int) entity.getMaxHealth());
            Component textComponent = new TextComponent(infoText);
            float textWidth = (float) font.width(textComponent);
            float textX = -textWidth / 2.0F;
            float hpTextY = -22.0F;

            font.drawInBatch(textComponent, textX, hpTextY, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 2. 경험치(처치 수) 정보 텍스트 (Y축: -12.0F)
            String xpText = String.format("XP: %d/%d", entity.getCurrentXp(), entity.getNeededXp());
            Component xpComponent = new TextComponent(xpText);
            float xpTextWidth = (float) font.width(xpComponent);
            float xpTextX = -xpTextWidth / 2.0F;
            float xpTextY = -12.0F;

            font.drawInBatch(xpComponent, xpTextX, xpTextY, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 그래픽 바 수치 선언
            float barWidth = 50.0F;
            float barHeight = 3.0F;
            float barX = -barWidth / 2.0F;

            // ==========================================
            // 🟩 3. HP 바 렌더링 (Y축: 2.0F)
            // ==========================================
            float hpBarY = 2.0F;
            float hpRatio = (float) entity.getHealth() / (float) entity.getMaxHealth();
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
            // 🟦 4. XP 바 렌더링 (Y축: 7.0F)
            // ==========================================
            float xpBarY = 7.0F;
            float xpRatio = (float) entity.getCurrentXp() / (float) entity.getNeededXp();
            if (xpRatio > 1.0F) xpRatio = 1.0F;
            float currentXpWidth = barWidth * xpRatio;

            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + barWidth, xpBarY + barHeight, 0x80505050);

            int xpColor = 0xFF33CCFF; // 파란색 경험치 바
            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + currentXpWidth, xpBarY + barHeight, xpColor);

            poseStack.popPose();
        }
    }

    private void drawSolidQuad(Matrix4f matrix, MultiBufferSource buffer, float minX, float minY, float maxX, float maxY, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        consumer.vertex(matrix, minX, maxY, 0.0F).color(red, green, blue, alpha).endVertex();
        consumer.vertex(matrix, maxX, maxY, 0.0F).color(red, green, blue, alpha).endVertex();
        consumer.vertex(matrix, maxX, minY, 0.0F).color(red, green, blue, alpha).endVertex();
        consumer.vertex(matrix, minX, minY, 0.0F).color(red, green, blue, alpha).endVertex();
    }
}
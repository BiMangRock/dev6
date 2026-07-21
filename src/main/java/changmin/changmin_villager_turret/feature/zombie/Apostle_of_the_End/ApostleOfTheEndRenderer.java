package changmin.changmin_villager_turret.feature.zombie.Apostle_of_the_End;

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

public class ApostleOfTheEndRenderer extends GeoEntityRenderer<ApostleOfTheEndEntity> {

    public ApostleOfTheEndRenderer(EntityRendererProvider.Context context) {
        super(context, new ApostleOfTheEndModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public ResourceLocation getTextureLocation(ApostleOfTheEndEntity instance) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/girl.png");
    }

    @Override
    public boolean shouldShowName(ApostleOfTheEndEntity entity) {
        return true;
    }

    @Override
    protected void renderNameTag(ApostleOfTheEndEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        if (d0 <= 4096.0D) {
            float heightOffset = entity.getBbHeight() + 0.5F;
            poseStack.pushPose();
            poseStack.translate(0.0D, (double) heightOffset, 0.0D);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();

            Font font = this.getFont();

            // 1. 체력 정보 텍스트 (Y축: -22.0F)
            String hpText = String.format("HP: %d/%d", (int) entity.getHealth(), (int) entity.getMaxHealth());
            if (!entity.isAlive() || entity.getHealth() <= 0) hpText = "DEAD";
            else if (entity.getSummonedAngelsCount() > 0) hpText += " [INVULNERABLE]";

            Component hpComponent = new TextComponent(hpText);
            font.drawInBatch(hpComponent, -((float)font.width(hpComponent) / 2.0F), -22.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 2. 소환 대기 정보 텍스트 (Y축: -12.0F)
            String summonText = String.format("Summon: %.1fs (Angels: %d/3)", (float)entity.getSummonTimer() / 20.0F, entity.getSummonedAngelsCount());
            Component summonComponent = new TextComponent(summonText);
            font.drawInBatch(summonComponent, -((float)font.width(summonComponent) / 2.0F), -12.0F, 0xFFFFFF00, false, matrix4f, buffer, false, 0, packedLight);

            float barWidth = 50.0F;
            float barHeight = 3.0F;
            float barX = -barWidth / 2.0F;

            // 3. HP 바 (Y축: 2.0F)
            float hpRatio = Math.max(0, entity.getHealth() / entity.getMaxHealth());
            drawSolidQuad(matrix4f, buffer, barX, 2.0F, barX + barWidth, 2.0F + barHeight, 0x80505050); // 배경
            int hpColor = (entity.getSummonedAngelsCount() > 0) ? 0xFF00FFFF : 0xFF00FF00; // 무적일 땐 하늘색, 아니면 초록색
            drawSolidQuad(matrix4f, buffer, barX, 2.0F, barX + (barWidth * hpRatio), 2.0F + barHeight, hpColor);

            // 4. 소환 쿨타임 바 (Y축: 7.0F)
            // 타이머가 0에 가까워질수록 바가 가득 차게 설정 (반전 계산)
            float summonRatio = 1.0F - ((float)entity.getSummonTimer() / (float)ApostleOfTheEndEntity.MAX_SUMMON_COOLDOWN);
            drawSolidQuad(matrix4f, buffer, barX, 7.0F, barX + barWidth, 7.0F + barHeight, 0x80505050); // 배경
            drawSolidQuad(matrix4f, buffer, barX, 7.0F, barX + (barWidth * summonRatio), 7.0F + barHeight, 0xFFFFCC00); // 노란색

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
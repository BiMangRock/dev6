package changmin.changmin_villager_turret.feature.zombie.healer_zombie;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class HealerZombieRenderer extends GeoEntityRenderer<HealerZombieEntity> {
    public HealerZombieRenderer(EntityRendererProvider.Context context) {
        super(context, new HealerZombieModel());
    }

    @Override
    public ResourceLocation getTextureLocation(HealerZombieEntity instance) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/healer_zombie.png");
    }

    @Override
    public boolean shouldShowName(HealerZombieEntity entity) {
        return true;
    }

    @Override
    protected void renderNameTag(HealerZombieEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (this.entityRenderDispatcher.distanceToSqr(entity) > 4096.0D) return;

        poseStack.pushPose();
        poseStack.translate(0.0D, entity.getBbHeight() + 1.2F, 0.0D);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = poseStack.last().pose();
        Font font = this.getFont();

        // 0. 최상단 타이틀 (연한 핑크색으로 표시, entity.getHealerLevel() 사용)
        String levelInfo = "Lv. " + entity.getHealerLevel() + " Healer Zombie";
        drawInfo(font, matrix, buffer, levelInfo, -30.0F, 0xFFFFB6C1, packedLight);

        float barW = 50.0F;
        float barX = -barW / 2.0F;
        float needed = (float) entity.getNeededXp();
        if (needed <= 0) needed = 1;

        // 1. HP 바 렌더링 (텍스트 포맷: 현재체력/최대체력, 색상: 연한 분홍색 #FFB6C1)
        String hpText = String.format("HP: %d/%d", (int) entity.getHealth(), (int) entity.getMaxHealth());
        drawInfo(font, matrix, buffer, hpText, -15.0F, -1, packedLight);
        float hpRatio = Math.min(1.0F, entity.getHealth() / entity.getMaxHealth());
        drawSolidQuad(matrix, buffer, barX, -7.0F, barX + barW, -5.0F, 0x80505050); // 어두운 회색 배경바
        drawSolidQuad(matrix, buffer, barX, -7.0F, barX + (barW * hpRatio), -5.0F, 0xFFFFB6C1); // 연분홍색 바 채우기

        // 2. XP 바 렌더링 (텍스트 포맷: 현재경험치/요구경험치, 색상: 골드)
        String xpText = String.format("XP: %d/%d", entity.getXp(), (int) needed);
        drawInfo(font, matrix, buffer, xpText, 0.0F, -1, packedLight);
        float xpRatio = Math.min(1.0F, (float) entity.getXp() / needed);
        drawSolidQuad(matrix, buffer, barX, 8.0F, barX + barW, 10.0F, 0x80505050); // 어두운 회색 배경바
        drawSolidQuad(matrix, buffer, barX, 8.0F, barX + (barW * xpRatio), 10.0F, 0xFFDAA520); // 금색 바 채우기

        poseStack.popPose();
    }

    private void drawInfo(Font font, Matrix4f matrix, MultiBufferSource buffer, String text, float y, int color, int packedLight) {
        font.drawInBatch(text, -font.width(text) / 2.0F, y, color, false, matrix, buffer, false, 0, packedLight);
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
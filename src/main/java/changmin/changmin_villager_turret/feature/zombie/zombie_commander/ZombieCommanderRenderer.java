package changmin.changmin_villager_turret.feature.zombie.zombie_commander;

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

public class ZombieCommanderRenderer extends GeoEntityRenderer<ZombieCommanderEntity> {
    public ZombieCommanderRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieCommanderModel());
    }

    @Override public ResourceLocation getTextureLocation(ZombieCommanderEntity instance) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/zombie_commander.png");
    }

    @Override public boolean shouldShowName(ZombieCommanderEntity entity) { return true; }

    @Override
    protected void renderNameTag(ZombieCommanderEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (this.entityRenderDispatcher.distanceToSqr(entity) > 4096.0D) return;

        poseStack.pushPose();
        poseStack.translate(0.0D, entity.getBbHeight() + 1.2F, 0.0D);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = poseStack.last().pose();
        Font font = this.getFont();

        // 💡 0. 최상단 레벨 표시 추가
        String levelInfo = "Lv. " + entity.getBossLevel() + " Commander";
        drawInfo(font, matrix, buffer, levelInfo, -45.0F, 0xFFDAA520, packedLight);

        float barW = 50.0F;
        float barX = -barW / 2.0F;
        float needed = (float)entity.getNeededXp();
        if (needed <= 0) needed = 1;

        // 1. HP
        drawInfo(font, matrix, buffer, String.format("HP: %d/%d", (int)entity.getHealth(), (int)entity.getMaxHealth()), -30.0F, -1, packedLight);
        float hpRatio = Math.min(1.0F, entity.getHealth() / entity.getMaxHealth());
        drawSolidQuad(matrix, buffer, barX, -22.0F, barX + barW, -20.0F, 0x80505050);
        drawSolidQuad(matrix, buffer, barX, -22.0F, barX + (barW * hpRatio), -20.0F, 0xFFEE4444);

        // 2. Buff XP
        drawInfo(font, matrix, buffer, String.format("Buff XP: %d/%d", entity.getBuffXp(), (int)needed), -15.0F, -1, packedLight);
        float bXpRatio = Math.min(1.0F, (float)entity.getBuffXp() / needed);
        drawSolidQuad(matrix, buffer, barX, -7.0F, barX + barW, -5.0F, 0x80505050);
        drawSolidQuad(matrix, buffer, barX, -7.0F, barX + (barW * bXpRatio), -5.0F, 0xFFDAA520);

        // 3. Attack XP
        drawInfo(font, matrix, buffer, String.format("Atk XP: %d/%d", entity.getAttackXp(), (int)needed), 0.0F, -1, packedLight);
        float aXpRatio = Math.min(1.0F, (float)entity.getAttackXp() / needed);
        drawSolidQuad(matrix, buffer, barX, 8.0F, barX + barW, 10.0F, 0x80505050);
        drawSolidQuad(matrix, buffer, barX, 8.0F, barX + (barW * aXpRatio), 10.0F, 0xFFAA00FF);

        // 4. Ultimate
        int remain = (600 - entity.getSyncCooldown()) / 20;
        drawInfo(font, matrix, buffer, "Ultimate: " + Math.max(0, remain) + "s", 15.0F, -1, packedLight);
        float cdRatio = Math.min(1.0F, (float)entity.getSyncCooldown() / 600.0F);
        drawSolidQuad(matrix, buffer, barX, 23.0F, barX + barW, 25.0F, 0x80505050);
        drawSolidQuad(matrix, buffer, barX, 23.0F, barX + (barW * cdRatio), 25.0F, 0xFF33CCFF);

        poseStack.popPose();
    }

    private void drawInfo(Font font, Matrix4f matrix, MultiBufferSource buffer, String text, float y, int color, int packedLight) {
        font.drawInBatch(text, -font.width(text)/2.0F, y, color, false, matrix, buffer, false, 0, packedLight);
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
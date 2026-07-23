package changmin.changmin_villager_turret.feature.hero.healer_hero;

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

public class HealerHeroRenderer extends GeoEntityRenderer<HealerHeroEntity> {
    public HealerHeroRenderer(EntityRendererProvider.Context context) {
        super(context, new HealerHeroModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public ResourceLocation getTextureLocation(HealerHeroEntity instance) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/hero_healer.png");
    }

    @Override
    public boolean shouldShowName(HealerHeroEntity entity) {
        return true;
    }

    @Override
    protected void renderNameTag(HealerHeroEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        if (d0 <= 4096.0D) {
            float heightOffset = entity.getBbHeight() + 0.5F;
            poseStack.pushPose();
            poseStack.translate(0.0D, (double) heightOffset, 0.0D);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();
            Font font = this.getFont();

            // 1. 이름 태그 배치
            String nameText = "Healer Hero";
            Component nameComponent = new TextComponent(nameText);
            float nameWidth = (float) font.width(nameComponent);
            font.drawInBatch(nameComponent, -nameWidth / 2.0F, -37.0F, 0xFFFFB6C1, false, matrix4f, buffer, false, 0, packedLight);

            // 2. 레벨 및 체력 수치 배치
            String infoText = String.format("Lv. %d (%d/%d)", entity.getHeroLevel(), (int) entity.getHealth(), (int) entity.getMaxHealth());
            Component textComponent = new TextComponent(infoText);
            float textWidth = (float) font.width(textComponent);
            font.drawInBatch(textComponent, -textWidth / 2.0F, -27.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 3. XP 정보 수치 배치 (힐 XP와 공격 XP 모두 표시)
            String xpText = String.format("Heal XP: %d/%d | Atk XP: %d/%d", entity.getXpBuff(), entity.getNeededXp(), entity.getXpAttack(), entity.getNeededXp());
            Component xpComponent = new TextComponent(xpText);
            float xpTextWidth = (float) font.width(xpComponent);
            font.drawInBatch(xpComponent, -xpTextWidth / 2.0F, -17.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

            float barWidth = 60.0F;
            float barHeight = 2.5F;
            float barX = -barWidth / 2.0F;

            // 4. 연분홍색 HP바 (위치 Y: -7.0F)
            float hpBarY = -7.0F;
            float hpRatio = (float) entity.getHealth() / (float) entity.getMaxHealth();
            if (hpRatio > 1.0F) hpRatio = 1.0F;
            float currentHpWidth = barWidth * hpRatio;
            drawSolidQuad(matrix4f, buffer, barX, hpBarY, barX + barWidth, hpBarY + barHeight, 0x80505050);
            drawSolidQuad(matrix4f, buffer, barX, hpBarY, barX + currentHpWidth, hpBarY + barHeight, 0xFFFFB6C1);

            // 5. 힐 경험치바 (금빛 옐로우, 위치 Y: -2.0F)
            float healXpBarY = -2.0F;
            float healRatio = (float) entity.getXpBuff() / (float) entity.getNeededXp();
            if (healRatio > 1.0F) healRatio = 1.0F;
            float currentHealWidth = barWidth * healRatio;
            drawSolidQuad(matrix4f, buffer, barX, healXpBarY, barX + barWidth, healXpBarY + barHeight, 0x80505050);
            drawSolidQuad(matrix4f, buffer, barX, healXpBarY, barX + currentHealWidth, healXpBarY + barHeight, 0xFFFFD700);

            // 6. 공격 경험치바 (스카이블루, 위치 Y: 3.0F)
            float atkXpBarY = 3.0F;
            float atkRatio = (float) entity.getXpAttack() / (float) entity.getNeededXp();
            if (atkRatio > 1.0F) atkRatio = 1.0F;
            float currentAtkWidth = barWidth * atkRatio;
            drawSolidQuad(matrix4f, buffer, barX, atkXpBarY, barX + barWidth, atkXpBarY + barHeight, 0x80505050);
            drawSolidQuad(matrix4f, buffer, barX, atkXpBarY, barX + currentAtkWidth, atkXpBarY + barHeight, 0xFF33CCFF);

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
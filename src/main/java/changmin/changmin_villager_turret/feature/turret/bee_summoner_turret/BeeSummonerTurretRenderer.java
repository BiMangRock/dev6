package changmin.changmin_villager_turret.feature.turret.bee_summoner_turret;

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

public class BeeSummonerTurretRenderer extends MobRenderer<BeeSummonerTurretEntity, VillagerModel<BeeSummonerTurretEntity>> {
    private static final ResourceLocation VILLAGER_LOCATION =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/villager/villager.png");

    public BeeSummonerTurretRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BeeSummonerTurretEntity entity) {
        return VILLAGER_LOCATION;
    }

    @Override
    protected boolean shouldShowName(BeeSummonerTurretEntity entity) {
        return true;
    }

    @Override
    protected void renderNameTag(BeeSummonerTurretEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
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
            String nameText = "Bee Summoner";
            Component nameComponent = new TextComponent(nameText);
            float nameWidth = (float)font.width(nameComponent);
            font.drawInBatch(nameComponent, -nameWidth / 2.0F, -32.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 1. 레벨 및 체력 표기 (Y축: -22.0F)
            String infoText = String.format("Lv. %d (%d/%d)", entity.getTurretLevel(), (int)entity.getHealth(), (int)entity.getMaxHealth());
            Component textComponent = new TextComponent(infoText);
            float textWidth = (float)font.width(textComponent);
            float textX = -textWidth / 2.0F;
            font.drawInBatch(textComponent, textX, -22.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 2. 경험치 텍스트 표기 (Y축: -12.0F)
            String xpText = String.format("XP: %d/%d", entity.getXp(), entity.getNeededXp());
            Component xpComponent = new TextComponent(xpText);
            float xpTextWidth = (float)font.width(xpComponent);
            float xpTextX = -xpTextWidth / 2.0F;
            font.drawInBatch(xpComponent, xpTextX, -12.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

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
            // 🟨 4. XP 바 그리기 (위치 Y: 7.0F)
            // ==========================================
            float xpBarY = 7.0F;
            float xpRatio = (float)entity.getXp() / (float)entity.getNeededXp();
            if (xpRatio > 1.0F) xpRatio = 1.0F;
            float currentXpWidth = barWidth * xpRatio;

            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + barWidth, xpBarY + barHeight, 0x80505050);

            int xpColor = 0xFFFFCC00; // 꿀벌 테마의 금빛 노란색
            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + currentXpWidth, xpBarY + barHeight, xpColor);

            // ==========================================
            // ⬜ 5. 🆕 쿨타임 바 그리기 (위치 Y: 12.0F)
            // ==========================================
            int remainingCD = entity.getSummonCooldown();
            if (remainingCD > 0) {
                float cdBarY = 12.0F;
                float cdRatio = (float)remainingCD / (float)entity.getCalculatedCooldown();
                if (cdRatio > 1.0F) cdRatio = 1.0F;
                float currentCdWidth = barWidth * cdRatio;

                // 쿨타임 백그라운드 바
                drawSolidQuad(matrix4f, buffer, barX, cdBarY, barX + barWidth, cdBarY + barHeight, 0x80505050);

                // 쿨타임 전경색 (출격 대기 시간을 상징하는 차분한 실버/화이트 색상)
                int cdColor = 0xFFDDDDDD;
                drawSolidQuad(matrix4f, buffer, barX, cdBarY, barX + currentCdWidth, cdBarY + barHeight, cdColor);
            }

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
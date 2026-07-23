package changmin.changmin_villager_turret.feature.turret.healer;

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

public class HealerRenderer extends MobRenderer<HealerTurretEntity, VillagerModel<HealerTurretEntity>> {

    private static final ResourceLocation PRIEST_LOCATION =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/villager/profession/cleric.png");
    private static final ResourceLocation BASE_VILLAGER_LOCATION =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/villager/villager.png");

    public HealerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);

        this.addLayer(new net.minecraft.client.renderer.entity.layers.RenderLayer<HealerTurretEntity, VillagerModel<HealerTurretEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, HealerTurretEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                renderColoredCutoutModel(this.getParentModel(), PRIEST_LOCATION, poseStack, buffer, packedLight, entity, 1.0F, 1.0F, 1.0F);
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(HealerTurretEntity entity) {
        return BASE_VILLAGER_LOCATION;
    }

    @Override
    protected boolean shouldShowName(HealerTurretEntity entity) {
        return true;
    }

    @Override
    protected void renderNameTag(HealerTurretEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        if (d0 <= 4096.0D) {
            float heightOffset = entity.getBbHeight() + 0.5F;
            poseStack.pushPose();
            poseStack.translate(0.0D, (double)heightOffset, 0.0D);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();

            Font font = this.getFont();

            // 🆕 0. 영문 이름표 추가 (Y축: -37.0F)
            String nameText = "Healer Turret";
            Component nameComponent = new TextComponent(nameText);
            float nameWidth = (float)font.width(nameComponent);
            font.drawInBatch(nameComponent, -nameWidth / 2.0F, -37.0F, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 1. 레벨 및 체력 정보 표시
            String infoText = String.format("Lv. %d (HP: %d/%d)", entity.getTurretLevel(), (int)entity.getHealth(), (int)entity.getMaxHealth());
            Component textComponent = new TextComponent(infoText);
            float textWidth = (float)font.width(textComponent);
            float textX = -textWidth / 2.0F;
            float hpTextY = -27.0F;

            font.drawInBatch(textComponent, textX, hpTextY, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 2. 치유량 누적 기반 경험치 텍스트
            String xpText = String.format("Heal XP: %d/%d", entity.getXp(), entity.getNeededXp());
            Component xpComponent = new TextComponent(xpText);
            float xpTextWidth = (float)font.width(xpComponent);
            float xpTextX = -xpTextWidth / 2.0F;
            float xpTextY = -17.0F;

            font.drawInBatch(xpComponent, xpTextX, xpTextY, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 🆕 3. 실시간 쿨타임 남은 시간 초 단위 텍스트 렌더링
            float currentCd = entity.getCurrentCooldown();
            String cdText = currentCd > 0 ? String.format("Cooldown: %.1fs", currentCd / 20.0F) : "Heal Ready!";
            Component cdComponent = new TextComponent(cdText);
            float cdTextWidth = (float)font.width(cdComponent);
            float cdTextX = -cdTextWidth / 2.0F;
            float cdTextY = -7.0F;

            font.drawInBatch(cdComponent, cdTextX, cdTextY, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 가로 50px 고정형 그래픽 바 위치 및 크기 선언
            float barWidth = 50.0F;
            float barHeight = 2.0F;
            float barX = -barWidth / 2.0F;

            // 4. 체력 게이지 바 렌더링 (Y축 5.0F)
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

            // 5. 경험치 바 렌더링 (Y축 10.0F)
            float xpBarY = 10.0F;
            float xpRatio = (float)entity.getXp() / (float)entity.getNeededXp();
            if (xpRatio > 1.0F) xpRatio = 1.0F;
            float currentXpWidth = barWidth * xpRatio;

            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + barWidth, xpBarY + barHeight, 0x80505050);

            int xpColor = 0xFF55FF55;
            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + currentXpWidth, xpBarY + barHeight, xpColor);

            // 🆕 6. 실시간 쿨타임 충전바 렌더링 (Y축 15.0F)
            float cdBarY = 15.0F;
            float maxCd = entity.getCalculatedCooldown();

            // 쿨다운이 돌 때 게이지바가 충전되는(Ready를 향해 차오르는) 비율 계산
            float cdRatio = maxCd > 0 ? (float)(maxCd - currentCd) / maxCd : 1.0F;
            if (cdRatio > 1.0F) cdRatio = 1.0F;
            float currentCdWidth = barWidth * cdRatio;

            drawSolidQuad(matrix4f, buffer, barX, cdBarY, barX + barWidth, cdBarY + barHeight, 0x80505050);

            int cdColor = 0xFFFF9933; // 따뜻한 오렌지/옐로우 빛의 충전바 색상
            drawSolidQuad(matrix4f, buffer, barX, cdBarY, barX + currentCdWidth, cdBarY + barHeight, cdColor);

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
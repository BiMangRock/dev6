package changmin.myMod.feature.turret.healer;

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

        // 🆕 기본 주민 몸체 위에 성직자 옷을 덧칠해서 렌더링하는 레이어 추가
        this.addLayer(new net.minecraft.client.renderer.entity.layers.RenderLayer<HealerTurretEntity, VillagerModel<HealerTurretEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, HealerTurretEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                renderColoredCutoutModel(this.getParentModel(), PRIEST_LOCATION, poseStack, buffer, packedLight, entity, 1.0F, 1.0F, 1.0F);
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(HealerTurretEntity entity) {
        // 🆕 기본 반환 텍스처는 투명하지 않은 주민 본체 몸뚱아리로 변경합니다.
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

            // 1. 레벨 및 체력 정보 표시
            String infoText = String.format("Lv. %d (HP: %d/%d)", entity.getTurretLevel(), (int)entity.getHealth(), (int)entity.getMaxHealth());
            Component textComponent = new TextComponent(infoText);
            float textWidth = (float)font.width(textComponent);
            float textX = -textWidth / 2.0F;
            float hpTextY = -22.0F;

            font.drawInBatch(textComponent, textX, hpTextY, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 2. 치유량 누적 기반 경험치 텍스트
            String xpText = String.format("Heal XP: %d/%d", entity.getXp(), entity.getNeededXp());
            Component xpComponent = new TextComponent(xpText);
            float xpTextWidth = (float)font.width(xpComponent);
            float xpTextX = -xpTextWidth / 2.0F;
            float xpTextY = -12.0F;

            font.drawInBatch(xpComponent, xpTextX, xpTextY, -1, false, matrix4f, buffer, false, 0, packedLight);

            // 그래픽 바 디자인 가로길이 고정값 선언
            float barWidth = 50.0F;
            float barHeight = 3.0F;
            float barX = -barWidth / 2.0F;

            // 3. 체력 게이지 바 렌더링 (Y축 2.0F)
            float hpBarY = 2.0F;
            float hpRatio = (float)entity.getHealth() / (float)entity.getMaxHealth();
            if (hpRatio > 1.0F) hpRatio = 1.0F;
            float currentHpWidth = barWidth * hpRatio;

            drawSolidQuad(matrix4f, buffer, barX, hpBarY, barX + barWidth, hpBarY + barHeight, 0x80505050);

            // 체력 비율에 따른 직관적 컬러 디스플레이
            int healthColor = 0xFF00FF00;
            if (hpRatio < 0.25F) {
                healthColor = 0xFFFF0000;
            } else if (hpRatio < 0.5F) {
                healthColor = 0xFFFFFF00;
            }
            drawSolidQuad(matrix4f, buffer, barX, hpBarY, barX + currentHpWidth, hpBarY + barHeight, healthColor);

            // 4. 경험치(치유한 체력 누적치) 바 렌더링 (Y축 7.0F)
            float xpBarY = 7.0F;
            float xpRatio = (float)entity.getXp() / (float)entity.getNeededXp();
            if (xpRatio > 1.0F) xpRatio = 1.0F;
            float currentXpWidth = barWidth * xpRatio;

            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + barWidth, xpBarY + barHeight, 0x80505050);

            // 성직자 테마에 어울리는 신성한 에메랄드 그린 컬러로 경험치 바를 그립니다.
            int xpColor = 0xFF55FF55;
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
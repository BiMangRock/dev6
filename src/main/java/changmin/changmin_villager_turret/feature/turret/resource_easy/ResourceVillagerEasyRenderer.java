package changmin.changmin_villager_turret.feature.turret.resource_easy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ResourceVillagerEasyRenderer extends MobRenderer<ResourceVillagerEasyEntity, VillagerModel<ResourceVillagerEasyEntity>> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/villager/villager.png");

    public ResourceVillagerEasyRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        this.addLayer(new VillagerProfessionLayer<>(this, context.getResourceManager(), "villager"));
    }

    @Override
    public ResourceLocation getTextureLocation(ResourceVillagerEasyEntity entity) {
        return VILLAGER_BASE_SKIN;
    }

    @Override
    protected boolean shouldShowName(ResourceVillagerEasyEntity entity) {
        return true;
    }

    @Override
    protected void renderNameTag(ResourceVillagerEasyEntity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        if (d0 <= 4096.0D) {
            float heightOffset = entity.getBbHeight() + 0.5F;
            poseStack.pushPose();
            poseStack.translate(0.0D, (double)heightOffset, 0.0D);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = poseStack.last().pose();

            Font font = this.getFont();

            // 0. 이름표 렌더링 (Y축: -32.0F) - 🌟 [수정] Easy 문구 적용
            // 이름은 언어 파일(entity.changmin_villager_turret.*)을 따릅니다. 게임 언어가 한국어면 한국어로 표시
            Component nameComponent = entity.getType().getDescription();
            float nameWidth = (float)font.width(nameComponent);
            font.drawInBatch(nameComponent, -nameWidth / 2.0F, -32.0F, -1, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);

            // 1. 레벨 정보 텍스트 (Y축: -22.0F)
            int levelValue = entity.getTurretLevel();
            String levelText = (levelValue >= 5) ? "Lv. INFINITE" : String.format("Lv. %d", levelValue);
            Component textComponent = Component.literal(levelText);
            float textWidth = (float)font.width(textComponent);
            float textX = -textWidth / 2.0F;
            float lvlTextY = -22.0F;

            font.drawInBatch(textComponent, textX, lvlTextY, -1, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);

            // 2. 전투 상태 및 실시간 카운트다운 출력 (Y축: -12.0F)
            boolean isCombat = entity.isCombatPhase();
            int phaseTicksLeft = entity.getPhaseTimer();
            int totalSecs = phaseTicksLeft / 20;
            int mins = totalSecs / 60;
            int secs = totalSecs % 60;

            String phaseText;
            int textColor;
            if (isCombat) {
                phaseText = String.format("[COMBAT] Remaining: %02d:%02d", mins, secs);
                textColor = 0xFFFF4444; // 붉은색 전투 텍스트
            } else {
                phaseText = String.format("[PREP] Next Wave: %02d:%02d", mins, secs);
                textColor = 0xFF55FF55; // 녹색 정비 대기 텍스트
            }

            Component cdComponent = Component.literal(phaseText);
            float cdTextWidth = (float)font.width(cdComponent);
            float cdTextX = -cdTextWidth / 2.0F;
            float cdTextY = -12.0F;

            font.drawInBatch(cdComponent, cdTextX, cdTextY, textColor, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);

            // 게이지 바 가로폭 선언
            float barWidth = 50.0F;
            float barHeight = 3.0F;
            float barX = -barWidth / 2.0F;

            // ==========================================
            // 🟩 3. HP 바 및 우측 체력 수치 렌더링 (Y축: 2.0F)
            // ==========================================
            float hpBarY = 2.0F;
            float hpRatio = (float)entity.getHealth() / (float)entity.getMaxHealth();
            if (hpRatio > 1.0F) hpRatio = 1.0F;
            float currentHpWidth = barWidth * hpRatio;

            // HP 바 배경
            drawSolidQuad(matrix4f, buffer, barX, hpBarY, barX + barWidth, hpBarY + barHeight, 0x80505050);

            // HP 바 전경 색상 결정
            int healthColor = 0xFF00FF00;
            if (hpRatio < 0.25F) {
                healthColor = 0xFFFF0000;
            } else if (hpRatio < 0.5F) {
                healthColor = 0xFFFFFF00;
            }
            drawSolidQuad(matrix4f, buffer, barX, hpBarY, barX + currentHpWidth, hpBarY + barHeight, healthColor);

            // HP 수치 출력
            String hpTextVal = String.format("%d/%d", (int)entity.getHealth(), (int)entity.getMaxHealth());
            float hpTextX = barX + barWidth + 4.0F;
            float hpTextY = hpBarY - 2.5F;
            font.drawInBatch(Component.literal(hpTextVal), hpTextX, hpTextY, -1, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);

            // ==========================================
            // 🟦 4. 가변형 프로그레스 바 렌더링 (Y축: 7.0F)
            // ==========================================
            float xpBarY = 7.0F;
            float xpRatio;
            String progressTextVal;

            if (isCombat) {
                // 전투 상태: 15초 자원 드롭 쿨타임 % 표시
                xpRatio = (float)entity.getXp() / (float)entity.getNeededXp();
                progressTextVal = String.format("Drop: %d%%", (entity.getXp() * 100) / entity.getNeededXp());
            } else {
                // 대기 상태: 4분(4800틱) 정비 시간 충전 % 표시
                int totalPrepTicks = 4800;
                int elapsedPrep = totalPrepTicks - entity.getPhaseTimer();
                xpRatio = (float)elapsedPrep / totalPrepTicks;
                progressTextVal = String.format("Prep: %d%%", (int)(xpRatio * 100));
            }

            if (xpRatio > 1.0F) xpRatio = 1.0F;
            if (xpRatio < 0.0F) xpRatio = 0.0F;
            float currentXpWidth = barWidth * xpRatio;

            // 프로그레스 바 배경
            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + barWidth, xpBarY + barHeight, 0x80505050);

            // 프로그레스 바 전경
            int xpColor = isCombat ? 0xFF33CCFF : 0xFF55FF55; // 전투 시 파란색, 정비 시 연두색으로 유연 변경
            drawSolidQuad(matrix4f, buffer, barX, xpBarY, barX + currentXpWidth, xpBarY + barHeight, xpColor);

            // 우측 상태 백분율 텍스트 출력
            float xpTextX = barX + barWidth + 4.0F;
            float xpTextY = xpBarY - 2.5F;
            font.drawInBatch(Component.literal(progressTextVal), xpTextX, xpTextY, xpColor, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);

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
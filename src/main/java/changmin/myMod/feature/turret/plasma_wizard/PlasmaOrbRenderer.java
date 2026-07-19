package changmin.myMod.feature.turret.plasma_wizard;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class PlasmaOrbRenderer extends GeoProjectilesRenderer<PlasmaOrbEntity> {
    public PlasmaOrbRenderer(EntityRendererProvider.Context context) {
        super(context, new PlasmaOrbModel());
    }

    @Override
    public void render(PlasmaOrbEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // 💡 실시간으로 갱신되는 스케일 값에 맞추어 시각적 3D 렌더링 크기 배율 조정
        float scale = entity.getOrbScale();
        poseStack.scale(scale, scale, scale);

        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
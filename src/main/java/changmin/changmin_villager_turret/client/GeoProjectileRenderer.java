package changmin.changmin_villager_turret.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 🏹 GeckoLib 투사체 렌더러 (GeckoLib 4용)
 *
 * GeckoLib 3에는 투사체 전용 GeoProjectilesRenderer가 있었는데, GeckoLib 4에서는 GeoEntityRenderer로 합쳐지면서
 * 투사체를 날아가는 방향으로 돌려주는 처리가 빠졌습니다. (생명체가 아니면 회전 0)
 * 예전과 똑같이 보이도록 GL3 투사체 렌더러와 같은 회전(yaw - 90, pitch)을 적용합니다.
 */
public class GeoProjectileRenderer<T extends Entity & GeoAnimatable> extends GeoEntityRenderer<T> {

    public GeoProjectileRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
    }

    @Override
    protected void applyRotations(T animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot())));
    }
}

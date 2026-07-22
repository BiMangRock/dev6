package changmin.changmin_villager_turret.feature.zombie.demon1;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class Demon1BatRenderer extends EntityRenderer<Demon1BatEntity> {
    private static final ResourceLocation BAT_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/bat.png");

    // 컴파일 에러 해결: BatModel에 타입 매개변수가 없으므로 제네릭 생략
    private final BatModel batModel;

    public Demon1BatRenderer(EntityRendererProvider.Context context) {
        super(context);
        // 바닐라 박쥐 레이어를 직접 구워와서 모델 생성 (따로 geo/animation 파일 불필요)
        this.batModel = new BatModel(context.bakeLayer(ModelLayers.BAT));
    }

    @Override
    public void render(Demon1BatEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 1. 박쥐 크기 축소 (투사체용으로 알맞게 스케일 조절)
        poseStack.scale(0.35F, 0.35F, 0.35F);

        // 2. 투사체 방향에 맞춘 회전 처리
        float lerpYaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F;
        float lerpPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        // 💡 1.18.2 ~ 1.19.2 기준 회전 코드
        poseStack.mulPose(com.mojang.math.Vector3f.YP.rotationDegrees(lerpYaw));
        poseStack.mulPose(com.mojang.math.Vector3f.ZP.rotationDegrees(lerpPitch));

        // ※ 만약 1.19.3 이상 혹은 1.20+ 버전에서 Vector3f 컴파일 에러 발생 시, 위 두 줄을 지우고 아래 주석 코드로 변경해 주세요.
        // poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(lerpYaw));
        // poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(lerpPitch));

        // 3. NullPointerException 방지를 위해 setupAnim을 우회하고 자식 파트를 수동 제어하여 날개짓 연출
        ModelPart root = this.batModel.root();
        ModelPart head = root.getChild("head");
        ModelPart body = root.getChild("body");
        ModelPart rightWing = body.getChild("right_wing");
        ModelPart leftWing = body.getChild("left_wing");
        ModelPart rightWingTip = rightWing.getChild("right_wing_tip");
        ModelPart leftWingTip = leftWing.getChild("left_wing_tip");

        float ageInTicks = (float)entity.tickCount + partialTicks;

        // 비행 중인 상태의 각도 연산 구현
        head.xRot = 0.0F;
        head.yRot = 0.0F;
        head.zRot = 0.0F;
        head.setPos(0.0F, 0.0F, 0.0F);
        rightWing.setPos(0.0F, 0.0F, 0.0F);
        leftWing.setPos(0.0F, 0.0F, 0.0F);

        body.xRot = ((float)Math.PI / 4F) + Mth.cos(ageInTicks * 0.1F) * 0.15F;
        body.yRot = 0.0F;

        // 날개 퍼덕임 연산
        rightWing.yRot = Mth.cos(ageInTicks * 74.48451F * ((float)Math.PI / 180F)) * (float)Math.PI * 0.25F;
        leftWing.yRot = -rightWing.yRot;
        rightWingTip.yRot = rightWing.yRot * 0.5F;
        leftWingTip.yRot = -rightWing.yRot * 0.5F;

        // 4. 버퍼에 렌더링
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        this.batModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Demon1BatEntity entity) {
        return BAT_TEXTURE;
    }
}
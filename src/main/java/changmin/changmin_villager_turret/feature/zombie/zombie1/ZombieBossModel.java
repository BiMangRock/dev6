package changmin.changmin_villager_turret.feature.zombie.zombie1;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ZombieBossModel extends AnimatedGeoModel<ZombieBossEntity> {

    // 모델 JSON 파일 지정
    @Override
    public ResourceLocation getModelLocation(ZombieBossEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/zombie1.geo.json");
    }

    // 텍스처(스킨) 이미지 파일 지정
    @Override
    public ResourceLocation getTextureLocation(ZombieBossEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/zombie1.png");
    }

    // 애니메이션 JSON 파일 지정
    @Override
    public ResourceLocation getAnimationFileLocation(ZombieBossEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/zombie1.animation.json");
    }
}
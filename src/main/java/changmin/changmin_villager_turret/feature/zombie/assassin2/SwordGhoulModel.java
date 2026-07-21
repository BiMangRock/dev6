package changmin.changmin_villager_turret.feature.zombie.assassin2;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SwordGhoulModel extends AnimatedGeoModel<SwordGhoulEntity> {

    @Override
    public ResourceLocation getModelLocation(SwordGhoulEntity object) {
        // 블록벤치에서 내보낸 geo.json 파일 경로
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/sword_ghoul.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(SwordGhoulEntity object) {
        // 제작한 png 텍스처 파일 경로
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/sword_ghoul.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SwordGhoulEntity animatable) {
        // 제작한 animation.json 파일 경로
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/sword_ghoul.animation.json");
    }
}
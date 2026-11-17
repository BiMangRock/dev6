package changmin.changmin_villager_turret.feature.zombie.assassin2;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class SwordGhoulModel extends GeoModel<SwordGhoulEntity> {

    @Override
    public ResourceLocation getModelResource(SwordGhoulEntity object) {
        // 블록벤치에서 내보낸 geo.json 파일 경로
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/sword_ghoul.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SwordGhoulEntity object) {
        // 제작한 png 텍스처 파일 경로
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/sword_ghoul.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SwordGhoulEntity animatable) {
        // 제작한 animation.json 파일 경로
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/sword_ghoul.animation.json");
    }
}
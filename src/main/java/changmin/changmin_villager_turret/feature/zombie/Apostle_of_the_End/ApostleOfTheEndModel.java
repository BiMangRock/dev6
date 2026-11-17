package changmin.changmin_villager_turret.feature.zombie.Apostle_of_the_End;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class ApostleOfTheEndModel extends GeoModel<ApostleOfTheEndEntity> {
    @Override
    public ResourceLocation getModelResource(ApostleOfTheEndEntity object) {
        // 수정됨: fromNamespaceAndPath 사용
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/gairl.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ApostleOfTheEndEntity object) {
        // 수정됨: fromNamespaceAndPath 사용
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/girl.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ApostleOfTheEndEntity animatable) {
        // 수정됨: fromNamespaceAndPath 사용
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/girl.animation.json");
    }
}
package changmin.changmin_villager_turret.feature.zombie.Apostle_of_the_End;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ApostleOfTheEndModel extends AnimatedGeoModel<ApostleOfTheEndEntity> {
    @Override
    public ResourceLocation getModelLocation(ApostleOfTheEndEntity object) {
        // 수정됨: fromNamespaceAndPath 사용
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/gairl.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ApostleOfTheEndEntity object) {
        // 수정됨: fromNamespaceAndPath 사용
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/girl.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ApostleOfTheEndEntity animatable) {
        // 수정됨: fromNamespaceAndPath 사용
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/girl.animation.json");
    }
}
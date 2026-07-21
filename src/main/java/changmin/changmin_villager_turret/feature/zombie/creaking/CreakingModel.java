package changmin.changmin_villager_turret.feature.zombie.creaking;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class CreakingModel extends AnimatedGeoModel<CreakingEntity> {
    @Override
    public ResourceLocation getModelLocation(CreakingEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/creaking.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(CreakingEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/creaking.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(CreakingEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/creaking.animation.json");
    }
}
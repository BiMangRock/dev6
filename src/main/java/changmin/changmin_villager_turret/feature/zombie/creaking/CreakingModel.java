package changmin.changmin_villager_turret.feature.zombie.creaking;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class CreakingModel extends GeoModel<CreakingEntity> {
    @Override
    public ResourceLocation getModelResource(CreakingEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/creaking.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CreakingEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/creaking.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CreakingEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/creaking.animation.json");
    }
}
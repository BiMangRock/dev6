package changmin.changmin_villager_turret.feature.zombie.raged_angel_zombie;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class RagedShockwaveModel extends GeoModel<RagedShockwaveEntity> {
    @Override
    public ResourceLocation getModelResource(RagedShockwaveEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/raged_shockwave.geo.json");
    }
    @Override
    public ResourceLocation getTextureResource(RagedShockwaveEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/raged_shockwave.png");
    }
    @Override
    public ResourceLocation getAnimationResource(RagedShockwaveEntity animatable) {

        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/raged_shockwave.animation.json");
    }
}
package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class ShockwaveModel extends GeoModel<ShockwaveEntity> {
    @Override
    public ResourceLocation getModelResource(ShockwaveEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/shockwave.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ShockwaveEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/shockwave.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ShockwaveEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/shockwave.animation.json");
    }
}
package changmin.changmin_villager_turret.feature.hero.healer_hero;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class LoveProjectileModel extends GeoModel<LoveProjectileEntity> {
    @Override
    public ResourceLocation getModelResource(LoveProjectileEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/love.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LoveProjectileEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/love.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LoveProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/love.animation.json");
    }
}
package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class AngelZombieModel extends GeoModel<AngelZombieEntity> {
    @Override
    public ResourceLocation getModelResource(AngelZombieEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/angel_zombie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AngelZombieEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/angel_zombie.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AngelZombieEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/angel_zombie.animation.json");
    }
}
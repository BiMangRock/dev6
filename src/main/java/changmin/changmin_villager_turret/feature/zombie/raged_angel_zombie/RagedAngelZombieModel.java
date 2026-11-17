package changmin.changmin_villager_turret.feature.zombie.raged_angel_zombie;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class RagedAngelZombieModel extends GeoModel<RagedAngelZombieEntity> {
    @Override
    public ResourceLocation getModelResource(RagedAngelZombieEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/raged_angel_zombie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RagedAngelZombieEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/raged_angel_zombie.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RagedAngelZombieEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/raged_angel_zombie.animation.json");
    }
}
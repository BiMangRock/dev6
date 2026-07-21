package changmin.changmin_villager_turret.feature.zombie.raged_angel_zombie;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RagedAngelZombieModel extends AnimatedGeoModel<RagedAngelZombieEntity> {
    @Override
    public ResourceLocation getModelLocation(RagedAngelZombieEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/raged_angel_zombie.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(RagedAngelZombieEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/raged_angel_zombie.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(RagedAngelZombieEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/raged_angel_zombie.animation.json");
    }
}
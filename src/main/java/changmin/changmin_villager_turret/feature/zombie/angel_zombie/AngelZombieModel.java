package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AngelZombieModel extends AnimatedGeoModel<AngelZombieEntity> {
    @Override
    public ResourceLocation getModelLocation(AngelZombieEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/angel_zombie.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AngelZombieEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/angel_zombie.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AngelZombieEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/angel_zombie.animation.json");
    }
}
package changmin.changmin_villager_turret.feature.zombie.healer_zombie;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class HealerZombieModel extends GeoModel<HealerZombieEntity> {
    @Override
    public ResourceLocation getModelResource(HealerZombieEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/healer_zombie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HealerZombieEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/healer_zombie.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HealerZombieEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/healer_zombie.animation.json");
    }
}
package changmin.changmin_villager_turret.feature.zombie.healer_zombie;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class HealerZombieModel extends AnimatedGeoModel<HealerZombieEntity> {
    @Override
    public ResourceLocation getModelLocation(HealerZombieEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/healer_zombie.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(HealerZombieEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/healer_zombie.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(HealerZombieEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/healer_zombie.animation.json");
    }
}
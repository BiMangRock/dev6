package changmin.changmin_villager_turret.feature.turret.goddess_of_flame;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GoddessOfFlameModel extends AnimatedGeoModel<GoddessOfFlameEntity> {
    @Override
    public ResourceLocation getModelLocation(GoddessOfFlameEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/girl2.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(GoddessOfFlameEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/girl2.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(GoddessOfFlameEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/girl2.animation.json");
    }
}
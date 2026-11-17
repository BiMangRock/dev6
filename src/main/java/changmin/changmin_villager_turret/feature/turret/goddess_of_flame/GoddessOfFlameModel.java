package changmin.changmin_villager_turret.feature.turret.goddess_of_flame;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class GoddessOfFlameModel extends GeoModel<GoddessOfFlameEntity> {
    @Override
    public ResourceLocation getModelResource(GoddessOfFlameEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/girl2.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GoddessOfFlameEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/girl2.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GoddessOfFlameEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/girl2.animation.json");
    }
}
package changmin.changmin_villager_turret.feature.zombie.demon1;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class Demon1Model extends GeoModel<Demon1Entity> {
    @Override
    public ResourceLocation getModelResource(Demon1Entity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/demon1.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Demon1Entity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/demon1.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Demon1Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/demon1.animation.json");
    }
}
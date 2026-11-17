package changmin.changmin_villager_turret.feature.zombie.assassin2;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class Assassin2Model extends GeoModel<Assassin2Entity> {

    @Override
    public ResourceLocation getModelResource(Assassin2Entity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/assacine2.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Assassin2Entity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/assacine2.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Assassin2Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/assacine2.animation.json");
    }
}
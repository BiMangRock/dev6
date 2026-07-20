package changmin.changmin_villager_turret.feature.zombie.assassin2;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class Assassin2Model extends AnimatedGeoModel<Assassin2Entity> {

    @Override
    public ResourceLocation getModelLocation(Assassin2Entity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/assacine2.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(Assassin2Entity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/assacine2.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(Assassin2Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/assacine2.animation.json");
    }
}
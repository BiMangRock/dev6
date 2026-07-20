package changmin.myMod.feature.zombie.assassin2;

import changmin.myMod.feature.zombie.assassin2.Assassin2Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class Assassin2Model extends AnimatedGeoModel<Assassin2Entity> {

    @Override
    public ResourceLocation getModelLocation(Assassin2Entity object) {
        return ResourceLocation.fromNamespaceAndPath("mymod", "geo/assacine2.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(Assassin2Entity object) {
        return ResourceLocation.fromNamespaceAndPath("mymod", "textures/entity/assacine2.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(Assassin2Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath("mymod", "animations/assacine2.animation.json");
    }
}
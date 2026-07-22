package changmin.changmin_villager_turret.feature.zombie.demon1;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class Demon1Model extends AnimatedGeoModel<Demon1Entity> {
    @Override
    public ResourceLocation getModelLocation(Demon1Entity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/demon1.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(Demon1Entity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/demon1.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(Demon1Entity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/demon1.animation.json");
    }
}
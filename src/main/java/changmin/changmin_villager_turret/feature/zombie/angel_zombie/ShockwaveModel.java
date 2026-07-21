package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ShockwaveModel extends AnimatedGeoModel<ShockwaveEntity> {
    @Override
    public ResourceLocation getModelLocation(ShockwaveEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/shockwave.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ShockwaveEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/shockwave.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ShockwaveEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/shockwave.animation.json");
    }
}
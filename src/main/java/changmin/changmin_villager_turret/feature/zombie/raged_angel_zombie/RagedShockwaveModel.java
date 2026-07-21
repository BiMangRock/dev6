package changmin.changmin_villager_turret.feature.zombie.raged_angel_zombie;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RagedShockwaveModel extends AnimatedGeoModel<RagedShockwaveEntity> {
    @Override
    public ResourceLocation getModelLocation(RagedShockwaveEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/raged_shockwave.geo.json");
    }
    @Override
    public ResourceLocation getTextureLocation(RagedShockwaveEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/raged_shockwave.png");
    }
    @Override
    public ResourceLocation getAnimationFileLocation(RagedShockwaveEntity animatable) {

        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/raged_shockwave.animation.json");
    }
}
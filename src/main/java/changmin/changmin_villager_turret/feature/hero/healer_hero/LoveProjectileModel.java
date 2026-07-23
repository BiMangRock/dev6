package changmin.changmin_villager_turret.feature.hero.healer_hero;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class LoveProjectileModel extends AnimatedGeoModel<LoveProjectileEntity> {
    @Override
    public ResourceLocation getModelLocation(LoveProjectileEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/love.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(LoveProjectileEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/love.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(LoveProjectileEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/love.animation.json");
    }
}
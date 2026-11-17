package changmin.changmin_villager_turret.feature.hero.healer_hero;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class HealerHeroModel extends GeoModel<HealerHeroEntity> {
    @Override
    public ResourceLocation getModelResource(HealerHeroEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/hero_healer.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HealerHeroEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/hero_healer.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HealerHeroEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/hero_healer.animation.json");
    }
}
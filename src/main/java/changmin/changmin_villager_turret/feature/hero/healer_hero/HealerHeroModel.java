package changmin.changmin_villager_turret.feature.hero.healer_hero;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class HealerHeroModel extends AnimatedGeoModel<HealerHeroEntity> {
    @Override
    public ResourceLocation getModelLocation(HealerHeroEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/hero_healer.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(HealerHeroEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/hero_healer.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(HealerHeroEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/hero_healer.animation.json");
    }
}
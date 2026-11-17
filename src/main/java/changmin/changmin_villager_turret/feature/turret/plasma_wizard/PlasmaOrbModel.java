package changmin.changmin_villager_turret.feature.turret.plasma_wizard;

import software.bernie.geckolib.model.GeoModel;

import changmin.changmin_villager_turret.changmin_villager_turret;
import net.minecraft.resources.ResourceLocation;

public class PlasmaOrbModel extends GeoModel<PlasmaOrbEntity> {

    @Override
    public ResourceLocation getModelResource(PlasmaOrbEntity object) {
        // 💡 1.20.6+ 감가상각(Deprecation) 경고 해결을 위해 fromNamespaceAndPath 사용
        return ResourceLocation.fromNamespaceAndPath(changmin_villager_turret.MODID, "geo/plasma_orb.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PlasmaOrbEntity object) {
        return ResourceLocation.fromNamespaceAndPath(changmin_villager_turret.MODID, "textures/entity/plasma_orb.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PlasmaOrbEntity object) {
        return ResourceLocation.fromNamespaceAndPath(changmin_villager_turret.MODID, "animations/plasma_orb.animation.json");
    }
}
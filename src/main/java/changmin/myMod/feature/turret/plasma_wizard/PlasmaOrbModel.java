package changmin.myMod.feature.turret.plasma_wizard;

import changmin.myMod.MyMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PlasmaOrbModel extends AnimatedGeoModel<PlasmaOrbEntity> {

    @Override
    public ResourceLocation getModelLocation(PlasmaOrbEntity object) {
        // 💡 1.20.6+ 감가상각(Deprecation) 경고 해결을 위해 fromNamespaceAndPath 사용
        return ResourceLocation.fromNamespaceAndPath(MyMod.MODID, "geo/plasma_orb.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(PlasmaOrbEntity object) {
        return ResourceLocation.fromNamespaceAndPath(MyMod.MODID, "textures/entity/plasma_orb.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(PlasmaOrbEntity object) {
        return ResourceLocation.fromNamespaceAndPath(MyMod.MODID, "animations/plasma_orb.animation.json");
    }
}
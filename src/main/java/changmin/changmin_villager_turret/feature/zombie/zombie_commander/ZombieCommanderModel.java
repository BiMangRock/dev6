package changmin.changmin_villager_turret.feature.zombie.zombie_commander;

import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class ZombieCommanderModel extends GeoModel<ZombieCommanderEntity> {
    @Override
    public ResourceLocation getModelResource(ZombieCommanderEntity object) {
        // 파일 이름을 zombie_commander.geo.json으로 저장하세요.
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/zombie_commander.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZombieCommanderEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/zombie_commander.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZombieCommanderEntity animatable) {
        // 파일 이름을 zombie_commander.animation.json으로 저장하세요.
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/zombie_commander.animation.json");
    }
}
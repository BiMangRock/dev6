package changmin.changmin_villager_turret.feature.zombie.zombie_commander;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ZombieCommanderModel extends AnimatedGeoModel<ZombieCommanderEntity> {
    @Override
    public ResourceLocation getModelLocation(ZombieCommanderEntity object) {
        // 파일 이름을 zombie_commander.geo.json으로 저장하세요.
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "geo/zombie_commander.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ZombieCommanderEntity object) {
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "textures/entity/zombie_commander.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ZombieCommanderEntity animatable) {
        // 파일 이름을 zombie_commander.animation.json으로 저장하세요.
        return ResourceLocation.fromNamespaceAndPath("changmin_villager_turret", "animations/zombie_commander.animation.json");
    }
}
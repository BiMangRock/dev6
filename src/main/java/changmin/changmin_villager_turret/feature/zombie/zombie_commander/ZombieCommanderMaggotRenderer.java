package changmin.changmin_villager_turret.feature.zombie.zombie_commander;

import net.minecraft.client.model.EndermiteModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ZombieCommanderMaggotRenderer extends MobRenderer<ZombieCommanderMaggotEntity, EndermiteModel<ZombieCommanderMaggotEntity>> {

    // 💡 생성자 대신 fromNamespaceAndPath 메서드를 사용하여 경고를 해결합니다.
    private static final ResourceLocation VANILLA_ENDERMITE_LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/endermite.png");

    public ZombieCommanderMaggotRenderer(EntityRendererProvider.Context context) {
        super(context, new EndermiteModel<>(context.bakeLayer(ModelLayers.ENDERMITE)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(ZombieCommanderMaggotEntity entity) {
        return VANILLA_ENDERMITE_LOCATION;
    }
}
package changmin.changmin_villager_turret.feature.turret.store;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TurretStoreRenderer extends MobRenderer<TurretStoreEntity, VillagerModel<TurretStoreEntity>> {
    // 💡 [수정됨] 1.20.6+ 이상 표준 대응을 위해 ResourceLocation.parse() 메서드를 사용해 객체를 생성합니다.
    private static final ResourceLocation VILLAGER_BASE_SKIN = ResourceLocation.parse("textures/entity/villager/villager.png");

    public TurretStoreRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(TurretStoreEntity entity) {
        return VILLAGER_BASE_SKIN;
    }
}
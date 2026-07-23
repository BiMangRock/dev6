package changmin.changmin_villager_turret.feature.hero.healer_hero;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class LoveProjectileRenderer extends GeoProjectilesRenderer<LoveProjectileEntity> {
    public LoveProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new LoveProjectileModel());
        this.shadowRadius = 0.0F; // 투사체 그림자 크기 비활성화
    }
}
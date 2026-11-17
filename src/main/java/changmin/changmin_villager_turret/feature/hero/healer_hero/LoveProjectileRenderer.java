package changmin.changmin_villager_turret.feature.hero.healer_hero;

import changmin.changmin_villager_turret.client.GeoProjectileRenderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class LoveProjectileRenderer extends GeoProjectileRenderer<LoveProjectileEntity> {
    public LoveProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new LoveProjectileModel());
        this.shadowRadius = 0.0F; // 투사체 그림자 크기 비활성화
    }
}
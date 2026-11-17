package changmin.changmin_villager_turret.feature.zombie.assassin2;

import changmin.changmin_villager_turret.client.GeoProjectileRenderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SwordGhoulRenderer extends GeoProjectileRenderer<SwordGhoulEntity> {

    public SwordGhoulRenderer(EntityRendererProvider.Context context) {
        // 위에서 만든 SwordGhoulModel을 등록합니다.
        super(context, new SwordGhoulModel());
        // 투사체 그림자 크기 (필요 없으면 0.0F)
        this.shadowRadius = 0.0F;
    }
}
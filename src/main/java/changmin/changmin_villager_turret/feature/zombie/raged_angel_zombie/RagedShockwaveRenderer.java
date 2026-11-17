package changmin.changmin_villager_turret.feature.zombie.raged_angel_zombie;

import changmin.changmin_villager_turret.client.GeoProjectileRenderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class RagedShockwaveRenderer extends GeoProjectileRenderer<RagedShockwaveEntity> {
    public RagedShockwaveRenderer(EntityRendererProvider.Context context) {
        super(context, new RagedShockwaveModel());
    }
}
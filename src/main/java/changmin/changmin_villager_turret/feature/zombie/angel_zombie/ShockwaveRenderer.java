package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import changmin.changmin_villager_turret.client.GeoProjectileRenderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ShockwaveRenderer extends GeoProjectileRenderer<ShockwaveEntity> {
    public ShockwaveRenderer(EntityRendererProvider.Context context) {
        super(context, new ShockwaveModel());
    }
}
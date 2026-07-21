package changmin.changmin_villager_turret.feature.zombie.raged_angel_zombie;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class RagedShockwaveRenderer extends GeoProjectilesRenderer<RagedShockwaveEntity> {
    public RagedShockwaveRenderer(EntityRendererProvider.Context context) {
        super(context, new RagedShockwaveModel());
    }
}
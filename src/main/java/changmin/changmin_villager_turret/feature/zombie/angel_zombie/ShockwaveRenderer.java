package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class ShockwaveRenderer extends GeoProjectilesRenderer<ShockwaveEntity> {
    public ShockwaveRenderer(EntityRendererProvider.Context context) {
        super(context, new ShockwaveModel());
    }
}
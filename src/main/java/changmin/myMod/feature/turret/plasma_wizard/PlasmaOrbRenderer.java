package changmin.myMod.feature.turret.plasma_wizard;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class PlasmaOrbRenderer extends GeoProjectilesRenderer<PlasmaOrbEntity> {
    public PlasmaOrbRenderer(EntityRendererProvider.Context context) {
        super(context, new PlasmaOrbModel());
    }
}
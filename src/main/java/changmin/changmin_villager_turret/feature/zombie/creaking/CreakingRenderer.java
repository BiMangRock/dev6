package changmin.changmin_villager_turret.feature.zombie.creaking;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class CreakingRenderer extends GeoEntityRenderer<CreakingEntity> {
    public CreakingRenderer(EntityRendererProvider.Context context) {
        super(context, new CreakingModel());
        this.shadowRadius = 0.5F;
    }
}
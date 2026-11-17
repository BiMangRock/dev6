package changmin.changmin_villager_turret.feature.zombie.creaking;

import software.bernie.geckolib.renderer.GeoEntityRenderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CreakingRenderer extends GeoEntityRenderer<CreakingEntity> {
    public CreakingRenderer(EntityRendererProvider.Context context) {
        super(context, new CreakingModel());
        this.shadowRadius = 0.5F;
    }
}
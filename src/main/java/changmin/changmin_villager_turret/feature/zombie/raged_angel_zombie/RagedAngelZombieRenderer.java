package changmin.changmin_villager_turret.feature.zombie.raged_angel_zombie;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class RagedAngelZombieRenderer extends GeoEntityRenderer<RagedAngelZombieEntity> {
    public RagedAngelZombieRenderer(EntityRendererProvider.Context context) {
        super(context, new RagedAngelZombieModel());
        this.shadowRadius = 0.4F;
    }
}
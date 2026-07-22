package changmin.changmin_villager_turret.feature.zombie.demon1;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class Demon1Renderer extends GeoEntityRenderer<Demon1Entity> {
    public Demon1Renderer(EntityRendererProvider.Context context) {
        super(context, new Demon1Model());
        this.shadowRadius = 0.4F;
    }
}
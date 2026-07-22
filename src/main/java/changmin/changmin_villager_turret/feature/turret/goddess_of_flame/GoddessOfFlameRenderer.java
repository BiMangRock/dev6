package changmin.changmin_villager_turret.feature.turret.goddess_of_flame;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class GoddessOfFlameRenderer extends GeoEntityRenderer<GoddessOfFlameEntity> {
    public GoddessOfFlameRenderer(EntityRendererProvider.Context context) {
        super(context, new GoddessOfFlameModel());
        this.shadowRadius = 0.4F;
    }
}
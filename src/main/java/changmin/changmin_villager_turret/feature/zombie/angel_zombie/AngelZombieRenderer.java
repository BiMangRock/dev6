package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import software.bernie.geckolib.renderer.GeoEntityRenderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class AngelZombieRenderer extends GeoEntityRenderer<AngelZombieEntity> {
    public AngelZombieRenderer(EntityRendererProvider.Context context) {
        super(context, new AngelZombieModel());
        this.shadowRadius = 0.4F;
    }
}
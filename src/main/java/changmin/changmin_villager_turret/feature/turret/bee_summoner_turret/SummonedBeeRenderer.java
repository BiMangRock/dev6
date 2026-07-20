package changmin.changmin_villager_turret.feature.turret.bee_summoner_turret;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SummonedBeeRenderer extends MobRenderer<SummonedBeeEntity, SummonedBeeModel> {

    private static final ResourceLocation BEE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/bee/bee.png");

    public SummonedBeeRenderer(EntityRendererProvider.Context context) {
        // ModelLayers.BEE 레이아웃을 통해 얻은 꿀벌 뼈대를 새롭게 작성한 SummonedBeeModel에 안정적으로 주입
        super(context, new SummonedBeeModel(context.bakeLayer(ModelLayers.BEE)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(SummonedBeeEntity entity) {
        return BEE_TEXTURE;
    }
}
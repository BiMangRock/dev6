package changmin.myMod.feature.zombie.assassin2;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class Assassin2Renderer extends GeoEntityRenderer<Assassin2Entity> {

    public Assassin2Renderer(EntityRendererProvider.Context context) {
        super(context, new Assassin2Model());
        this.shadowRadius = 0.5F; // 몹 밑에 생기는 그림자 크기
    }

    @Override
    public ResourceLocation getTextureLocation(Assassin2Entity instance) {
        return ResourceLocation.fromNamespaceAndPath("mymod", "textures/entity/assacine2.png");
    }
}
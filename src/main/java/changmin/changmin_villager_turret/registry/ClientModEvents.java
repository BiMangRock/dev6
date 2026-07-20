package changmin.changmin_villager_turret.registry;

import changmin.changmin_villager_turret.changmin_villager_turret;
import changmin.changmin_villager_turret.feature.turret.villager_turret.VillagerTurretRenderer;
import changmin.changmin_villager_turret.feature.zombie.zombie1.ZombieBossRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// value = Dist.CLIENT를 주어 클라이언트 환경에서만 이 클래스가 작동하도록 분리합니다.
@Mod.EventBusSubscriber(modid = changmin_villager_turret.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 1. 좀비 보스용 렌더러 등록
        event.registerEntityRenderer(ModEntityTypes.ZOMBIE_BOSS.get(), ZombieBossRenderer::new);

        // 2. 주민 터렛용 렌더러 등록 (혹시 아직 다른 곳에 등록되지 않았다면 안전하게 함께 선언합니다)
        event.registerEntityRenderer(ModEntityTypes.VILLAGER_TURRET.get(), VillagerTurretRenderer::new);
    }
}
package changmin.myMod.registry;

import changmin.myMod.MyMod;
import changmin.myMod.feature.zombie.zombie1.ZombieBossEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// bus = Mod.EventBusSubscriber.Bus.MOD 임을 반드시 확인해야 합니다. (MOD 버스 사용)
@Mod.EventBusSubscriber(modid = MyMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        // 좀비 보스의 능력치 정보를 포지 엔진에 정식 등록합니다.
        event.put(ModEntityTypes.ZOMBIE_BOSS.get(), ZombieBossEntity.createAttributes().build());
    }
}
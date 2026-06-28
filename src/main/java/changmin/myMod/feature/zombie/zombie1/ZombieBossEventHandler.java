package changmin.myMod.feature.zombie.zombie1;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "mymod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ZombieBossEventHandler {

    // 오직 zombie1(ZombieBossEntity)만 감지하여 레벨업 및 킬 스코어를 누적시킵니다.
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        if (killer instanceof ZombieBossEntity boss) {
            boss.recordKill();
        }
    }
}
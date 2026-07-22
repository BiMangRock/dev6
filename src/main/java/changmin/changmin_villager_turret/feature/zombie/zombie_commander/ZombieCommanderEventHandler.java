package changmin.changmin_villager_turret.feature.zombie.zombie_commander;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "changmin_villager_turret", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ZombieCommanderEventHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        // 커맨더 좀비가 직접 처치했을 때도 포인트(Attack XP)를 줍니다.
        if (killer instanceof ZombieCommanderEntity commander) {
            commander.recordKill();
        }
    }
}
package changmin.myMod.feature.zombie.assassin2;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "mymod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Assassin2EventHandler {

    // 오직 assassin2(Assassin2Entity)가 적을 죽였을 때만 감지하여 킬 경험치를 누적시킵니다.
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        if (killer instanceof Assassin2Entity assassin) {
            assassin.recordKill();
        }
    }
}
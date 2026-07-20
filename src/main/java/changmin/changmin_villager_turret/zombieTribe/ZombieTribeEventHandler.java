package changmin.changmin_villager_turret.zombieTribe;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "changmin_villager_turret", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ZombieTribeEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntityLiving();
        Entity attacker = event.getSource().getEntity();

        if (attacker instanceof LivingEntity livingAttacker) {
            if (IZombieTribe.isZombieTribe(victim) && IZombieTribe.isZombieTribe(livingAttacker)) {
                event.setAmount(0.0F);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity attacker = event.getEntityLiving();
        LivingEntity newTarget = event.getNewTarget();

        if (IZombieTribe.isZombieTribe(attacker) && newTarget != null) {
            if (IZombieTribe.isZombieTribe(newTarget)) {
                event.setCanceled(true);
            }
        }
    }
}
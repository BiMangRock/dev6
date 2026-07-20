package changmin.changmin_villager_turret.ally;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent; // 신형 이벤트로 변경
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "changmin_villager_turret", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AllyEventHandler {

    // 🛡️ 아군끼리(혹은 플레이어와의 사이에서) 오가는 모든 피해량을 0으로 차단 및 이벤트 취소
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntityLiving();
        Entity attacker = event.getSource().getEntity();

        if (attacker != null) {
            // Case 1: 맞은 놈도 아군이고 때린 놈도 아군인 경우
            if (victim instanceof IAlly && attacker instanceof IAlly) {
                event.setAmount(0.0F);
                event.setCanceled(true);
            }
            // Case 2: 맞은 놈은 아군인데 플레이어가 실수로 쏜 경우 (플레이어 오사 방지)
            else if (victim instanceof IAlly && attacker instanceof Player) {
                event.setAmount(0.0F);
                event.setCanceled(true);
            }
            // Case 3: 맞은 놈은 플레이어인데 아군 몹이 실수로 오인 사격한 경우
            else if (victim instanceof Player && attacker instanceof IAlly) {
                event.setAmount(0.0F);
                event.setCanceled(true);
            }
        }
    }

    // 🎯 신형 이벤트를 이용해 아군끼리 타겟 지정을 시도할 때 원천 차단
    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity attacker = event.getEntityLiving();
        LivingEntity newTarget = event.getNewTarget(); // 새로 설정되려는 타겟팅 대상

        // 타겟팅을 시도하려는 주체가 아군이고, 지정하려는 대상이 있는 경우
        if (attacker instanceof IAlly && newTarget != null) {
            // 대상이 아군이거나 플레이어라면 타겟 변경 이벤트 자체를 취소(Canceled)하여 무효화!
            if (newTarget instanceof IAlly || newTarget instanceof Player) {
                event.setCanceled(true);
            }
        }
    }
}
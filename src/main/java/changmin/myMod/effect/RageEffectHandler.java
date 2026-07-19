package changmin.myMod.effect;

import changmin.myMod.MyMod;
import changmin.myMod.ally.IAlly;
import changmin.myMod.registry.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent; // 👈 포지 타겟 변경 이벤트
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RageEffectHandler {

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Mob mob && !mob.level.isClientSide) {
            // 💡 좀비가 분노(RAGE) 효과에 걸려 있는 동안에만 작동합니다.
            if (mob.hasEffect(ModEffects.RAGE.get())) {
                LivingEntity newTarget = event.getNewTarget();

                // 바닐라 AI가 타겟을 갑자기 null로 지워버리거나, 플레이어 또는 아군 터렛을 조준하려 할 때 차단
                if (newTarget == null || newTarget instanceof Player || newTarget instanceof IAlly) {
                    LivingEntity closestHostile = findClosestHostile(mob);
                    if (closestHostile != null) {
                        // 💡 포지 이벤트를 가로채서 강제로 겹쳐있는 다른 적대적 좀비로 타겟을 영구 고정시킵니다.
                        event.setNewTarget(closestHostile);
                    }
                }
            }
        }
    }

    // RageEffect와 동일한 기준으로 가장 가까운 적대적 몹을 수색하는 헬퍼 메서드
    private static LivingEntity findClosestHostile(Mob mob) {
        AABB area = mob.getBoundingBox().inflate(10.0D);
        List<LivingEntity> hostiles = mob.level.getEntitiesOfClass(LivingEntity.class, area, target ->
                target instanceof Enemy
                        && !(target instanceof Player)
                        && !(target instanceof IAlly)
                        && target != mob
                        && target.isAlive()
        );

        LivingEntity closestHostile = null;
        double closestDist = Double.MAX_VALUE;

        for (LivingEntity target : hostiles) {
            double dist = mob.distanceToSqr(target);
            if (dist < closestDist) {
                closestDist = dist;
                closestHostile = target;
            }
        }
        return closestHostile;
    }
}
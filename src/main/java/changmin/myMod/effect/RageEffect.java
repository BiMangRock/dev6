package changmin.myMod.effect;

import changmin.myMod.ally.IAlly;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class RageEffect extends MobEffect {
    public RageEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF3333);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Mob mob && !mob.level.isClientSide) {
            // 💡 [개선]: 까다로운 내장 탐색기를 우회하고, 범위 내의 적대적 몹들을 수동으로 확실하게 검출합니다.
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

            if (closestHostile != null) {
                mob.setTarget(closestHostile);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0; // 0.5초 주기로 작동
    }
}
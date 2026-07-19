package changmin.myMod.effect;

import changmin.myMod.ally.IAlly;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster; // 👈 Enemy 대신 Monster 임포트
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
            AABB area = mob.getBoundingBox().inflate(10.0D);

            List<Monster> hostiles = mob.level.getEntitiesOfClass(Monster.class, area, target ->
                    !(target instanceof IAlly)        // 1. 플레이어 조건은 빼고, 아군 주민 터렛만 제외
                            && target != mob                 // 2. 자기 자신 제외
                            && target.isAlive()                  // 3. 살아있는 대상만
            );

            LivingEntity closestHostile = null;
            double closestDist = Double.MAX_VALUE;

            for (Monster target : hostiles) {
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
        return duration % 10 == 0;
    }
}
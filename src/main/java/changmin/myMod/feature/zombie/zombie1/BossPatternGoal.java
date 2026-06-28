package changmin.myMod.feature.zombie.zombie1;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class BossPatternGoal extends Goal {
    private final ZombieBossEntity boss;
    private int patternCooldown = 0;

    public BossPatternGoal(ZombieBossEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.boss.getTarget() != null && this.boss.getTarget().isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = this.boss.getTarget();
        if (target != null) {
            // 타겟 응시
            this.boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distanceSq = this.boss.distanceToSqr(target);

            if (this.patternCooldown > 0) {
                this.patternCooldown--;
            }

            // 쿨타임이 끝났을 때 거리에 맞춰 알맞은 패턴 시작 트리거 호출
            if (this.boss.getActiveAttack() == 0 && this.patternCooldown <= 0) {
                if (distanceSq < 25.0D) {
                    this.boss.triggerAttack3();
                    this.patternCooldown = 120; // 6초 대기
                } else if (distanceSq < 400.0D) {
                    this.boss.triggerAttack1();
                    this.patternCooldown = 80;  // 4초 대기
                }
            }
        }
    }
}
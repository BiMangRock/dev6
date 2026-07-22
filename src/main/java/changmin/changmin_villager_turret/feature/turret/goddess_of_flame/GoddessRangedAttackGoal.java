package changmin.changmin_villager_turret.feature.turret.goddess_of_flame;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

public class GoddessRangedAttackGoal extends Goal {
    private final IGoddessAttack goddess;
    private int attackDelay = 40;

    public GoddessRangedAttackGoal(IGoddessAttack goddess) {
        this.goddess = goddess;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return goddess.asMonster().getTarget() != null && goddess.asMonster().getTarget().isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = goddess.asMonster().getTarget();
        if (target == null) return;

        double distanceSq = goddess.asMonster().distanceToSqr(target);
        goddess.asMonster().getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 일정 거리(16블록 내) 안으로 들어오면 제자리에 정지하여 폭격을 가함
        if (distanceSq < 256.0D) {
            goddess.asMonster().getNavigation().stop();

            if (attackDelay > 0) {
                attackDelay--;
            }

            if (attackDelay <= 0) {
                goddess.startAttackSequence(target);
                // 4초 주기 (2초 공격 지속 + 2초 휴식)
                attackDelay = 80;
            }
        } else {
            // 거리가 멀면 타겟 방향으로 접근
            goddess.asMonster().getNavigation().moveTo(target, 1.1D);
        }
    }
}
package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

public class AngelRangedAttackGoal extends Goal {
    // 💡 특정 엔티티 대신 인터페이스 사용
    private final IAngelAttack angel;
    private int attackDelay = 40;

    public AngelRangedAttackGoal(IAngelAttack angel) {
        this.angel = angel;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return angel.asMonster().getTarget() != null && angel.asMonster().getTarget().isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = angel.asMonster().getTarget();
        if (target == null) return;

        // 모든 entity 호출을 angel.asMonster()로 변경
        double distanceSq = angel.asMonster().distanceToSqr(target.getX(), angel.asMonster().getY(), target.getZ());
        angel.asMonster().getLookControl().setLookAt(target, 30.0F, 30.0F);

        double desiredY = target.getY() + 6.5D;

        if (angel.asMonster().getY() < desiredY) {
            angel.asMonster().setDeltaMovement(angel.asMonster().getDeltaMovement().add(0, 0.05, 0));
        } else if (angel.asMonster().getY() > desiredY + 2.0D) {
            angel.asMonster().setDeltaMovement(angel.asMonster().getDeltaMovement().add(0, -0.02, 0));
        }

        if (distanceSq < 400.0D) {
            if (attackDelay > 0) attackDelay--;

            if (attackDelay <= 0) {
                // 💡 수정된 부분: performDonutAttack 대신 startAttackSequence를 호출합니다.
                // 이렇게 하면 일반 천사는 1발을 쏘고, 분노한 천사는 3발을 쏩니다.
                angel.startAttackSequence(target);

                // 분노한 천사의 3연발 시간(1초 이상)을 고려하여 쿨타임을 넉넉히 줍니다.
                attackDelay = 100;
            }

            if (distanceSq < 16.0D) {
                angel.asMonster().getNavigation().stop();
            }
        else {
            angel.asMonster().getNavigation().moveTo(target.getX(), desiredY, target.getZ(), 1.0D);
        }
    }
}
}
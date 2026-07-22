package changmin.changmin_villager_turret.feature.zombie.demon1;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

public class Demon1RangedAttackGoal extends Goal {
    private final IDemon1Attack demon;
    private int attackDelay = 40;

    public Demon1RangedAttackGoal(IDemon1Attack demon) {
        this.demon = demon;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return demon.asMonster().getTarget() != null && demon.asMonster().getTarget().isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = demon.asMonster().getTarget();
        if (target == null) return;

        double distanceSq = demon.asMonster().distanceToSqr(target.getX(), demon.asMonster().getY(), target.getZ());
        demon.asMonster().getLookControl().setLookAt(target, 30.0F, 30.0F);

        double desiredY = target.getY() + 6.5D;

        if (demon.asMonster().getY() < desiredY) {
            demon.asMonster().setDeltaMovement(demon.asMonster().getDeltaMovement().add(0, 0.05, 0));
        } else if (demon.asMonster().getY() > desiredY + 2.0D) {
            demon.asMonster().setDeltaMovement(demon.asMonster().getDeltaMovement().add(0, -0.02, 0));
        }

        if (distanceSq < 400.0D) {
            if (attackDelay > 0) {
                attackDelay--;
            }

            if (attackDelay <= 0) {
                demon.startAttackSequence(target);
                // 악마의 전체 쿨타임 (2초 공격 동작 + 다음 공격 대기 1초)
                attackDelay = 60;
            }

            if (distanceSq < 16.0D) {
                demon.asMonster().getNavigation().stop();
            } else {
                demon.asMonster().getNavigation().moveTo(target.getX(), desiredY, target.getZ(), 1.0D);
            }
        } else {
            demon.asMonster().getNavigation().moveTo(target.getX(), desiredY, target.getZ(), 1.0D);
        }
    }
}
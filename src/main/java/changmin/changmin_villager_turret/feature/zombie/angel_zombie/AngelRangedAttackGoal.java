package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public class AngelRangedAttackGoal extends Goal {
    private final AngelZombieEntity entity;
    private int attackDelay = 40;

    public AngelRangedAttackGoal(AngelZombieEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return entity.getTarget() != null && entity.getTarget().isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = entity.getTarget();
        if (target == null) return;

        double distanceSq = entity.distanceToSqr(target.getX(), entity.getY(), target.getZ());
        entity.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 💡 [수정] 고공 비행 유지 로직: 타겟의 Y축 기준 +6.5블록 높이를 목표로 함
        double desiredY = target.getY() + 6.5D;

        if (entity.getY() < desiredY) {
            // 목표 높이보다 낮으면 상승
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.05, 0));
        } else if (entity.getY() > desiredY + 2.0D) {
            // 너무 높으면 살짝 하강
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, -0.02, 0));
        }

        // 수평 거리 조절 (대상의 머리 위 4~15블록 내에서 머묾)
        if (distanceSq < 225.0D) { // 15블록 이내
            if (attackDelay > 0) attackDelay--;
            if (attackDelay <= 0) {
                entity.performRangedAttack(target);
                attackDelay = 50;
            }
            // 너무 가까워지면(수평 거리 4블록 미만) 이동 정지
            if (distanceSq < 16.0D) {
                entity.getNavigation().stop();
            }
        } else {
            // 너무 멀면 대상의 머리 위 허공으로 이동
            entity.getNavigation().moveTo(target.getX(), desiredY, target.getZ(), 1.0D);
        }
    }
}
package changmin.changmin_villager_turret.feature.zombie.Apostle_of_the_End;

import changmin.changmin_villager_turret.feature.zombie.angel_zombie.AngelZombieArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public class ApostleCirclingAttackGoal extends Goal {
    private final ApostleOfTheEndEntity entity;
    private int attackDelay = 20;
    private float circlingAngle = 0; // 원형 이동을 위한 각도
    private boolean clockwise = true; // 회전 방향

    public ApostleCirclingAttackGoal(ApostleOfTheEndEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return entity.getTarget() != null && entity.getTarget().isAlive();
    }

    @Override
    public void start() {
        this.clockwise = entity.getRandom().nextBoolean();
        this.circlingAngle = (float) Math.atan2(entity.getZ() - entity.getTarget().getZ(), entity.getX() - entity.getTarget().getX());
    }

    @Override
    public void tick() {
        LivingEntity target = entity.getTarget();
        if (target == null) return;

        double distSq = entity.distanceToSqr(target);
        entity.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 1. 공격 로직
        if (attackDelay > 0) attackDelay--;
        if (attackDelay <= 0) {
            entity.attackTimer = 20;
            performAttack(target);
            attackDelay = 60 + entity.getRandom().nextInt(20);
        }

        // 2. 맴돌기 이동 로직
        double desiredDist = 12.0D; // 유지하고 싶은 거리 (12블록)

        // 각도 업데이트 (회전)
        if (clockwise) circlingAngle += 0.05F;
        else circlingAngle -= 0.05F;

        // 원형 좌표 계산
        double targetX = target.getX() + Math.cos(circlingAngle) * desiredDist;
        double targetZ = target.getZ() + Math.sin(circlingAngle) * desiredDist;

        // 타겟과의 거리에 따라 원으로 접근하거나 유지
        if (distSq > 400.0D) { // 20블록보다 멀면 타겟에게 직접 접근
            entity.getNavigation().moveTo(target, 1.2D);
        } else {
            // 사거리 안이라면 타겟 주위의 계산된 좌표로 이동 (맴돌기)
            entity.getNavigation().moveTo(targetX, target.getY(), targetZ, 1.0D);
        }
    }

    private void performAttack(LivingEntity target) {
        Vec3 lookVec = entity.getViewVector(1.0F);
        Vec3 startPos = entity.position().add(0, 1.5, 0).subtract(lookVec.scale(0.5D));
        int arrowCount = entity.getSummonedAngelsCount() + 1;

        for (int i = 0; i < arrowCount; i++) {
            AngelZombieArrow arrow = new AngelZombieArrow(entity.level, entity);
            arrow.setPos(startPos.x, startPos.y, startPos.z);
            double d0 = target.getX() - startPos.x;
            double d1 = target.getY(0.33D) - startPos.y;
            double d2 = target.getZ() - startPos.z;
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            arrow.shoot(d0, d1 + d3 * 0.15D, d2, 1.6F, 3.0F); // 조금 더 퍼지게
            entity.level.addFreshEntity(arrow);
        }
    }
}
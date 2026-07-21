package changmin.changmin_villager_turret.feature.zombie.Apostle_of_the_End;

import changmin.changmin_villager_turret.feature.zombie.angel_zombie.AngelZombieArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public class ApostleRangedAttackGoal extends Goal {
    private final ApostleOfTheEndEntity entity;
    private int attackDelay = 20;

    public ApostleRangedAttackGoal(ApostleOfTheEndEntity entity) {
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

        double distSq = entity.distanceToSqr(target);
        entity.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (distSq < 400.0D) { // 약 20블록 이내
            if (attackDelay > 0) attackDelay--;

            if (attackDelay <= 0) {
                entity.attackTimer = 20; // 공격 애니메이션 실행

                // 1. 화살 출발 위치 계산 (몸통 뒤쪽 상단)
                Vec3 lookVec = entity.getViewVector(1.0F); // 바라보는 방향 벡터
                // 현재 위치 + 높이 보정(1.2) - (바라보는 방향 * 0.5) => 즉, 뒤로 0.5블록
                Vec3 startPos = entity.position().add(0, 1.2, 0).subtract(lookVec.scale(0.5D));

                // 2. 화살 개수 결정 (살아있는 천사 수 + 1)
                int arrowCount = entity.getSummonedAngelsCount() + 1;

                // 3. 화살 다중 발사
                for (int i = 0; i < arrowCount; i++) {
                    shootArrow(target, startPos);
                }

                attackDelay = 50; // 다음 공격까지 약 2.5초 대기
            }
            if (distSq < 64.0D) { // 너무 가까우면 정지
                entity.getNavigation().stop();
            }
        } else {
            entity.getNavigation().moveTo(target, 1.0D);
        }
    }

    private void shootArrow(LivingEntity target, Vec3 startPos) {
        AngelZombieArrow arrow = new AngelZombieArrow(entity.level, entity);

        // 화살의 시작 위치를 계산된 뒤쪽 위치로 강제 설정
        arrow.setPos(startPos.x, startPos.y, startPos.z);

        double d0 = target.getX() - startPos.x;
        double d1 = target.getY(0.33D) - startPos.y;
        double d2 = target.getZ() - startPos.z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        // shoot(x, y, z, 속도, 부정확도)
        // 부정확도(inaccuracy)를 2.0F 정도로 주어 여러 발이 퍼지게 함
        arrow.shoot(d0, d1 + d3 * 0.15D, d2, 1.6F, 2.0F);

        entity.level.addFreshEntity(arrow);
    }
}
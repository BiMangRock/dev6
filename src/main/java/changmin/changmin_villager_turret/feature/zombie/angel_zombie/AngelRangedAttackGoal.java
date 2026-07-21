package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

public class AngelRangedAttackGoal extends Goal {
    private final AngelZombieEntity entity;
    private int attackDelay = 40; // 초기 대기 시간

    public AngelRangedAttackGoal(AngelZombieEntity entity) {
        this.entity = entity;
        // 이동과 바라보기를 동시에 수행하도록 플래그 설정
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 타겟이 있고 살아있을 때만 실행
        return entity.getTarget() != null && entity.getTarget().isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = entity.getTarget();
        if (target == null) return;

        // 1. 타겟 바라보기
        entity.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distanceSq = entity.distanceToSqr(target.getX(), entity.getY(), target.getZ());

        // 2. 공중 고도 유지 로직 (타겟보다 약 6.5블록 위 유지)
        double desiredY = target.getY() + 6.5D;
        if (entity.getY() < desiredY) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.05, 0));
        } else if (entity.getY() > desiredY + 2.0D) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, -0.02, 0));
        }

        // 3. 거리 기반 이동 및 공격 로직
        // 약 20블록(20^2 = 400) 이내일 때 공격 준비
        if (distanceSq < 400.0D) {
            if (attackDelay > 0) {
                attackDelay--;
            }

            if (attackDelay <= 0) {
                // 도넛 공격 수행 (이제 투사체이므로 타겟 정보를 넘김)
                entity.performDonutAttack(target);

                // 애니메이션이 4초(80틱)이므로, 공격 주기를 4~5초로 설정
                // 100틱 = 5초 (공격 후 1초 정도의 여유를 둠)
                attackDelay = 100;
            }

            // 너무 가까우면 정지 (적당한 거리 유지)
            if (distanceSq < 16.0D) {
                entity.getNavigation().stop();
            } else {
                entity.getNavigation().moveTo(target.getX(), desiredY, target.getZ(), 1.0D);
            }
        } else {
            // 거리가 멀면 타겟 방향으로 비행 이동
            entity.getNavigation().moveTo(target.getX(), desiredY, target.getZ(), 1.0D);
        }
    }

    @Override
    public void stop() {
        // 목표가 사라졌을 때 초기화
        this.attackDelay = 40;
    }
}
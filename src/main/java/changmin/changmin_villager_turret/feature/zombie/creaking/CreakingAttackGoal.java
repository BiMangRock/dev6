package changmin.changmin_villager_turret.feature.zombie.creaking;

import changmin.changmin_villager_turret.feature.zombie.angel_zombie.AngelZombieArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

public class CreakingAttackGoal extends Goal {
    private final CreakingEntity entity;
    private int attackTicks = 0;       // 공격 진행 타이머 (25틱 = 1.25초)
    private int cooldownTicks = 40;     // 공격 간 대기 쿨타임
    private float circlingAngle = 0;    // 원형 맴돌기 각도
    private boolean clockwise = true;   // 회전 방향 플래그

    public CreakingAttackGoal(CreakingEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = entity.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        this.clockwise = entity.getRandom().nextBoolean();
        LivingEntity target = entity.getTarget();
        if (target != null) {
            this.circlingAngle = (float) Math.atan2(entity.getZ() - target.getZ(), entity.getX() - target.getX());
        }
    }

    @Override
    public void tick() {
        LivingEntity target = entity.getTarget();
        if (target == null) return;

        entity.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distSq = entity.distanceToSqr(target);

        if (this.attackTicks > 0) {
            // 💡 1. 공격 수행 중 (총 1.25초 = 25틱)
            this.attackTicks--;
            entity.getNavigation().stop(); // 경로 탐색 정지

            // 💡 [물리 정지 제어]: 수평 속도(X, Z)를 강제로 0으로 잠가 관성 슬라이딩 및 걷기 동작을 원천 차단합니다.
            entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);

            // 0.5초 준비 후, 남은 15틱 동안 연사
            if (this.attackTicks <= 15) {
                if (this.attackTicks % 3 == 0) {
                    shootArrow(target);
                }
            }

            if (this.attackTicks <= 0) {
                this.cooldownTicks = 60 + entity.getRandom().nextInt(20);
            }
        } else {
            // 💡 2. 공격 쿨타임 중: 타겟 주변 원형 맴돌기 로직 실행
            if (this.cooldownTicks > 0) {
                this.cooldownTicks--;
            }

            double desiredDist = 10.0D;
            if (clockwise) circlingAngle += 0.04F;
            else circlingAngle -= 0.04F;

            double targetX = target.getX() + Math.cos(circlingAngle) * desiredDist;
            double targetZ = target.getZ() + Math.sin(circlingAngle) * desiredDist;

            if (distSq > 400.0D) {
                entity.getNavigation().moveTo(target, 1.2D);
            } else {
                entity.getNavigation().moveTo(targetX, target.getY(), targetZ, 1.0D);
            }

            // 쿨타임이 끝났고 사거리 범위 이내라면 공격 시퀀스 재시작
            if (this.cooldownTicks <= 0 && distSq <= 256.0D) {
                this.attackTicks = 25;   // 서버 AI 틱 세팅
                entity.attackTimer = 25; // 서버 엔티티 타이머 세팅

                // 💡 [클라이언트 동기화]: 서버에서 클라이언트로 "공격 애니메이션을 시작해라" 라는 이벤트를 즉시 전송합니다.
                // 바닐라 철골렘이 공격 애니메이션을 보낼 때 쓰는 규격 패킷입니다.
                entity.level.broadcastEntityEvent(entity, (byte) 4);
            }
        }
    }

    private void shootArrow(LivingEntity target) {
        if (entity.level.isClientSide) return;

        AngelZombieArrow arrow = new AngelZombieArrow(entity.level, entity);
        arrow.setPos(entity.getX(), entity.getEyeY() - 0.1D, entity.getZ());

        double d0 = target.getX() - entity.getX();
        double d1 = target.getY(0.33D) - arrow.getY();
        double d2 = target.getZ() - entity.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        arrow.shoot(d0, d1 + d3 * 0.18D, d2, 1.8F, 1.5F);
        entity.level.addFreshEntity(arrow);
    }
}
package changmin.changmin_villager_turret.feature.zombie.assassin2;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public class Assassin2IntegratedGoal extends Goal {
    private final Assassin2Entity assassin;
    private int attackCooldown = 0;
    private int strafeTick = 0;
    private boolean strafeRight = true;
    private int doubleJumpDelay = -1; // 이단 점프 대기용 타이머

    public Assassin2IntegratedGoal(Assassin2Entity assassin) {
        this.assassin = assassin;
        // 이동(MOVE)과 바라보기(LOOK) 권한을 이 Goal이 독점합니다.
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return assassin.getTarget() != null && assassin.getTarget().isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = assassin.getTarget();
        if (target == null) return;

        double distSq = assassin.distanceToSqr(target);
        // 항상 타겟을 바라봅니다.
        assassin.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 1. [공격 로직]
        if (attackCooldown > 0) attackCooldown--;

        // 사거리 약 15블록 이내일 때 공격
        if (distSq < 225.0D && attackCooldown <= 0) {
            shootSwordGhoul(target);
            // 레벨업에 따라 쿨타임 감소 (15~40틱)
            this.attackCooldown = Math.max(15, 40 - (assassin.getAssassinLevel() * 2));
        }

        // 2. [기동 및 회피 로직]
        if (distSq < 400.0D) { // 20블록 이내 전투 범위
            strafeTick++;

            // 20틱마다 좌우 이동 방향 전환 여부 결정 (30% 확률)
            if (strafeTick % 20 == 0) {
                if (assassin.getRandom().nextFloat() < 0.3) strafeRight = !strafeRight;
            }

            // 타겟을 중심으로 원을 그리는 이동 좌표 계산
            Vec3 relativePos = assassin.position().subtract(target.position()).normalize();
            // 타겟과의 거리를 10블록 정도로 유지하며 옆으로 이동(Strafing)
            Vec3 sideVec = new Vec3(-relativePos.z, 0, relativePos.x).scale(strafeRight ? 3.5 : -3.5);
            Vec3 destination = target.position().add(relativePos.scale(10.0)).add(sideVec);

            assassin.getNavigation().moveTo(destination.x, destination.y, destination.z, 1.4D);

            // --- 점프 기동 ---
            // (1) 지상에서 첫 번째 점프 시도 (약 4% 확률)
            if (assassin.isOnGround() && assassin.getRandom().nextInt(25) == 0) {
                assassin.performAssassinJump(); // Entity에 만든 public 메서드

                // 점프 시 보는 방향으로 살짝 추진력 추가
                Vec3 dash = assassin.getLookAngle().scale(0.5);
                assassin.setDeltaMovement(assassin.getDeltaMovement().add(dash.x, 0.2, dash.z));

                // 7틱(약 0.35초) 뒤에 이단 점프 기회 부여
                this.doubleJumpDelay = 7;
            }
        } else {
            // 거리가 멀면 타겟에게 빠르게 접근
            assassin.getNavigation().moveTo(target, 1.6D);
        }

        // (2) 💡 [이단 점프 실행 체크]
        if (this.doubleJumpDelay > 0) {
            this.doubleJumpDelay--;
            if (this.doubleJumpDelay == 0) {
                // 💡 조건: 공중에 떠 있어야 함 + 50% 확률(nextBoolean) 당첨 시 실행
                if (!assassin.isOnGround() && assassin.getRandom().nextBoolean()) {
                    assassin.performAirJump(); // 공중 도약

                    // 이단 점프 시 더 강력한 공중 대쉬 효과
                    Vec3 airDash = assassin.getLookAngle().scale(0.8);
                    assassin.setDeltaMovement(assassin.getDeltaMovement().add(airDash.x, 0.1, airDash.z));
                }
                this.doubleJumpDelay = -1; // 타이머 초기화
            }
        }
    }

    private void shootSwordGhoul(LivingEntity target) {
        assassin.setAttacking(true);
        assassin.attackTimer = 10;

        // 3D 조준 벡터 계산 (눈 높이 기준)
        Vec3 start = assassin.position().add(0, assassin.getEyeHeight(), 0);
        Vec3 end = target.position().add(0, target.getEyeHeight() * 0.8, 0);
        Vec3 dir = end.subtract(start).normalize();

        double speed = 0.9D;
        SwordGhoulEntity ghoul = new SwordGhoulEntity(assassin.level, assassin, dir.x * speed, dir.y * speed, dir.z * speed);
        assassin.level.addFreshEntity(ghoul);
    }

    @Override
    public void stop() {
        super.stop();
        this.doubleJumpDelay = -1;
        this.strafeTick = 0;
        this.assassin.setAttacking(false);
    }
}
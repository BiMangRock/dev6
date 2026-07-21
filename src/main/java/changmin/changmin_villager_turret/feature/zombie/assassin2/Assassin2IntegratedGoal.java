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

    public Assassin2IntegratedGoal(Assassin2Entity assassin) {
        this.assassin = assassin;
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
        assassin.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 1. 공격 로직
        if (attackCooldown > 0) attackCooldown--;

        if (distSq < 225.0D && attackCooldown <= 0) {
            shootSwordGhoul(target);
            // 발사 즉시 쿨타임 설정 (중복 발사 방지)
            this.attackCooldown = Math.max(15, 40 - (assassin.getAssassinLevel() * 2));
        }

        // 2. 💡 쿨타임 중 기동 로직 (요리조리 이동)
        if (distSq < 256.0D) { // 약 16블록 이내일 때
            strafeTick++;

            // 20틱마다 좌우 방향 전환
            if (strafeTick % 20 == 0) {
                if (assassin.getRandom().nextFloat() < 0.3) strafeRight = !strafeRight;
            }

            // 타겟 주변을 원형으로 맴도는 위치 계산
            Vec3 relativePos = assassin.position().subtract(target.position()).normalize();
            Vec3 sideVec = new Vec3(-relativePos.z, 0, relativePos.x).scale(strafeRight ? 3.0 : -3.0);
            Vec3 destination = target.position().add(relativePos.scale(10.0)).add(sideVec);

            assassin.getNavigation().moveTo(destination.x, destination.y, destination.z, 1.4D);

            // 랜덤 점프 (기동성)
            if (assassin.isOnGround() && assassin.getRandom().nextInt(25) == 0) {
                assassin.performAssassinJump();
                Vec3 dash = assassin.getLookAngle().scale(0.6);
                assassin.setDeltaMovement(assassin.getDeltaMovement().add(dash.x, 0.3, dash.z));
            }
        } else {
            // 거리가 멀면 타겟에게 빠르게 접근
            assassin.getNavigation().moveTo(target, 1.6D);
        }
    }

    private void shootSwordGhoul(LivingEntity target) {
        assassin.setAttacking(true);
        assassin.attackTimer = 10;

        // 3D 조준 벡터
        Vec3 start = assassin.position().add(0, assassin.getEyeHeight(), 0);
        Vec3 end = target.position().add(0, target.getEyeHeight() * 0.8, 0);
        Vec3 dir = end.subtract(start).normalize();

        double speed = 0.9D;
        SwordGhoulEntity ghoul = new SwordGhoulEntity(assassin.level, assassin, dir.x * speed, dir.y * speed, dir.z * speed);
        assassin.level.addFreshEntity(ghoul);
    }
}
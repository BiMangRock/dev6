package changmin.changmin_villager_turret.feature.hero.healer_hero;

import changmin.changmin_villager_turret.feature.turret.healer.HealerTurretEntity;
import changmin.changmin_villager_turret.registry.ModEntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class HealerHeroActionGoal extends Goal {
    private final HealerHeroEntity hero;
    private int attackCooldown = 0;
    private int summonCooldown = 0;

    // 무작위 전투 선회 이동을 처리하는 타이머 및 동기화 플래그
    private int moveTimeout = 0;
    private boolean isPositionChosen = false;

    public HealerHeroActionGoal(HealerHeroEntity hero) {
        this.hero = hero;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.hero.getTarget() != null && this.hero.getTarget().isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = this.hero.getTarget();
        if (target == null) return;

        this.hero.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.summonCooldown > 0) this.summonCooldown--;
        if (this.moveTimeout > 0) this.moveTimeout--;

        // 영웅이 소환 주문이나 사격 모션(애니메이션 중)을 취하고 있다면 자리에 멈추어 작업을 마저 끝마칩니다.
        if (this.hero.getAnimationState() > 0) {
            this.hero.getNavigation().stop();
            this.isPositionChosen = false;
            return;
        }

        double distSq = this.hero.distanceToSqr(target);

        // 스킬 1: 힐러 터렛 소환 시도 (소환할 자리를 확보하기 위해 정지 후 캐스팅 수행)
        if (this.summonCooldown <= 0 && this.hero.getSummonedTurretsCount() < this.hero.getMaxTurretsCount()) {
            summonHealerTurret();
            this.summonCooldown = 200; // 소환 쿨타임 10초(200틱)
            this.isPositionChosen = false; // 새로운 회피 구역 재지정
            return;
        }

        // 🆕 [무작위 선회 기동 연산]
        // 타겟과의 안전거리를 유지하며 무작위 각도로 쉴 새 없이 움직이게 하여 힐러 터렛 사이를 우회합니다.
        if (!this.isPositionChosen || this.hero.getNavigation().isDone() || this.moveTimeout <= 0) {
            double angle = this.hero.getRandom().nextDouble() * Math.PI * 2;
            double distance = 6.0D + this.hero.getRandom().nextDouble() * 4.0D; // 대상과 6~10블록 사이의 각도로 유동적 보정
            double tx = target.getX() + Math.cos(angle) * distance;
            double tz = target.getZ() + Math.sin(angle) * distance;
            double ty = target.getY();

            // 목표 좌표로 기동 비행/도보 (걷기 애니메이션이 자연스럽게 활성화됩니다)
            this.hero.getNavigation().moveTo(tx, ty, tz, 1.0D);
            this.isPositionChosen = true;
            this.moveTimeout = 40; // 좌표를 너무 오랫동안 헤매는 것을 차단하기 위해 2초의 런타임 제한 제공
        }

        // 스킬 2: 사격 각도를 확보하고 기동이 일정 수준 마쳐진 타이밍에 정밀 사격 실시
        if (distSq <= 256.0D && this.attackCooldown <= 0) {
            // 사격 모션이 깔끔하게 표현되도록 기동 일시 제동 후 격발
            this.hero.getNavigation().stop();
            shootHeartProjectile(target);
            this.attackCooldown = 40; // 사격 쿨타임 2초
            this.isPositionChosen = false; // 사격 즉시 다른 3D 우회 좌표로 기동 시작
        }
    }

    private void summonHealerTurret() {
        this.hero.triggerBuildAnimation();
        HealerTurretEntity turret = ModEntityTypes.HEALER_TURRET.get().create(this.hero.level);
        if (turret != null) {
            double sx = this.hero.getX() + (this.hero.getRandom().nextDouble() - 0.5D) * 4.0D;
            double sz = this.hero.getZ() + (this.hero.getRandom().nextDouble() - 0.5D) * 4.0D;
            turret.moveTo(sx, this.hero.getY(), sz, this.hero.getYRot(), 0.0F);

            turret.setOwnerUUID(this.hero.getUUID());

            this.hero.level.addFreshEntity(turret);
            this.hero.trackSummonedTurret(turret.getUUID());
        }
    }

    private void shootHeartProjectile(LivingEntity target) {
        this.hero.triggerAttackOrHealAnimation();
        int count = 1 + (this.hero.getHeroLevel() - 1);

        for (int i = 0; i < count; i++) {
            Vec3 start = this.hero.position().add(0, this.hero.getEyeHeight() - 0.3D, 0);
            Vec3 end = target.position().add(0, target.getEyeHeight() * 0.8D, 0);
            Vec3 dir = end.subtract(start).normalize();

            if (count > 1) {
                double spreadDegrees = (i - (count - 1) / 2.0D) * 8.0D;
                double rad = Math.toRadians(spreadDegrees);
                double rx = dir.x * Math.cos(rad) - dir.z * Math.sin(rad);
                double rz = dir.x * Math.sin(rad) + dir.z * Math.cos(rad);
                dir = new Vec3(rx, dir.y, rz).normalize();
            }

            double speed = 0.3D; // 조율된 1/3 속도 적용
            LoveProjectileEntity projectile = new LoveProjectileEntity(this.hero.level, this.hero, this.hero.getHeroLevel());
            projectile.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
            this.hero.level.addFreshEntity(projectile);
        }
    }
}
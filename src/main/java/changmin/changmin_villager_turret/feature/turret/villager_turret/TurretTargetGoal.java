package changmin.changmin_villager_turret.feature.turret.villager_turret;

import changmin.changmin_villager_turret.config.TurretConfig;
import changmin.changmin_villager_turret.feature.turret.common.TargetPriority;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * 🎯 주민 터렛 표적 선택
 *
 * 예전: 가장 가까운 좀비만 노림 (NearestAttackableTargetGoal 기본 동작)
 * 지금: 후보 전체를 점수로 평가해 가장 위험하거나(힐러·지휘관·보스) 곧 죽일 수 있는 적을 우선 노리고,
 *       다른 터렛이 이미 노리는 적은 감점해서 화력을 분산합니다. (TargetPriority 참고)
 *       교전 중에도 1초(기본값)마다 더 좋은 표적이 생겼는지 다시 평가합니다.
 */
public class TurretTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final VillagerTurretEntity turret;
    private int retargetTimer = 0;

    public TurretTargetGoal(VillagerTurretEntity turret, Class<T> targetType) {
        // mustSee = false로 설정하여 부모 클래스의 시야 체크를 수동으로 제어합니다.
        super(turret, targetType, false);
        this.turret = turret;

        // 스캔 단계에서 호출되는 동적 필터
        this.targetConditions.selector((targetEntity) -> {
            // [종족 체크] 좀비 진형이 아니면 제외
            if (!IZombieTribe.isZombieTribe(targetEntity)) {
                return false;
            }
            // [투시 체크] 투시 업그레이드가 없을 때만 시야 충돌을 계산합니다.
            if (this.turret.getCanSeeThroughWalls() == 0) {
                return this.turret.getSensing().hasLineOfSight(targetEntity);
            }
            return true;
        });
    }

    /** 가장 가까운 1마리 대신, 후보 전체를 점수로 비교해 최적의 표적을 고릅니다. */
    @Override
    protected void findTarget() {
        AABB area = this.getTargetSearchArea(this.getFollowDistance());
        List<T> candidates = this.mob.level().getEntitiesOfClass(this.targetType, area,
                e -> this.targetConditions.test(this.mob, e));

        double expectedHit = TurretConfig.arrowDamage() + this.turret.getDamageLevel() * TurretConfig.arrowDamagePerLevel();
        this.target = TargetPriority.pickBest(this.mob, candidates, expectedHit,
                VillagerTurretEntity.class, this.getFollowDistance());
    }

    @Override
    public void start() {
        super.start();
        this.retargetTimer = TurretConfig.retargetInterval();
    }

    /** 교전 중 주기적으로 재평가: 힐러가 새로 나타나는 등 더 좋은 표적이 생기면 갈아탑니다. */
    @Override
    public void tick() {
        super.tick();
        if (--this.retargetTimer > 0) return;
        this.retargetTimer = TurretConfig.retargetInterval();

        this.findTarget();
        if (this.target != null && this.target != this.mob.getTarget()) {
            this.mob.setTarget(this.target);
        }
    }

    @Override
    public boolean canContinueToUse() {
        boolean cont = super.canContinueToUse();
        if (cont) {
            LivingEntity currentTarget = this.turret.getTarget();
            if (currentTarget != null) {
                // 추적 도중 대상이 좀비 종족 판정에서 벗어났다면 타겟팅을 해제합니다.
                if (!IZombieTribe.isZombieTribe(currentTarget)) {
                    return false;
                }
                // 시야 체크가 켜져 있을 때 벽 뒤로 숨으면 추적을 중단합니다.
                if (this.turret.getCanSeeThroughWalls() == 0) {
                    return this.turret.getSensing().hasLineOfSight(currentTarget);
                }
            }
        }
        return cont;
    }
}

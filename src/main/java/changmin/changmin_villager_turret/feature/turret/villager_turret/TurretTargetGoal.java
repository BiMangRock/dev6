package changmin.changmin_villager_turret.feature.turret.villager_turret;

import changmin.changmin_villager_turret.zombieTribe.IZombieTribe; // 신규 추가된 임포트 구문
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class TurretTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final VillagerTurretEntity turret;

    public TurretTargetGoal(VillagerTurretEntity turret, Class<T> targetType) {
        // mustSee = false로 설정하여 부모 클래스의 시야 체크를 수동으로 제어합니다.
        super(turret, targetType, false);
        this.turret = turret;

        // 스캔 단계(super.canUse)에서 호출되는 동적 필터
        this.targetConditions.selector((targetEntity) -> {
            // [종족 체크] 대상이 좀비 진형(바닐라 좀비 혹은 IZombieTribe)이 아니면 조준 대상에서 즉시 제외
            if (!IZombieTribe.isZombieTribe(targetEntity)) {
                return false;
            }

            // [투시 체크] 투시 업그레이드(CanSeeThroughWalls)가 없을 때만 시야 충돌을 계산합니다.
            if (this.turret.getCanSeeThroughWalls() == 0) {
                return this.turret.getSensing().hasLineOfSight(targetEntity);
            }
            return true;
        });
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
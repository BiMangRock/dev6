package changmin.myMod.feature.turret.villager_turret;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class TurretTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final VillagerTurretEntity turret;

    public TurretTargetGoal(VillagerTurretEntity turret, Class<T> targetType) {
        // mustSee = false로 설정하여, 부모 클래스의 무조건적인 시야 체크를 우회합니다.
        super(turret, targetType, false);
        this.turret = turret;

        // [핵심 해결책] 스캔 단계(super.canUse)에서 호출되는 동적 필터(selector)를 정의합니다.
        this.targetConditions.selector((targetEntity) -> {
            // 투시 훈련(CanSeeThroughWalls) 업그레이드가 비활성화(0) 상태라면
            if (this.turret.getCanSeeThroughWalls() == 0) {
                // 시야(hasLineOfSight)에 직접적으로 닿는 대상만 타겟팅을 허용합니다.
                return this.turret.getSensing().hasLineOfSight(targetEntity);
            }
            // 업그레이드가 되어 있다면(1) 벽 너머의 대상도 모두 허용합니다.
            return true;
        });
    }

    @Override
    public boolean canContinueToUse() {
        boolean cont = super.canContinueToUse();
        // 이미 타겟으로 지정된 좀비가 움직이다가 벽 뒤로 숨었을 때 추적을 유지할지 결정합니다.
        if (cont && this.turret.getCanSeeThroughWalls() == 0) {
            LivingEntity currentTarget = this.turret.getTarget();
            if (currentTarget != null) {
                return this.turret.getSensing().hasLineOfSight(currentTarget);
            }
        }
        return cont;
    }
}
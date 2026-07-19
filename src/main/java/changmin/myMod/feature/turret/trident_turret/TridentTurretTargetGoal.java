package changmin.myMod.feature.turret.trident_turret;

import changmin.myMod.zombieTribe.IZombieTribe;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class TridentTurretTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final TridentTurretEntity turret;

    public TridentTurretTargetGoal(TridentTurretEntity turret, Class<T> targetType) {
        // mustSee = false로 상속하여 시야 판정을 커스텀 필터에서 직접 제어합니다.
        super(turret, targetType, false);
        this.turret = turret;

        // 타겟 스캔 조건 필터링
        this.targetConditions.selector((targetEntity) -> {
            // 좀비 종족 진형(IZombieTribe 구현체 혹은 바닐라 좀비류)만 조준 대상으로 설정
            if (!IZombieTribe.isZombieTribe(targetEntity)) {
                return false;
            }
            // 삼지창 터렛은 벽을 뚫는 투시 기능이 기본적으로 없으므로 상시 시야 확인
            return this.turret.getSensing().hasLineOfSight(targetEntity);
        });
    }

    @Override
    public boolean canContinueToUse() {
        boolean cont = super.canContinueToUse();
        if (cont) {
            LivingEntity currentTarget = this.turret.getTarget();
            if (currentTarget != null) {
                if (!IZombieTribe.isZombieTribe(currentTarget)) {
                    return false;
                }
                return this.turret.getSensing().hasLineOfSight(currentTarget);
            }
        }
        return cont;
    }
}
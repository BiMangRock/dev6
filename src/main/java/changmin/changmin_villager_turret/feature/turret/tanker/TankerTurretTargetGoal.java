package changmin.changmin_villager_turret.feature.turret.tanker;

import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class TankerTurretTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final TankerTurretEntity turret;

    public TankerTurretTargetGoal(TankerTurretEntity turret, Class<T> targetType) {
        super(turret, targetType, false);
        this.turret = turret;

        // 좀비들만 골라 시야 및 타겟팅을 잡는 동적 필터
        this.targetConditions.selector((targetEntity) -> {
            if (!IZombieTribe.isZombieTribe(targetEntity)) {
                return false;
            }
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
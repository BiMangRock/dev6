package changmin.myMod.feature.turret.villager_turret;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class TurretTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final VillagerTurretEntity turret;

    public TurretTargetGoal(VillagerTurretEntity turret, Class<T> targetType) {
        // mustSee = false로 생성하여 기본 조건 통과 후 스탯에 따라 수동 검사 진행
        super(turret, targetType, false);
        this.turret = turret;
    }

    @Override
    public boolean canUse() {
        boolean use = super.canUse();
        if (use && this.turret.getCanSeeThroughWalls() == 0) {
            if (this.target != null) {
                return this.turret.getSensing().hasLineOfSight(this.target);
            }
        }
        return use;
    }

    @Override
    public boolean canContinueToUse() {
        boolean cont = super.canContinueToUse();
        if (cont && this.turret.getCanSeeThroughWalls() == 0) {
            LivingEntity currentTarget = this.turret.getTarget();
            if (currentTarget != null) {
                return this.turret.getSensing().hasLineOfSight(currentTarget);
            }
        }
        return cont;
    }
}
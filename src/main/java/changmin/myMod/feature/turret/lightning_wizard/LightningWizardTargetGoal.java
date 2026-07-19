package changmin.myMod.feature.turret.lightning_wizard;

import changmin.myMod.zombieTribe.IZombieTribe;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class LightningWizardTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final LightningWizardEntity turret;

    public LightningWizardTargetGoal(LightningWizardEntity turret, Class<T> targetType) {
        super(turret, targetType, false);
        this.turret = turret;

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
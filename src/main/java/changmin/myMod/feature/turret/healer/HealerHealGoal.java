package changmin.myMod.feature.turret.healer;

import changmin.myMod.ally.IAlly;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class HealerHealGoal extends Goal {
    private final HealerTurretEntity healer;
    private LivingEntity target;

    public HealerHealGoal(HealerTurretEntity healer) {
        this.healer = healer;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.healer.getCurrentCooldown() > 0) {
            return false;
        }

        this.target = findHealingTarget();
        return this.target != null;
    }

    @Override
    public void start() {
        if (this.healer.getAoeHealEnabled() == 1) {
            performAoeHeal();
        } else if (this.target != null) {
            performSingleHeal(this.target);
        }
    }

    private LivingEntity findHealingTarget() {
        double range = 8.0D + (this.healer.getRangeLevel() * 1.0D);
        AABB box = this.healer.getBoundingBox().inflate(range);
        List<LivingEntity> entities = this.healer.level.getEntitiesOfClass(LivingEntity.class, box);

        LivingEntity bestTarget = null;
        double closestDist = Double.MAX_VALUE;

        // 1순위: 주변 플레이어 중 가장 체력이 낮은 대상
        for (LivingEntity entity : entities) {
            if (entity instanceof Player && entity.isAlive() && entity.getHealth() < entity.getMaxHealth()) {
                double dist = this.healer.distanceToSqr(entity);
                if (dist < closestDist) {
                    closestDist = dist;
                    bestTarget = entity;
                }
            }
        }

        // 2순위: 🆕 자기 자신(healer)을 포함한 아군 중 가장 가까운 대상 (거리가 0이므로 자가 치유를 최우선으로 진행함)
        if (bestTarget == null) {
            closestDist = Double.MAX_VALUE;
            for (LivingEntity entity : entities) {
                if (entity.isAlive() && (entity == this.healer || this.healer.isAllyWith(entity))) {
                    if (entity.getHealth() < entity.getMaxHealth()) {
                        double dist = this.healer.distanceToSqr(entity);
                        if (dist < closestDist) {
                            closestDist = dist;
                            bestTarget = entity;
                        }
                    }
                }
            }
        }

        return bestTarget;
    }

    private void performSingleHeal(LivingEntity target) {
        float restored = healEntity(target, false);
        if (restored > 0) {
            this.healer.addXp((int) restored);

            if (this.healer.level instanceof ServerLevel serverLevel) {
                HealerEffectHelper.spawnSingleHealLineEffects(serverLevel, this.healer, target);
                HealerEffectHelper.damageUndeadInRange(serverLevel, this.healer);
            }
            this.healer.setCurrentCooldown(this.healer.getCalculatedCooldown());
        }
    }

    private void performAoeHeal() {
        double range = 8.0D + (this.healer.getRangeLevel() * 1.0D);
        AABB box = this.healer.getBoundingBox().inflate(range);
        List<LivingEntity> entities = this.healer.level.getEntitiesOfClass(LivingEntity.class, box);

        float totalRestored = 0.0F;
        boolean healedAtLeastOne = false;

        for (LivingEntity entity : entities) {
            // 🆕 광역 치유 연산에서도 본인(healer)을 포함시켜 동시 치유
            if (entity.isAlive() && (entity == this.healer || entity instanceof Player || this.healer.isAllyWith(entity))) {
                if (entity.getHealth() < entity.getMaxHealth()) {
                    float restored = healEntity(entity, true); // 광역 50% 치유 패널티 적용
                    if (restored > 0) {
                        totalRestored += restored;
                        healedAtLeastOne = true;
                        if (this.healer.level instanceof ServerLevel serverLevel) {
                            HealerEffectHelper.spawnHealBubbleEffects(serverLevel, entity);
                        }
                    }
                }
            }
        }

        if (healedAtLeastOne) {
            this.healer.addXp((int) totalRestored);
            if (this.healer.level instanceof ServerLevel serverLevel) {
                HealerEffectHelper.spawnAoeRingEffects(serverLevel, this.healer);
                HealerEffectHelper.damageUndeadInRange(serverLevel, this.healer);
            }
            this.healer.setCurrentCooldown(this.healer.getCalculatedCooldown());
        }
    }

    private float healEntity(LivingEntity entity, boolean isAoe) {
        float currentHealth = entity.getHealth();
        float maxHealth = entity.getMaxHealth();

        float healAmount = this.healer.getHealAmount();
        if (isAoe) {
            healAmount *= 0.5F; // 🆕 광역 치유 패널티(힐량 50% 감소)
        }

        float neededHeal = maxHealth - currentHealth;
        float actualHealed = Math.min(healAmount, neededHeal);

        if (actualHealed > 0) {
            entity.heal(actualHealed);

            // 🆕 초과 치유량 보호막(흡수 하트) 전환 로직
            float excess = healAmount - neededHeal;
            if (excess > 0 && this.healer.getShieldLevel() > 0) {
                float currentAbsorption = entity.getAbsorptionAmount();
                entity.setAbsorptionAmount(currentAbsorption + excess); // 일단 무제한 누적 적용
            }

            if (this.healer.getCleanseEnabled() == 1) {
                entity.removeEffect(MobEffects.POISON);
                entity.removeEffect(MobEffects.WITHER);
                entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                entity.removeEffect(MobEffects.WEAKNESS);
            }
        }
        return actualHealed;
    }
}
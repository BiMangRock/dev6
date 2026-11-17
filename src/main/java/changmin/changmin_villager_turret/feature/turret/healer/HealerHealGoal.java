package changmin.changmin_villager_turret.feature.turret.healer;

import changmin.changmin_villager_turret.config.TurretConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class HealerHealGoal extends Goal {
    private int scanCooldown = 0; // 범위 검색 주기 카운터
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
        // 부상자가 없을 때 2틱마다 범위 검색이 반복되지 않도록 주기 제한
        // (canUse는 2틱마다 호출될 수 있어 tickCount % N 대신 호출 횟수 카운터 사용)
        if (--this.scanCooldown > 0) {
            return false;
        }
        this.scanCooldown = Math.max(1, TurretConfig.scanInterval() / 2);

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

    // 💡 [수정됨] 거리 기준이 아닌, 범위 내에서 체력 퍼센트가 가장 낮은 아군을 탐색하는 로직
    private LivingEntity findHealingTarget() {
        double range = 8.0D + (this.healer.getRangeLevel() * 1.0D);
        AABB box = this.healer.getBoundingBox().inflate(range);
        List<LivingEntity> entities = this.healer.level.getEntitiesOfClass(LivingEntity.class, box);

        LivingEntity bestTarget = null;
        double lowestHealthPercent = 1.0D; // 1.0D는 체력 100%를 뜻하며, 이보다 낮아야(부상 상태여야) 대상이 됩니다.

        for (LivingEntity entity : entities) {
            // 생존해 있으며 플레이어이거나, 자기 자신(healer)이거나, 등록된 아군인 경우
            if (entity.isAlive() && (entity instanceof Player || entity == this.healer || this.healer.isAllyWith(entity))) {
                double maxHealth = entity.getMaxHealth();
                double currentHealth = entity.getHealth();

                // 부상당한 대상인 경우
                if (currentHealth < maxHealth) {
                    double healthPercent = currentHealth / maxHealth; // 절대적인 양이 아닌 퍼센트 비율 계산

                    // 1순위: 체력 비율(%)이 더 낮은 대상이 최우선이 됩니다.
                    if (healthPercent < lowestHealthPercent) {
                        lowestHealthPercent = healthPercent;
                        bestTarget = entity;
                    }
                    // 2순위: 체력 비율(%)이 완전히 동일하다면, 더 가까이 있는 대상을 선택합니다 (타이브레이커)
                    else if (Math.abs(healthPercent - lowestHealthPercent) < 0.0001D && bestTarget != null) {
                        if (this.healer.distanceToSqr(entity) < this.healer.distanceToSqr(bestTarget)) {
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
            // 광역 치유 연산에서도 본인(healer)을 포함시켜 동시 치유
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
            healAmount *= 0.5F; // 광역 치유 패널티(힐량 50% 감소)
        }

        float neededHeal = maxHealth - currentHealth;
        float actualHealed = Math.min(healAmount, neededHeal);

        if (actualHealed > 0) {
            entity.heal(actualHealed);

            // 초과 치유량 보호막(흡수 하트) 전환 로직
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
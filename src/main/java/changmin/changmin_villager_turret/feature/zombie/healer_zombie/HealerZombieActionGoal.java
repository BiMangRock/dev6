package changmin.changmin_villager_turret.feature.zombie.healer_zombie;

import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

public class HealerZombieActionGoal extends Goal {
    private final HealerZombieEntity healer;
    private LivingEntity target;
    private int tickCounter = 0;
    private int cooldown = 0;

    public HealerZombieActionGoal(HealerZombieEntity healer) {
        this.healer = healer;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }

        if (this.healer.getHealth() <= this.healer.getMaxHealth() * 0.5F) {
            this.target = this.healer;
            return true;
        }

        double range = this.healer.getHealRange();
        List<LivingEntity> targets = this.healer.level.getEntitiesOfClass(
                LivingEntity.class,
                this.healer.getBoundingBox().inflate(range),
                e -> e.isAlive() && IZombieTribe.isZombieTribe(e) && !(e instanceof IAlly)
        );

        LivingEntity worstAlly = null;
        double maxLostHealth = 0;

        for (LivingEntity entity : targets) {
            double lostHealth = entity.getMaxHealth() - entity.getHealth();
            if (lostHealth > maxLostHealth) {
                maxLostHealth = lostHealth;
                worstAlly = entity;
            }
        }

        if (worstAlly != null && maxLostHealth > 0) {
            this.target = worstAlly;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        // 도중에 취소되지 않고 애니메이션 전체 길이인 40틱(2초)을 정상 수복하도록 조건을 제어합니다.
        return this.tickCounter < 40;
    }

    @Override
    public void start() {
        this.tickCounter = 0;
        this.healer.getNavigation().stop();
        this.healer.triggerHealAnimation();
    }

    @Override
    public void tick() {
        if (this.target != null && this.target.isAlive()) {
            this.healer.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        }

        this.tickCounter++;

        // 1.5초(30틱) 경과 시 회복 능력 주입 및 선형 광선 방출 효과
        if (this.tickCounter == 30) {
            if (this.target != null && this.target.isAlive() && this.healer.level instanceof ServerLevel serverLevel) {
                float healAmount = this.healer.getHealAmount();
                this.target.heal(healAmount);

                // 에너지 흐름 시각화 선 생성
                spawnHealLineEffects(serverLevel, this.healer, this.target);

                this.healer.addXp(1);
            }
        }
    }

    @Override
    public void stop() {
        this.target = null;
        this.tickCounter = 0;
        this.cooldown = 40;
        this.healer.setAnimationState(0); // 완료 시 완벽히 상태 초기화
    }

    private void spawnHealLineEffects(ServerLevel level, HealerZombieEntity healer, LivingEntity target) {
        double startX = healer.getX();
        double startY = healer.getY(1.2D);
        double startZ = healer.getZ();

        double endX = target.getX();
        double endY = target.getY(0.5D);
        double endZ = target.getZ();

        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        int particleCount = (int) (distance * 4);
        for (int i = 0; i <= particleCount; i++) {
            double ratio = (double) i / particleCount;
            double px = startX + dx * ratio;
            double py = startY + dy * ratio;
            double pz = startZ + dz * ratio;

            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        level.sendParticles(ParticleTypes.HEART, endX, endY + 0.5D, endZ, 5, 0.3D, 0.3D, 0.3D, 0.02D);
        healer.playSound(SoundEvents.VILLAGER_WORK_CLERIC, 1.0F, 1.1F + healer.getRandom().nextFloat() * 0.2F);
    }
}
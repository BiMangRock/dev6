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

        // 1. 자신의 체력이 50% 이하라면 자가 치유 최우선 지정
        if (this.healer.getHealth() <= this.healer.getMaxHealth() * 0.5F) {
            this.target = this.healer;
            return true;
        }

        // 2. 주변 좀비 동맹 중 체력 손실이 가장 큰 대상 탐색
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
        // 애니메이션이 무조건 끝까지(2초 = 40틱) 재생되도록 유지 조건을 타겟 생사 여부와 분리합니다.
        return this.tickCounter < 40;
    }

    @Override
    public void start() {
        this.tickCounter = 0;
        this.healer.getNavigation().stop();
        this.healer.triggerHealAnimation(); // 애니메이션 상태 및 타이머 활성화
    }

    @Override
    public void tick() {
        if (this.target != null && this.target.isAlive()) {
            this.healer.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        }

        this.tickCounter++;

        // 1.5초(30틱) 시점에 타겟에게 힐 및 선형 빔 연출 적용
        if (this.tickCounter == 30) {
            if (this.target != null && this.target.isAlive() && this.healer.level instanceof ServerLevel serverLevel) {
                float healAmount = this.healer.getHealAmount();
                this.target.heal(healAmount);

                // 빔 효과 연출 실행
                spawnHealLineEffects(serverLevel, this.healer, this.target);

                // 경험치 추가
                this.healer.addXp(1);
            }
        }
    }

    @Override
    public void stop() {
        this.target = null;
        this.tickCounter = 0;
        this.cooldown = 40; // 재사용 대기시간
        this.healer.setAnimationState(0); // 골 종료 시 애니메이션 상태를 확실하게 복구
    }

    // 🆕 힐러 좀비와 치유 대상 사이를 잇는 파티클 줄기 연출 메서드
    private void spawnHealLineEffects(ServerLevel level, HealerZombieEntity healer, LivingEntity target) {
        double startX = healer.getX();
        double startY = healer.getY(1.2D); // 좀비의 가슴/눈높이 부근에서 시작
        double startZ = healer.getZ();

        double endX = target.getX();
        double endY = target.getY(0.5D); // 대상의 몸 중앙 부근으로 종점 설정
        double endZ = target.getZ();

        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // 거리에 비례하여 파티클을 촘촘하게 배치 (블록당 4개 수준)
        int particleCount = (int) (distance * 4);
        for (int i = 0; i <= particleCount; i++) {
            double ratio = (double) i / particleCount;
            double px = startX + dx * ratio;
            double py = startY + dy * ratio;
            double pz = startZ + dz * ratio;

            // 힐러 주민 터렛과 동일한 HAPPY_VILLAGER 입자로 연결 선을 구성합니다.
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        // 대상 주변에 하트 폭발 효과 추가
        level.sendParticles(ParticleTypes.HEART, endX, endY + 0.5D, endZ, 5, 0.3D, 0.3D, 0.3D, 0.02D);

        // 치유 주문 소리 효과 재생
        healer.playSound(SoundEvents.VILLAGER_WORK_CLERIC, 1.0F, 1.1F + healer.getRandom().nextFloat() * 0.2F);
    }
}
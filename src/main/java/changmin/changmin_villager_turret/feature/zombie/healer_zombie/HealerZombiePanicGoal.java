package changmin.changmin_villager_turret.feature.zombie.healer_zombie;

import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

public class HealerZombiePanicGoal extends Goal {
    private final HealerZombieEntity healer;
    private final double speedModifier;
    private int panicTime = 0;
    private double posX;
    private double posY;
    private double posZ;

    public HealerZombiePanicGoal(HealerZombieEntity healer, double speedModifier) {
        this.healer = healer;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // 🆕 치유 애니메이션(State == 1)이 활성화되어 시전 중인 경우에는 도망 골로 전환하지 못하도록 제어합니다.
        if (this.healer.getAnimationState() == 1) {
            return false;
        }

        return this.healer.getLastHurtByMob() != null && this.healer.tickCount - this.healer.getLastHurtByMobTimestamp() < 60;
    }

    @Override
    public void start() {
        this.panicTime = 60; // 3초 유지
        this.findRandomPosition();
    }

    private void findRandomPosition() {
        double rx = this.healer.getX() + (this.healer.getRandom().nextDouble() - 0.5D) * 16.0D;
        double ry = this.healer.getY() + (this.healer.getRandom().nextDouble() - 0.5D) * 4.0D;
        double rz = this.healer.getZ() + (this.healer.getRandom().nextDouble() - 0.5D) * 16.0D;
        this.posX = rx;
        this.posY = ry;
        this.posZ = rz;
        this.healer.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
    }

    @Override
    public boolean canContinueToUse() {
        // 도망 중에도 혹시 치유 조건이 먼저 충족되더라도 도망 상태 타이머가 끝날 때까지 이동을 이어갑니다.
        return this.panicTime > 0 && this.healer.getLastHurtByMob() != null;
    }

    @Override
    public void tick() {
        this.panicTime--;
        if (this.panicTime % 15 == 0 || this.healer.getNavigation().isDone()) {
            this.findRandomPosition();
        }
    }

    @Override
    public void stop() {
        this.healer.setLastHurtByMob(null);
        this.healer.getNavigation().stop();
    }
}
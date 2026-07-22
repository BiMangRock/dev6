package changmin.changmin_villager_turret.feature.zombie.zombie_commander;

import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import java.util.EnumSet;

public class FollowAlliesGoal extends Goal {
    private final ZombieCommanderEntity commander;
    private final double speedModifier;
    private LivingEntity targetAlly;

    public FollowAlliesGoal(ZombieCommanderEntity commander, double speedModifier) {
        this.commander = commander;
        this.speedModifier = speedModifier; // 여기서 2.0D를 전달받음
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        this.targetAlly = this.commander.level.getNearestEntity(
                LivingEntity.class,
                TargetingConditions.forNonCombat().range(32.0D).selector(e ->
                        IZombieTribe.isZombieTribe(e) && !e.getUUID().equals(commander.getUUID())
                ),
                this.commander, this.commander.getX(), this.commander.getY(), this.commander.getZ(),
                this.commander.getBoundingBox().inflate(32.0D)
        );
        // 아군이 버프 범위보다 멀리 있을 때만 2배 속도로 추적
        return targetAlly != null && this.commander.distanceToSqr(targetAlly) > Math.pow(this.commander.getBuffRange(), 2);
    }

    @Override
    public boolean canContinueToUse() {
        return targetAlly != null && targetAlly.isAlive() &&
                this.commander.distanceToSqr(targetAlly) > Math.pow(this.commander.getBuffRange(), 2);
    }

    @Override
    public void start() {
        // 아군을 향해 2배 속도로 이동
        this.commander.getNavigation().moveTo(this.targetAlly, this.speedModifier);
    }

    @Override
    public void stop() {
        this.targetAlly = null;
        this.commander.getNavigation().stop();
    }
}
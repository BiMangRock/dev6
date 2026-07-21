package changmin.changmin_villager_turret.feature.zombie.assassin2;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class Assassin2ShadowMoveGoal extends Goal {
    private final Assassin2Entity assassin;
    private LivingEntity target;
    private int moveTick = 0;

    public Assassin2ShadowMoveGoal(Assassin2Entity assassin) {
        this.assassin = assassin;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        this.target = assassin.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void tick() {
        double distSq = assassin.distanceToSqr(target);
        moveTick++;

        // 1. 타겟 주변을 랜덤하게 맴돌기
        if (moveTick % 20 == 0) {
            double angle = assassin.getRandom().nextDouble() * Math.PI * 2;
            double radius = 4.0D + assassin.getRandom().nextDouble() * 4.0D;
            double tx = target.getX() + Math.cos(angle) * radius;
            double tz = target.getZ() + Math.sin(angle) * radius;
            assassin.getNavigation().moveTo(tx, target.getY(), tz, 1.5D);
        }

        // 2. 암살자 다운 랜덤 점프 (회피 로직)
        if (assassin.isOnGround() && assassin.getRandom().nextInt(30) == 0) {
            assassin.jumpFromGround();
            // 점프 시 공중에서 대쉬하는 듯한 속도 부여
            Vec3 look = assassin.getLookAngle();
            assassin.setDeltaMovement(assassin.getDeltaMovement().add(look.x * 0.5, 0.3, look.z * 0.5));
        }

        // 3. 너무 멀어지면 빠르게 접근
        if (distSq > 256.0D) {
            assassin.getNavigation().moveTo(target, 1.8D);
        }
    }
}
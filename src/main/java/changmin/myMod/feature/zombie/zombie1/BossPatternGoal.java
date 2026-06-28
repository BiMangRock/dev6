package changmin.myMod.feature.zombie.zombie1;

import changmin.myMod.ally.IAlly;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import java.util.EnumSet;

public class BossPatternGoal extends Goal {
    private final ZombieBossEntity boss;
    private int patternCooldown = 0;

    public BossPatternGoal(ZombieBossEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Player nearestPlayer = getNearestPlayerInRange(20.0D);
        if (nearestPlayer != null) {
            this.boss.setTarget(nearestPlayer);
        }

        LivingEntity target = this.boss.getTarget();
        return target != null && target.isAlive() && (target instanceof Player || target instanceof IAlly);
    }

    @Override
    public void tick() {
        Player nearestPlayer = getNearestPlayerInRange(20.0D);
        if (nearestPlayer != null && this.boss.getTarget() != nearestPlayer) {
            this.boss.setTarget(nearestPlayer);
        }

        LivingEntity target = this.boss.getTarget();
        if (target != null) {
            this.boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distanceSq = this.boss.distanceToSqr(target);

            if (this.patternCooldown > 0) {
                this.patternCooldown--;
            }

            // 조준 사격을 위해 걷기를 완전히 정지시킵니다.
            this.boss.getNavigation().stop();

            // 쿨타임이 끝났고 사정거리(20블록) 내에 타겟이 있다면 2, 3, 5 중 랜덤 실행
            if (this.boss.getActiveAttack() == 0 && this.patternCooldown <= 0) {
                if (distanceSq < 400.0D) {
                    int randomChoice = this.boss.getRandom().nextInt(3); // 0, 1, 2

                    if (randomChoice == 0) {
                        this.boss.triggerAttack2(); // 기존 정밀 조준 사격
                        this.patternCooldown = 80;  // 4초 대기
                    } else if (randomChoice == 1) {
                        this.boss.triggerAttack3(); // 3D 구형 부채꼴 나선 사격 (강화됨)
                        this.patternCooldown = 120; // 6초 대기
                    } else {
                        this.boss.triggerAttack5(); // 🆕 제자리 360도 진공 난사 (신설)
                        this.patternCooldown = 130; // 6.5초 대기
                    }
                }
            }
        }
    }

    @Override
    public void stop() {
        this.boss.getNavigation().stop();
    }

    private Player getNearestPlayerInRange(double range) {
        Player nearestPlayer = null;
        double nearestDistanceSq = range * range;

        for (Player player : this.boss.level.players()) {
            if (player.isAlive() && !player.isCreative() && !player.isSpectator()) {
                double distSq = this.boss.distanceToSqr(player);
                if (distSq < nearestDistanceSq) {
                    nearestDistanceSq = distSq;
                    nearestPlayer = player;
                }
            }
        }
        return nearestPlayer;
    }
}
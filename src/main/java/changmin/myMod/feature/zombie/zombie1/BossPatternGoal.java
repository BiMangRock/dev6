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
        // 💡 [조정] 넉백에 밀려나도 타겟팅 상태를 유지하기 위해 플레이어 탐색 사거리를 40블록으로 확장합니다.
        Player nearestPlayer = getNearestPlayerInRange(40.0D);
        if (nearestPlayer != null) {
            this.boss.setTarget(nearestPlayer);
        }

        LivingEntity target = this.boss.getTarget();
        return target != null && target.isAlive() && (target instanceof Player || target instanceof IAlly);
    }

    @Override
    public void tick() {
        // 실시간 넉백 밀려남 보정을 위해 공격 시 작동하는 감지 범위도 40블록 기준으로 동작합니다.
        Player nearestPlayer = getNearestPlayerInRange(40.0D);
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

            this.boss.getNavigation().stop();

            // 💡 [조정] 공격 가동 한계 검사 거리를 20블록(제곱 400)에서 40블록(제곱 1600.0)으로 확장했습니다.
            if (this.boss.getActiveAttack() == 0 && this.patternCooldown <= 0) {
                if (distanceSq < 1600.0D) {
                    int randomChoice = this.boss.getRandom().nextInt(3); // 0, 1, 2

                    if (randomChoice == 0) {
                        this.boss.triggerAttack2();
                        this.patternCooldown = 80;
                    } else if (randomChoice == 1) {
                        this.boss.triggerAttack3();
                        this.patternCooldown = 120;
                    } else {
                        this.boss.triggerAttack5();
                        this.patternCooldown = 130;
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
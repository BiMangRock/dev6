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
        // 💡 [추가] 아군(IAlly)을 상대로 대기 중이더라도 플레이어가 사정거리(20블록) 내로 들어오면 즉시 타겟을 가로챕니다 [1].
        Player nearestPlayer = getNearestPlayerInRange(20.0D);
        if (nearestPlayer != null) {
            this.boss.setTarget(nearestPlayer);
        }

        LivingEntity target = this.boss.getTarget();
        return target != null && target.isAlive() && (target instanceof Player || target instanceof IAlly);
    }

    @Override
    public void tick() {
        // 💡 [추가] 아군과 이미 난타전을 벌이는 도중일지라도, 매 틱마다 플레이어가 난입했는지 실시간 검사해 타겟을 강제 변경합니다.
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

            // 원거리 사격 시 행동 방랑을 멈추기 위해 정지 명령을 내립니다.
            this.boss.getNavigation().stop();

            // 쿨타임이 끝났고 사정거리 내에 플레이어 혹은 아군이 있다면 2, 3, 4 중 무작위 원거리 공격 실행
            if (this.boss.getActiveAttack() == 0 && this.patternCooldown <= 0) {
                if (distanceSq < 400.0D) { // 20블록 이내
                    int randomChoice = this.boss.getRandom().nextInt(3);

                    if (randomChoice == 0) {
                        this.boss.triggerAttack2();
                        this.patternCooldown = 80;
                    } else if (randomChoice == 1) {
                        this.boss.triggerAttack3();
                        this.patternCooldown = 120;
                    } else {
                        this.boss.triggerAttack4();
                        this.patternCooldown = 140;
                    }
                }
            }
        }
    }

    @Override
    public void stop() {
        this.boss.getNavigation().stop();
    }

    /**
     * 💡 [추가] 마인크래프트 버전에 따른 맵핑 충돌을 방지하기 위해
     * 월드 내 모든 플레이어를 추적하여 생존 상태이고 크리에이티브/관전자가 아닌 "가장 가까운 플레이어"를 수동 탐색합니다.
     */
    private Player getNearestPlayerInRange(double range) {
        Player nearestPlayer = null;
        double nearestDistanceSq = range * range; // 효율적인 비교를 위해 제곱 거리 사용

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
package changmin.myMod.feature.zombie.assassin2;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class Assassin2AttackGoal extends MeleeAttackGoal {
    private final Assassin2Entity assassin;
    private int animTicks = 0;             // 애니메이션 재생 상태 유지용 타이머
    private int damageDelayTicks = -1;     // 실제 데미지를 주기까지 남은 시간 타이머
    private LivingEntity attackTarget = null; // 데미지를 줄 타겟 임시 저장

    public Assassin2AttackGoal(Assassin2Entity assassin, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(assassin, speedModifier, followingTargetEvenIfNotSeen);
        this.assassin = assassin;
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
        double reach = this.getAttackReachSqr(enemy);

        // 아직 이전 공격 모션이 진행 중이 아니고, 공격 대기시간(쿨타임)이 끝났을 때만 새로운 공격 시작
        if (distToEnemySqr <= reach && this.getTicksUntilNextAttack() <= 0 && this.animTicks <= 0) {
            this.resetAttackCooldown();

            // 1. 공격 애니메이션 시작 신호를 클라이언트에 보냅니다.
            this.assassin.setAttacking(true);
            this.animTicks = 20; // 애니메이션의 총 길이 (20틱 = 1초)

            // 2. 💡 [지연 타격] 칼을 내리찍는 모션(0.5초 = 10틱 후)에 맞춰 데미지를 주도록 예약합니다.
            this.damageDelayTicks = 10;
            this.attackTarget = enemy;

            this.assassin.swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public void tick() {
        super.tick();

        // 전체 애니메이션 지속 시간 타이머 차감
        if (this.animTicks > 0) {
            this.animTicks--;
            if (this.animTicks <= 0) {
                this.assassin.setAttacking(false);
                this.attackTarget = null;
            }
        }

        // 💡 [지연 타격 처리] 예약된 시간이 흘러 0이 되는 타이밍에 실제로 공격 판정을 가합니다.
        if (this.damageDelayTicks > 0) {
            this.damageDelayTicks--;
            if (this.damageDelayTicks == 0) {
                if (this.attackTarget != null && this.attackTarget.isAlive()
                        && this.assassin.distanceToSqr(this.attackTarget) <= this.getAttackReachSqr(this.attackTarget)) {
                    // 단검이 내리꽂히는 시각적 타이밍에 정확히 데미지를 입힙니다!
                    this.assassin.doHurtTarget(this.attackTarget);
                }
                this.damageDelayTicks = -1;
            }
        }
    }

    @Override
    protected int getAttackInterval() {
        // 공격 주기를 30틱(1.5초)으로 늘려 공격 모션이 끝난 뒤 다음 공격을 시전하도록 여유를 둡니다.
        return 30;
    }

    @Override
    public void stop() {
        super.stop();
        this.assassin.setAttacking(false);
        this.animTicks = 0;
        this.damageDelayTicks = -1;
        this.attackTarget = null;
    }
}
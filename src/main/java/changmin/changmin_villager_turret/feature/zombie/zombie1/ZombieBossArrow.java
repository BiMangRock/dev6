package changmin.changmin_villager_turret.feature.zombie.zombie1;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class ZombieBossArrow extends Arrow {
    private int ticksAlive = 0;
    private int maxLifeTicks = 100; // 💡 [추가] 기본 수명을 5초(100틱)로 설정 [2]

    public ZombieBossArrow(Level level, LivingEntity shooter) {
        super(level, shooter);
    }

    // 💡 [추가] 특정 특수 화살만 수명을 개별적으로 덮어쓸 수 있도록 세터 메서드 추가 [2]
    public void setMaxLifeTicks(int maxLifeTicks) {
        this.maxLifeTicks = maxLifeTicks;
    }

    @Override
    public void tick() {
        super.tick();

        // 서버 측에서 화살의 수명을 계산하여 지정된 수명이 되면 자동 폐기 처리합니다 [2]
        if (!this.level.isClientSide) {
            this.ticksAlive++;
            if (this.ticksAlive >= this.maxLifeTicks) { // 💡 [수정] 고정값이 아닌 유동적인 변수 기준으로 작동합니다 [2]
                this.discard();
            }
        }
    }
}
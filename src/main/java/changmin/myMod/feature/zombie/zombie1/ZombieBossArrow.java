package changmin.myMod.feature.zombie.zombie1;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class ZombieBossArrow extends Arrow {
    private int ticksAlive = 0;

    public ZombieBossArrow(Level level, LivingEntity shooter) {
        super(level, shooter);
    }

    @Override
    public void tick() {
        super.tick();

        // 서버 측에서 화살의 수명을 계산하여 10초(200틱)가 되면 자동 폐기 처리합니다 [1.2.7]
        if (!this.level.isClientSide) {
            this.ticksAlive++;
            if (this.ticksAlive >= 200) {
                this.discard();
            }
        }
    }
}
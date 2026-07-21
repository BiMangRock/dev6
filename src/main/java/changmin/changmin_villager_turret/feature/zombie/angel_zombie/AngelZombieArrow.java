package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import changmin.changmin_villager_turret.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class AngelZombieArrow extends Arrow {
    // 레지스트리 등록을 위한 생성자
    public AngelZombieArrow(EntityType<? extends Arrow> type, Level level) {
        super(type, level);
    }

    // 소환 시 사용할 생성자
    public AngelZombieArrow(Level level, LivingEntity shooter) {
        super(ModEntityTypes.ANGEL_ZOMBIE_ARROW.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        // 서버 측에서 100틱(5초) 지나면 삭제
        if (!this.level.isClientSide) {
            if (this.tickCount >= 100) {
                this.discard();
            }
        }
    }
}
package changmin.changmin_villager_turret.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class StunEffect extends MobEffect {
    public StunEffect() {
        super(MobEffectCategory.HARMFUL, 0x55FFFF); // 전자기 기절 하늘색 빛깔
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
// 땅 위에 있지 않을 때만(공중에 떠 있을 때만) 아래로 당깁니다.
        if (!entity.isOnGround()) {
            entity.setDeltaMovement(0, -0.2D, 0);
        } else {
            // 이미 땅 위라면 수평 이동만 막습니다.
            entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
        }

        if (entity instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 효과가 지속되는 동안 매 틱마다 위 로직을 실행함
        return true;
    }
}
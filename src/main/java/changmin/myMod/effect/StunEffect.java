package changmin.myMod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class StunEffect extends MobEffect {
    public StunEffect() {
        super(MobEffectCategory.HARMFUL, 0x55FFFF); // 전자기 기절 하늘색 빛깔
    }

    // 1.18.2 바닐라 소스 기준, applyUpdateEffect가 아닌 applyEffectTick을 오버라이드해야 합니다.
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 수평 속도를 제어하여 제자리에 멈추게 함
        entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);

        if (entity instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
    }

    // 매 틱마다 위의 applyEffectTick이 무조건 실행되도록 true를 반환합니다.
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
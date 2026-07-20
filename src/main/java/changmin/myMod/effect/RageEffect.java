package changmin.myMod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class RageEffect extends MobEffect {
    public RageEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF3333);
    }

    // 타겟팅 및 AI 제어는 RageEffectHandler에서 실시간 주입 방식으로 안전하게 처리하므로,
    // 이곳의 applyEffectTick 메서드와 관련 임포트들은 모두 제거하셔도 됩니다.
}
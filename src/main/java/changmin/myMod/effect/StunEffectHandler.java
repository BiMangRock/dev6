package changmin.myMod.effect;

import changmin.myMod.MyMod;
import changmin.myMod.registry.ModEffects; // 👈 신규 생성한 ModEffects 클래스 임포트 추가
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StunEffectHandler {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            // 우리가 생성한 등록 객체(STUN)로부터 기절 여부를 정밀 확인
            if (attacker.hasEffect(ModEffects.STUN.get())) {
                event.setCanceled(true);
            }
        }
    }
}
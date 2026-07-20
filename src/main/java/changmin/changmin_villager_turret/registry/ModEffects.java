package changmin.changmin_villager_turret.registry;

import changmin.changmin_villager_turret.changmin_villager_turret;
import changmin.changmin_villager_turret.effect.StunEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import changmin.changmin_villager_turret.effect.RageEffect; // 👈 임포트 추가

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, changmin_villager_turret.MODID);

    // 기절(Stun) 포션 효과 등록
    public static final RegistryObject<MobEffect> STUN =
            MOB_EFFECTS.register("stun", StunEffect::new);

    // 💡 분노 상태효과(RAGE) 등록 추가
    public static final RegistryObject<MobEffect> RAGE = MOB_EFFECTS.register("rage", RageEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
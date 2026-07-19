package changmin.myMod.registry;

import changmin.myMod.MyMod;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, MyMod.MODID);

    // 💡 지속 시간 1분(1200틱)짜리 분노 포션 사양 등록
    public static final RegistryObject<Potion> RAGE_POTION = POTIONS.register("rage",
            () -> new Potion(new MobEffectInstance(ModEffects.RAGE.get(), 1200, 0)));

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}
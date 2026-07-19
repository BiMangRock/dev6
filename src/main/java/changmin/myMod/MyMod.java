package changmin.myMod;

import changmin.myMod.registry.ModBlocks;
import changmin.myMod.registry.ModEntityTypes;
import changmin.myMod.registry.ModItems;
import changmin.myMod.feature.turret.villager_turret.VillagerTurretEntity;
import changmin.myMod.feature.turret.villager_turret.VillagerTurretRenderer;
import changmin.myMod.feature.turret.resource_villager1.ResourceVillagerEntity;
import changmin.myMod.feature.turret.resource_villager1.ResourceVillagerRenderer;
import changmin.myMod.feature.turret.healer.HealerTurretEntity;
import changmin.myMod.feature.turret.healer.HealerRenderer;
// 🆕 삼지창 주민 터렛 관련 클래스 임포트 추가
import changmin.myMod.feature.turret.trident_turret.TridentTurretEntity;
import changmin.myMod.feature.turret.trident_turret.TridentTurretRenderer;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib3.GeckoLib;

@Mod(MyMod.MODID)
public class MyMod {
    public static final String MODID = "mymod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MyMod() {
        GeckoLib.initialize();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntityTypes.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("MyMod Setup Complete");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntityTypes.VILLAGER_TURRET.get(), VillagerTurretRenderer::new);
        EntityRenderers.register(ModEntityTypes.RESOURCE_VILLAGER.get(), ResourceVillagerRenderer::new);
        EntityRenderers.register(ModEntityTypes.HEALER_TURRET.get(), HealerRenderer::new);

        // 🆕 삼지창 주민 터렛 렌더러 등록 추가
        EntityRenderers.register(ModEntityTypes.TRIDENT_TURRET.get(), TridentTurretRenderer::new);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {

        @SubscribeEvent
        public static void onAttributeCreate(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.VILLAGER_TURRET.get(), VillagerTurretEntity.createAttributes().build());
            event.put(ModEntityTypes.RESOURCE_VILLAGER.get(), ResourceVillagerEntity.createAttributes().build());
            event.put(ModEntityTypes.HEALER_TURRET.get(), HealerTurretEntity.createAttributes().build());

            // 🆕 삼지창 주민 터렛 기초 속성 등록 추가
            event.put(ModEntityTypes.TRIDENT_TURRET.get(), TridentTurretEntity.createAttributes().build());
        }
    }
}
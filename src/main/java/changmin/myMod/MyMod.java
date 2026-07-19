package changmin.myMod;

import changmin.myMod.registry.ModBlocks;
import changmin.myMod.registry.ModEntityTypes;
import changmin.myMod.registry.ModItems;
import changmin.myMod.registry.ModEffects;
import changmin.myMod.feature.turret.villager_turret.VillagerTurretEntity;
import changmin.myMod.feature.turret.villager_turret.VillagerTurretRenderer;
import changmin.myMod.feature.turret.resource_villager1.ResourceVillagerEntity;
import changmin.myMod.feature.turret.resource_villager1.ResourceVillagerRenderer;
import changmin.myMod.feature.turret.healer.HealerTurretEntity;
import changmin.myMod.feature.turret.healer.HealerRenderer;
import changmin.myMod.feature.turret.trident_turret.TridentTurretEntity;
import changmin.myMod.feature.turret.trident_turret.TridentTurretRenderer;
import changmin.myMod.feature.turret.tanker.TankerTurretEntity;
import changmin.myMod.feature.turret.tanker.TankerTurretRenderer;
import changmin.myMod.feature.turret.lightning_wizard.LightningWizardEntity;
import changmin.myMod.feature.turret.lightning_wizard.LightningWizardRenderer;
import changmin.myMod.feature.turret.plasma_wizard.*;


import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
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
        ModEffects.register(modEventBus);

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
        EntityRenderers.register(ModEntityTypes.TRIDENT_TURRET.get(), TridentTurretRenderer::new);
        EntityRenderers.register(ModEntityTypes.TANKER_TURRET.get(), TankerTurretRenderer::new);
        EntityRenderers.register(ModEntityTypes.LIGHTNING_WIZARD.get(), LightningWizardRenderer::new);
        EntityRenderers.register(ModEntityTypes.LIGHTNING_PROJECTILE.get(), NoopRenderer::new);

        // 🆕 플라즈마 마법사 및 게코립 3D 투사체 렌더러 등록 추가
        EntityRenderers.register(ModEntityTypes.PLASMA_WIZARD.get(), PlasmaWizardRenderer::new);
        EntityRenderers.register(ModEntityTypes.PLASMA_ORB.get(), PlasmaOrbRenderer::new);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {

        @SubscribeEvent
        public static void onAttributeCreate(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.VILLAGER_TURRET.get(), VillagerTurretEntity.createAttributes().build());
            event.put(ModEntityTypes.RESOURCE_VILLAGER.get(), ResourceVillagerEntity.createAttributes().build());
            event.put(ModEntityTypes.HEALER_TURRET.get(), HealerTurretEntity.createAttributes().build());
            event.put(ModEntityTypes.TRIDENT_TURRET.get(), TridentTurretEntity.createAttributes().build());
            event.put(ModEntityTypes.TANKER_TURRET.get(), TankerTurretEntity.createAttributes().build());
            event.put(ModEntityTypes.LIGHTNING_WIZARD.get(), LightningWizardEntity.createAttributes().build());

            // 🆕 플라즈마 마법사 기본 능력치 정식 매핑 등록
            event.put(ModEntityTypes.PLASMA_WIZARD.get(), PlasmaWizardEntity.createAttributes().build());
        }
    }
}
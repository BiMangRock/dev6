package changmin.myMod;

import changmin.myMod.registry.ModBlocks;
import changmin.myMod.registry.ModEntityTypes;
import changmin.myMod.registry.ModItems;
import changmin.myMod.registry.ModEffects; // 🆕 새로 만든 기절 포션 효과 등록 임포트
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
// 🆕 번개 마법사 관련 클래스 임포트 추가
import changmin.myMod.feature.turret.lightning_wizard.LightningWizardEntity;
import changmin.myMod.feature.turret.lightning_wizard.LightningWizardRenderer;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer; // 🆕 투명 렌더러 임포트
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
        ModEffects.register(modEventBus); // 🆕 기절 효과 레지스트리 버스 등록 추가!

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

        // 🆕 번개 마법사 렌더러 등록 추가
        EntityRenderers.register(ModEntityTypes.LIGHTNING_WIZARD.get(), LightningWizardRenderer::new);

        // 🆕 투사체(화살) 자체 모델링을 렌더링하지 않도록 빈 NoopRenderer를 연결합니다.
        // 이를 통해 게임 상에서는 화살 텍스처 없이 "오직 푸른 스파크 파티클 꼬리만" 날아다니게 연출됩니다.
        EntityRenderers.register(ModEntityTypes.LIGHTNING_PROJECTILE.get(), NoopRenderer::new);
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

            // 🆕 번개 마법사 터렛 기초 속성 등록 추가
            event.put(ModEntityTypes.LIGHTNING_WIZARD.get(), LightningWizardEntity.createAttributes().build());
        }
    }
}
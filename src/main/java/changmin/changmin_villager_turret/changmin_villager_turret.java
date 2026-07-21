package changmin.changmin_villager_turret;

import changmin.changmin_villager_turret.feature.zombie.Apostle_of_the_End.ApostleOfTheEndEntity;
import changmin.changmin_villager_turret.feature.zombie.assassin2.Assassin2Entity;
import changmin.changmin_villager_turret.registry.*;
import changmin.changmin_villager_turret.feature.turret.villager_turret.VillagerTurretEntity;
import changmin.changmin_villager_turret.feature.turret.villager_turret.VillagerTurretRenderer;
import changmin.changmin_villager_turret.feature.turret.resource_villager1.ResourceVillagerEntity;
import changmin.changmin_villager_turret.feature.turret.resource_villager1.ResourceVillagerRenderer;
import changmin.changmin_villager_turret.feature.turret.healer.HealerTurretEntity;
import changmin.changmin_villager_turret.feature.turret.healer.HealerRenderer;
import changmin.changmin_villager_turret.feature.turret.trident_turret.TridentTurretEntity;
import changmin.changmin_villager_turret.feature.turret.trident_turret.TridentTurretRenderer;
import changmin.changmin_villager_turret.feature.turret.tanker.TankerTurretEntity;
import changmin.changmin_villager_turret.feature.turret.tanker.TankerTurretRenderer;
import changmin.changmin_villager_turret.feature.turret.lightning_wizard.LightningWizardEntity;
import changmin.changmin_villager_turret.feature.turret.lightning_wizard.LightningWizardRenderer;
import changmin.changmin_villager_turret.feature.turret.plasma_wizard.*;

// 🆕 벌 소환사 터렛 관련 패키지 일괄 임포트 추가
import changmin.changmin_villager_turret.feature.turret.bee_summoner_turret.*;

import changmin.changmin_villager_turret.util.BetterBrewingRecipe;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib3.GeckoLib;
import changmin.changmin_villager_turret.feature.zombie.assassin2.Assassin2Renderer;
import changmin.changmin_villager_turret.feature.zombie.angel_zombie.*;
import changmin.changmin_villager_turret.feature.zombie.Apostle_of_the_End.*;

@Mod(changmin_villager_turret.MODID)
public class changmin_villager_turret {
    public static final String MODID = "changmin_villager_turret";
    private static final Logger LOGGER = LogUtils.getLogger();

    public changmin_villager_turret() {
        GeckoLib.initialize();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntityTypes.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEffects.register(modEventBus);
        ModPotions.register(modEventBus); // 💡 포션 레지스트리 버스 등록 추가

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // 💡 [컴파일 에러 우회 해결]: 포지 공식 레지스트리에 헬퍼 객체를 등록하여 private 제한을 우회합니다.
            // 조합 공식: 어색한 물약(AWKWARD) + 레드스톤 가루(REDSTONE) ➔ 우리가 등록한 분노 물약(RAGE_POTION)
            BrewingRecipeRegistry.addRecipe(new BetterBrewingRecipe(
                    net.minecraft.world.item.alchemy.Potions.AWKWARD,
                    net.minecraft.world.item.Items.REDSTONE,
                    ModPotions.RAGE_POTION.get()
            ));

            // 💡 벌 소환사 터렛 내부에서 소환할 벌의 EntityType이 null이 되지 않도록 연동합니다.
            BeeSummonerTurretEntity.SUMMONED_BEE_TYPE = ModEntityTypes.SUMMONED_BEE.get();
        });
        LOGGER.info("changmin_villager_turret Setup Complete");
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

        EntityRenderers.register(ModEntityTypes.ASSASSIN2.get(), Assassin2Renderer::new);

        // 🆕 벌 소환사 터렛 및 소환되는 벌 클라이언트 렌더러 등록
        EntityRenderers.register(ModEntityTypes.BEE_SUMMONER_TURRET.get(), BeeSummonerTurretRenderer::new);
        EntityRenderers.register(ModEntityTypes.SUMMONED_BEE.get(), SummonedBeeRenderer::new);

        EntityRenderers.register(ModEntityTypes.ANGEL_ZOMBIE.get(), AngelZombieRenderer::new);
        EntityRenderers.register(ModEntityTypes.ANGEL_ZOMBIE_ARROW.get(), net.minecraft.client.renderer.entity.TippableArrowRenderer::new);

        // 🆕 도넛 충격파 전용 렌더러 등록
        EntityRenderers.register(ModEntityTypes.SHOCKWAVE.get(), ShockwaveRenderer::new);

        // clientSetup 메서드 안에 추가
        EntityRenderers.register(ModEntityTypes.APOSTLE_OF_THE_END.get(), ApostleOfTheEndRenderer::new);
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

            event.put(ModEntityTypes.ASSASSIN2.get(), Assassin2Entity.createAttributes().build());

            // 🆕 벌 소환사 터렛 및 소환되는 벌의 기본 능력치 정보 등록
            event.put(ModEntityTypes.BEE_SUMMONER_TURRET.get(), BeeSummonerTurretEntity.createAttributes().build());
            event.put(ModEntityTypes.SUMMONED_BEE.get(), SummonedBeeEntity.createAttributes().build());

            event.put(ModEntityTypes.ANGEL_ZOMBIE.get(), AngelZombieEntity.createAttributes().build());

            // onAttributeCreate 메서드 안에 추가
            event.put(ModEntityTypes.APOSTLE_OF_THE_END.get(), ApostleOfTheEndEntity.createAttributes().build());
        }
    }
}
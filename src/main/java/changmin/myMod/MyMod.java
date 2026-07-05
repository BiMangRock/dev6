package changmin.myMod;

import changmin.myMod.registry.ModBlocks;
import changmin.myMod.registry.ModEntityTypes;
import changmin.myMod.registry.ModItems;
import changmin.myMod.feature.turret.villager_turret.VillagerTurretEntity;
import changmin.myMod.feature.turret.villager_turret.VillagerTurretRenderer;
import changmin.myMod.feature.turret.resource_villager1.ResourceVillagerEntity;   // 🆕 신규 임포트 추가
import changmin.myMod.feature.turret.resource_villager1.ResourceVillagerRenderer; // 🆕 신규 임포트 추가
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
        GeckoLib.initialize(); // 생성자 가장 위에 추가
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntityTypes.register(modEventBus);
        ModBlocks.register(modEventBus); // 🆕 신규 추가: 블록 등록 버스를 아이템보다 먼저 실행합니다.
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
        // 🆕 자원 수확 주민 렌더러 등록 추가
        EntityRenderers.register(ModEntityTypes.RESOURCE_VILLAGER.get(), ResourceVillagerRenderer::new);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {

        @SubscribeEvent
        public static void onAttributeCreate(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.VILLAGER_TURRET.get(), VillagerTurretEntity.createAttributes().build());
            // 🆕 자원 수확 주민 기초 속성 등록 추가
            event.put(ModEntityTypes.RESOURCE_VILLAGER.get(), ResourceVillagerEntity.createAttributes().build());
        }
    }
}
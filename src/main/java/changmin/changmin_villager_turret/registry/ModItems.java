package changmin.changmin_villager_turret.registry;

import changmin.changmin_villager_turret.changmin_villager_turret;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, changmin_villager_turret.MODID);

    // 주민 터렛 스폰알
    public static final RegistryObject<Item> VILLAGER_TURRET_SPAWN_EGG =
            ITEMS.register("villager_turret_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.VILLAGER_TURRET, 0x563C33, 0xBD8B72,
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 좀비 보스 스폰알
    public static final RegistryObject<Item> ZOMBIE1_SPAWN_EGG =
            ITEMS.register("zombie1_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.ZOMBIE_BOSS, 0x1E3B20, 0x8A1212,
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 🆕 자원 수확 주민 터렛 스폰알 등록 (초록색 테마)
    public static final RegistryObject<Item> RESOURCE_VILLAGER_SPAWN_EGG =
            ITEMS.register("resource_villager_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.RESOURCE_VILLAGER, 0x1F5C1F, 0x5C9C5C,
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 등급별 터렛 포인트 토큰 3종 등록
    public static final RegistryObject<Item> TURRET_POINT_TOKEN_LOW =
            ITEMS.register("turret_point_token_low",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> TURRET_POINT_TOKEN_MID =
            ITEMS.register("turret_point_token_mid",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> TURRET_POINT_TOKEN_HIGH =
            ITEMS.register("turret_point_token_high",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 응축된 에메랄드 아이템 등록
    public static final RegistryObject<Item> CONDENSED_EMERALD =
            ITEMS.register("condensed_emerald",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 터렛 부품 거래소(자판기) 블록 아이템 등록
    public static final RegistryObject<Item> TURRET_STORE_BLOCK_ITEM =
            ITEMS.register("turret_store",
                    () -> new BlockItem(ModBlocks.TURRET_STORE.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 화살 비행 속도 업그레이드서
    public static final RegistryObject<Item> SPEED_UPGRADE =
            ITEMS.register("speed_upgrade",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 재장전 속도 업그레이드서
    public static final RegistryObject<Item> RECHARGE_UPGRADE =
            ITEMS.register("recharge_upgrade",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 일직선 점사 패턴 업그레이드서
    public static final RegistryObject<Item> BURST_UPGRADE =
            ITEMS.register("burst_upgrade",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 부채꼴 발사 패턴 업그레이드서
    public static final RegistryObject<Item> FAN_UPGRADE =
            ITEMS.register("fan_upgrade",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 치명적인 독 화살 업그레이드서
    public static final RegistryObject<Item> POISON_ARROW_UPGRADE =
            ITEMS.register("poison_arrow_upgrade",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));



    // 🆕 치유 전용 터렛 포인트 토큰 3종 신규 등록
    public static final RegistryObject<Item> HEALER_POINT_TOKEN_LOW =
            ITEMS.register("healer_point_token_low",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> HEALER_POINT_TOKEN_MID =
            ITEMS.register("healer_point_token_mid",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> HEALER_POINT_TOKEN_HIGH =
            ITEMS.register("healer_point_token_high",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));


    // 🆕 삼지창 주민 터렛 스폰알 등록 (바다 청록색 테마)
    public static final RegistryObject<Item> TRIDENT_TURRET_SPAWN_EGG =
            ITEMS.register("trident_turret_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.TRIDENT_TURRET, 0x1A4D62, 0x56C2D6,
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 🆕 삼지창 전용 터렛 포인트 토큰 3종 등록
    public static final RegistryObject<Item> TRIDENT_POINT_TOKEN_LOW =
            ITEMS.register("trident_point_token_low",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> TRIDENT_POINT_TOKEN_MID =
            ITEMS.register("trident_point_token_mid",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> TRIDENT_POINT_TOKEN_HIGH =
            ITEMS.register("trident_point_token_high",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));


    // 🆕 탱커 주민 터렛 스폰알 등록 (단단한 무쇠 회색 0x4C515B와 주황색 불꽃 0xFF8822 테마)
    public static final RegistryObject<Item> TANKER_TURRET_SPAWN_EGG =
            ITEMS.register("tanker_turret_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.TANKER_TURRET, 0x4C515B, 0xFF8822,
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));


    // 🆕 탱커 전용 터렛 포인트 토큰 3종 등록
    public static final RegistryObject<Item> TANKER_POINT_TOKEN_LOW =
            ITEMS.register("tanker_point_token_low",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> TANKER_POINT_TOKEN_MID =
            ITEMS.register("tanker_point_token_mid",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> TANKER_POINT_TOKEN_HIGH =
            ITEMS.register("tanker_point_token_high",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));


    // 🆕 번개 마법사 스폰알 등록 (전자기 하늘색 0x00E1FF과 마법 보라색 0x7E00FF 테마)
    public static final RegistryObject<Item> LIGHTNING_WIZARD_SPAWN_EGG =
            ITEMS.register("lightning_wizard_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.LIGHTNING_WIZARD, 0x00E1FF, 0x7E00FF,
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));



    // 🆕 번개 마법사 전용 터렛 포인트 토큰 3종 등록
    public static final RegistryObject<Item> LIGHTNING_POINT_TOKEN_LOW =
            ITEMS.register("lightning_point_token_low",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> LIGHTNING_POINT_TOKEN_MID =
            ITEMS.register("lightning_point_token_mid",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> LIGHTNING_POINT_TOKEN_HIGH =
            ITEMS.register("lightning_point_token_high",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 🆕 플라즈마 마법사 터렛 스폰알 등록
    public static final RegistryObject<Item> PLASMA_WIZARD_SPAWN_EGG =
            ITEMS.register("plasma_wizard_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.PLASMA_WIZARD, 0x00E1FF, 0x002B47,
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> PLASMA_POINT_TOKEN_LOW = ITEMS.register("plasma_point_token_low",
            () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> PLASMA_POINT_TOKEN_MID = ITEMS.register("plasma_point_token_mid",
            () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> PLASMA_POINT_TOKEN_HIGH = ITEMS.register("plasma_point_token_high",
            () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> ASSASSIN2_SPAWN_EGG = ITEMS.register("assassin2_spawn_egg", ()
            -> new ForgeSpawnEggItem(ModEntityTypes.ASSASSIN2, 0x1D2D1B, 0xE2C055,
            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> BEE_TURRET_SPAWN_EGG =
            ITEMS.register("bee_turret_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.BEE_SUMMONER_TURRET, 0xEAF2B0, 0xF2CD5C,
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> BEE_POINT_TOKEN_LOW =
            ITEMS.register("bee_point_token_low",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> BEE_POINT_TOKEN_MID =
            ITEMS.register("bee_point_token_mid",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> BEE_POINT_TOKEN_HIGH =
            ITEMS.register("bee_point_token_high",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> ANGEL_ZOMBIE_SPAWN_EGG =
            ITEMS.register("angel_zombie_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.ANGEL_ZOMBIE, 0xFFFFFF, 0x445621,
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> APOSTLE_OF_THE_END_SPAWN_EGG =
            ITEMS.register("apostle_of_the_end_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.APOSTLE_OF_THE_END, 0x222222, 0xFFD700,
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static final RegistryObject<Item> HEALER_TURRET_SPAWN_EGG = ITEMS.register("healer_turret_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntityTypes.HEALER_TURRET, // 스폰할 엔티티 지정
                    0xFF5555,                    // 알의 바탕색 (예: 연성직자 붉은색 테마)
                    0x55FF55,                    // 알의 점박이 색 (예: 치유 초록색 테마)
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC) // 크리에이티브 기타 탭에 등록
            ));
}
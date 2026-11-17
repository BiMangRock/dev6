package changmin.changmin_villager_turret.registry;

import changmin.changmin_villager_turret.changmin_villager_turret;
import changmin.changmin_villager_turret.feature.hero.healer_hero.GuardLinkerItem;
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
                            new Item.Properties()));

    // 좀비 보스 스폰알
    public static final RegistryObject<Item> ZOMBIE1_SPAWN_EGG =
            ITEMS.register("zombie1_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.ZOMBIE_BOSS, 0x1E3B20, 0x8A1212,
                            new Item.Properties()));

    // 자원 수확 주민 터렛 스폰알 등록 (초록색 테마)
    public static final RegistryObject<Item> RESOURCE_VILLAGER_SPAWN_EGG =
            ITEMS.register("resource_villager_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.RESOURCE_VILLAGER, 0x1F5C1F, 0x5C9C5C,
                            new Item.Properties()));

    // 등급별 터렛 포인트 토큰 3종 등록
    public static final RegistryObject<Item> TURRET_POINT_TOKEN_LOW =
            ITEMS.register("turret_point_token_low",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TURRET_POINT_TOKEN_MID =
            ITEMS.register("turret_point_token_mid",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TURRET_POINT_TOKEN_HIGH =
            ITEMS.register("turret_point_token_high",
                    () -> new Item(new Item.Properties()));

    // 응축된 에메랄드 아이템 등록
    public static final RegistryObject<Item> CONDENSED_EMERALD =
            ITEMS.register("condensed_emerald",
                    () -> new Item(new Item.Properties()));

    // 🆕 상점 주민 스폰알 등록 (나무 판자 3개로 조합될 예정)
    public static final RegistryObject<Item> TURRET_STORE_SPAWN_EGG =
            ITEMS.register("turret_store_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.TURRET_STORE, 0x3E2723, 0xEAC15C,
                            new Item.Properties()));

    // 화살 비행 속도 업그레이드서
    public static final RegistryObject<Item> SPEED_UPGRADE =
            ITEMS.register("speed_upgrade",
                    () -> new Item(new Item.Properties()));

    // 재장전 속도 업그레이드서
    public static final RegistryObject<Item> RECHARGE_UPGRADE =
            ITEMS.register("recharge_upgrade",
                    () -> new Item(new Item.Properties()));

    // 일직선 점사 패턴 업그레이드서
    public static final RegistryObject<Item> BURST_UPGRADE =
            ITEMS.register("burst_upgrade",
                    () -> new Item(new Item.Properties()));

    // 부채꼴 발사 패턴 업그레이드서
    public static final RegistryObject<Item> FAN_UPGRADE =
            ITEMS.register("fan_upgrade",
                    () -> new Item(new Item.Properties()));

    // 치명적인 독 화살 업그레이드서
    public static final RegistryObject<Item> POISON_ARROW_UPGRADE =
            ITEMS.register("poison_arrow_upgrade",
                    () -> new Item(new Item.Properties()));



    // 치유 전용 터렛 포인트 토큰 3종 신규 등록
    public static final RegistryObject<Item> HEALER_POINT_TOKEN_LOW =
            ITEMS.register("healer_point_token_low",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HEALER_POINT_TOKEN_MID =
            ITEMS.register("healer_point_token_mid",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HEALER_POINT_TOKEN_HIGH =
            ITEMS.register("healer_point_token_high",
                    () -> new Item(new Item.Properties()));


    // 삼지창 주민 터렛 스폰알 등록 (바다 청록색 테마)
    public static final RegistryObject<Item> TRIDENT_TURRET_SPAWN_EGG =
            ITEMS.register("trident_turret_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.TRIDENT_TURRET, 0x1A4D62, 0x56C2D6,
                            new Item.Properties()));

    // 삼지창 전용 터렛 포인트 토큰 3종 등록
    public static final RegistryObject<Item> TRIDENT_POINT_TOKEN_LOW =
            ITEMS.register("trident_point_token_low",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TRIDENT_POINT_TOKEN_MID =
            ITEMS.register("trident_point_token_mid",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TRIDENT_POINT_TOKEN_HIGH =
            ITEMS.register("trident_point_token_high",
                    () -> new Item(new Item.Properties()));


    // 탱커 주민 터렛 스폰알 등록 (단단한 무쇠 회색 0x4C515B와 주황색 불꽃 0xFF8822 테마)
    public static final RegistryObject<Item> TANKER_TURRET_SPAWN_EGG =
            ITEMS.register("tanker_turret_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.TANKER_TURRET, 0x4C515B, 0xFF8822,
                            new Item.Properties()));


    // 탱커 전용 터렛 포인트 토큰 3종 등록
    public static final RegistryObject<Item> TANKER_POINT_TOKEN_LOW =
            ITEMS.register("tanker_point_token_low",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TANKER_POINT_TOKEN_MID =
            ITEMS.register("tanker_point_token_mid",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TANKER_POINT_TOKEN_HIGH =
            ITEMS.register("tanker_point_token_high",
                    () -> new Item(new Item.Properties()));


    // 번개 마법사 스폰알 등록 (전자기 하늘색 0x00E1FF과 마법 보라색 0x7E00FF 테마)
    public static final RegistryObject<Item> LIGHTNING_WIZARD_SPAWN_EGG =
            ITEMS.register("lightning_wizard_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.LIGHTNING_WIZARD, 0x00E1FF, 0x7E00FF,
                            new Item.Properties()));



    // 번개 마법사 전용 터렛 포인트 토큰 3종 등록
    public static final RegistryObject<Item> LIGHTNING_POINT_TOKEN_LOW =
            ITEMS.register("lightning_point_token_low",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> LIGHTNING_POINT_TOKEN_MID =
            ITEMS.register("lightning_point_token_mid",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> LIGHTNING_POINT_TOKEN_HIGH =
            ITEMS.register("lightning_point_token_high",
                    () -> new Item(new Item.Properties()));

    // 플라즈마 마법사 터렛 스폰알 등록
    public static final RegistryObject<Item> PLASMA_WIZARD_SPAWN_EGG =
            ITEMS.register("plasma_wizard_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.PLASMA_WIZARD, 0x00E1FF, 0x002B47,
                            new Item.Properties()));

    public static final RegistryObject<Item> PLASMA_POINT_TOKEN_LOW = ITEMS.register("plasma_point_token_low",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> PLASMA_POINT_TOKEN_MID = ITEMS.register("plasma_point_token_mid",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> PLASMA_POINT_TOKEN_HIGH = ITEMS.register("plasma_point_token_high",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ASSASSIN2_SPAWN_EGG = ITEMS.register("assassin2_spawn_egg", ()
            -> new ForgeSpawnEggItem(ModEntityTypes.ASSASSIN2, 0x1D2D1B, 0xE2C055,
            new Item.Properties()));

    public static final RegistryObject<Item> BEE_TURRET_SPAWN_EGG =
            ITEMS.register("bee_turret_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.BEE_SUMMONER_TURRET, 0xEAF2B0, 0xF2CD5C,
                            new Item.Properties()));

    public static final RegistryObject<Item> BEE_POINT_TOKEN_LOW =
            ITEMS.register("bee_point_token_low",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BEE_POINT_TOKEN_MID =
            ITEMS.register("bee_point_token_mid",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BEE_POINT_TOKEN_HIGH =
            ITEMS.register("bee_point_token_high",
                    () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ANGEL_ZOMBIE_SPAWN_EGG =
            ITEMS.register("angel_zombie_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.ANGEL_ZOMBIE, 0xFFFFFF, 0x445621,
                            new Item.Properties()));

    public static final RegistryObject<Item> APOSTLE_OF_THE_END_SPAWN_EGG =
            ITEMS.register("apostle_of_the_end_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.APOSTLE_OF_THE_END, 0x222222, 0xFFD700,
                            new Item.Properties()));

    public static final RegistryObject<Item> RAGED_ANGEL_ZOMBIE_SPAWN_EGG =
            ITEMS.register("raged_angel_zombie_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.RAGED_ANGEL_ZOMBIE, 0x8B0000, 0x445621,
                            new Item.Properties()));

    public static final RegistryObject<Item> CREAKING_SPAWN_EGG =
            ITEMS.register("creaking_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.CREAKING, 0x3E382F, 0xE9B113,
                            new Item.Properties()));

    // 악마 1 스폰알 등록 (진한 자색 0x1E0D1C 과 붉은 피색 0x8A1212 테마)
    public static final RegistryObject<Item> DEMON1_SPAWN_EGG =
            ITEMS.register("demon1_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.DEMON1, 0x1E0D1C, 0x8A1212,
                            new Item.Properties()));

    // 주황색 바탕 0xFF8500 에 황금빛 불꽃 무늬 0xFFD700 스폰알 등록
    public static final RegistryObject<Item> GODDESS_OF_FLAME_SPAWN_EGG =
            ITEMS.register("goddess_of_flame_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.GODDESS_OF_FLAME, 0xFF8500, 0xFFD700,
                            new Item.Properties()));

    public static final RegistryObject<Item> ZOMBIE_COMMANDER_SPAWN_EGG =
            ITEMS.register("zombie_commander_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.ZOMBIE_COMMANDER, 0x1D2D1B, 0xDAA520,
                            new Item.Properties()));

    // 💡 [수정 완료] 중복된 ID를 고유 ID인 "healer_zombie_spawn_egg"로 수정하여 빌드 오류를 방지했습니다.
    public static final RegistryObject<Item> HEALER_ZOMBIE_SPAWN_EGG =
            ITEMS.register("healer_zombie_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.HEALER_ZOMBIE, 0x1D2D1B, 0xFFFFB6C1,
                            new Item.Properties()));

    // 힐러 히어로 스폰알 등록
    public static final RegistryObject<Item> HEALER_HERO_SPAWN_EGG =
            ITEMS.register("healer_hero_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.HEALER_HERO, 0xFFC0CB, 0xFF69B4,
                            new Item.Properties()));

    // 경비병 임명장(연결 도구) 등록
    public static final RegistryObject<Item> GUARD_LINKER =
            ITEMS.register("guard_linker",
                    () -> new GuardLinkerItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        ModCreativeTabs.TABS.register(eventBus); // 1.19.3+: 크리에이티브 탭은 별도 레지스트리로 등록


    }

    public static final RegistryObject<Item> RESOURCE_VILLAGER_EASY_SPAWN_EGG =
            ITEMS.register("resource_villager_easy_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.RESOURCE_VILLAGER_EASY, 0x1F5C1F, 0xADF8AD, // 연한 초록 테마 색상 적용
                            new Item.Properties()));



    public static final RegistryObject<Item> HEALER_TURRET_SPAWN_EGG = ITEMS.register("healer_turret_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntityTypes.HEALER_TURRET,
                    0xFF5555,
                    0x55FF55,
                    new Item.Properties()
            ));
}
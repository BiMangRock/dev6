package changmin.myMod.registry;

import changmin.myMod.MyMod;
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
            DeferredRegister.create(ForgeRegistries.ITEMS, MyMod.MODID);

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

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
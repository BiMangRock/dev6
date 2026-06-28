package changmin.myMod.registry;

import changmin.myMod.MyMod;
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

    // 🆕 좀비 보스 스폰알 (zombie1_spawn_egg)
    // 앞부분 색상(0x1E3B20)은 달걀 기본색, 뒷부분 색상(0x8A1212)은 달걀 무늬색입니다. 마음에 드는 색상 코드로 자유롭게 변경 가능합니다.
    public static final RegistryObject<Item> ZOMBIE1_SPAWN_EGG =
            ITEMS.register("zombie1_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.ZOMBIE_BOSS, 0x1E3B20, 0x8A1212,
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 🆕 1. 터렛 포인트 토큰 (레벨업 보상)
    public static final RegistryObject<Item> TURRET_POINT_TOKEN =
            ITEMS.register("turret_point_token",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 🆕 2. 화살 비행 속도 업그레이드서
    public static final RegistryObject<Item> SPEED_UPGRADE =
            ITEMS.register("speed_upgrade",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 🆕 3. 재장전 속도 업그레이드서
    public static final RegistryObject<Item> RECHARGE_UPGRADE =
            ITEMS.register("recharge_upgrade",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 🆕 4. 일직선 점사 패턴 업그레이드서
    public static final RegistryObject<Item> BURST_UPGRADE =
            ITEMS.register("burst_upgrade",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 🆕 5. 부채꼴 발사 패턴 업그레이드서
    public static final RegistryObject<Item> FAN_UPGRADE =
            ITEMS.register("fan_upgrade",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    // 🆕 6. 치명적인 독 화살 업그레이드서
    public static final RegistryObject<Item> POISON_ARROW_UPGRADE =
            ITEMS.register("poison_arrow_upgrade",
                    () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
package changmin.myMod.feature.turret;

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

    public static final RegistryObject<Item> VILLAGER_TURRET_SPAWN_EGG =
            ITEMS.register("villager_turret_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntityTypes.VILLAGER_TURRET, 0x563C33, 0xBD8B72,
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
package changmin.changmin_villager_turret.registry;

import changmin.changmin_villager_turret.changmin_villager_turret;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 🗂️ 크리에이티브 탭 (1.19.3+ 방식)
 *
 * 1.18.2에서는 아이템마다 .tab(CreativeModeTab.TAB_MISC)로 "기타" 탭에 넣었지만,
 * 1.20.1에서는 그 방식이 없어져서 이 모드 전용 탭을 하나 만들고 등록된 아이템을 전부 넣습니다.
 * 탭 이름은 언어 파일의 itemGroup.changmin_villager_turret 값입니다.
 */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, changmin_villager_turret.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + changmin_villager_turret.MODID))
            .icon(() -> new ItemStack(ModItems.VILLAGER_TURRET_SPAWN_EGG.get()))
            .displayItems((parameters, output) -> ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build());

    private ModCreativeTabs() {}
}

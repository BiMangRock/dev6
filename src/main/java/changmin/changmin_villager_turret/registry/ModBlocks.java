package changmin.changmin_villager_turret.registry;

import changmin.changmin_villager_turret.changmin_villager_turret;
import changmin.changmin_villager_turret.feature.turret.store.TurretStoreBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, changmin_villager_turret.MODID);

    // 🆕 터렛 부품 거래소(자판기) 블록 등록
    public static final RegistryObject<Block> TURRET_STORE = BLOCKS.register("turret_store",
            () -> new TurretStoreBlock(BlockBehaviour.Properties.of(Material.STONE)
                    .strength(3.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion())); // 바닐라 석재 절단기처럼 속이 보이는 비정형 블록 설정을 위해 noOcclusion 적용

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
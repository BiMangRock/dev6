package changmin.myMod.registry;

import changmin.myMod.MyMod;
import changmin.myMod.feature.turret.villager_turret.VillagerTurretEntity;
import changmin.myMod.feature.zombie.zombie1.ZombieBossEntity; // 🆕 보스 클래스 임포트
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, MyMod.MODID);

    // 주민 터렛 등록
    public static final RegistryObject<EntityType<VillagerTurretEntity>> VILLAGER_TURRET =
            ENTITY_TYPES.register("villager_turret",
                    () -> EntityType.Builder.of(VillagerTurretEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .build("villager_turret"));

    // 🆕 좀비 보스 등록 (몬스터 카테고리로 설정)
    public static final RegistryObject<EntityType<ZombieBossEntity>> ZOMBIE_BOSS =
            ENTITY_TYPES.register("zombie_boss",
                    () -> EntityType.Builder.of(ZombieBossEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F) // 가로 0.6블록, 세로 1.95블록 (원하는 크기로 조절 가능)
                            .build("zombie_boss"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
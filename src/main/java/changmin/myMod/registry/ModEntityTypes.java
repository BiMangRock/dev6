package changmin.myMod.registry;

import changmin.myMod.MyMod;
import changmin.myMod.feature.turret.healer.HealerTurretEntity;
import changmin.myMod.feature.turret.trident_turret.TridentTurretEntity;
import changmin.myMod.feature.turret.villager_turret.VillagerTurretEntity;
import changmin.myMod.feature.turret.resource_villager1.ResourceVillagerEntity; // 🆕 신규 임포트 추가
import changmin.myMod.feature.zombie.zombie1.ZombieBossEntity;

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

    // 삼지창 주민 터렛 등록 (MISC 카테고리 설정)
    public static final RegistryObject<EntityType<TridentTurretEntity>> TRIDENT_TURRET =
            ENTITY_TYPES.register("trident_turret",
                    () -> EntityType.Builder.of(TridentTurretEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F) // 주민과 동일한 기본 히트박스 크기
                            .build("trident_turret"));

    // 좀비 보스 등록 (몬스터 카테고리로 설정)
    public static final RegistryObject<EntityType<ZombieBossEntity>> ZOMBIE_BOSS =
            ENTITY_TYPES.register("zombie_boss",
                    () -> EntityType.Builder.of(ZombieBossEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("zombie_boss"));

    // 🆕 자원 수확 주민 등록 (MISC 카테고리 설정)
    public static final RegistryObject<EntityType<ResourceVillagerEntity>> RESOURCE_VILLAGER =
            ENTITY_TYPES.register("resource_villager",
                    () -> EntityType.Builder.of(ResourceVillagerEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .build("resource_villager"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    public static final RegistryObject<EntityType<HealerTurretEntity>> HEALER_TURRET =
            ENTITY_TYPES.register("healer_turret",
                    () -> EntityType.Builder.of(HealerTurretEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F) // 주민 크기와 동일
                            .build("healer_turret"));
}
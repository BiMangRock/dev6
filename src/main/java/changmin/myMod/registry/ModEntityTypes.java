package changmin.myMod.registry;

import changmin.myMod.MyMod;
import changmin.myMod.feature.turret.healer.HealerTurretEntity;
import changmin.myMod.feature.turret.tanker.TankerTurretEntity;
import changmin.myMod.feature.turret.trident_turret.TridentTurretEntity;
import changmin.myMod.feature.turret.villager_turret.VillagerTurretEntity;
import changmin.myMod.feature.turret.resource_villager1.ResourceVillagerEntity;
import changmin.myMod.feature.turret.lightning_wizard.LightningWizardEntity; // 🆕 추가
import changmin.myMod.feature.turret.lightning_wizard.LightningProjectileEntity; // 🆕 추가
import changmin.myMod.feature.zombie.zombie1.ZombieBossEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import changmin.myMod.feature.turret.plasma_wizard.PlasmaWizardEntity;
import changmin.myMod.feature.turret.plasma_wizard.*;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, MyMod.MODID);

    public static final RegistryObject<EntityType<VillagerTurretEntity>> VILLAGER_TURRET =
            ENTITY_TYPES.register("villager_turret",
                    () -> EntityType.Builder.of(VillagerTurretEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .build("villager_turret"));

    public static final RegistryObject<EntityType<TridentTurretEntity>> TRIDENT_TURRET =
            ENTITY_TYPES.register("trident_turret",
                    () -> EntityType.Builder.of(TridentTurretEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .build("trident_turret"));

    public static final RegistryObject<EntityType<ZombieBossEntity>> ZOMBIE_BOSS =
            ENTITY_TYPES.register("zombie_boss",
                    () -> EntityType.Builder.of(ZombieBossEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("zombie_boss"));

    public static final RegistryObject<EntityType<ResourceVillagerEntity>> RESOURCE_VILLAGER =
            ENTITY_TYPES.register("resource_villager",
                    () -> EntityType.Builder.of(ResourceVillagerEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .build("resource_villager"));

    public static final RegistryObject<EntityType<TankerTurretEntity>> TANKER_TURRET =
            ENTITY_TYPES.register("tanker_turret",
                    () -> EntityType.Builder.of(TankerTurretEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .build("tanker_turret"));

    public static final RegistryObject<EntityType<HealerTurretEntity>> HEALER_TURRET =
            ENTITY_TYPES.register("healer_turret",
                    () -> EntityType.Builder.of(HealerTurretEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .build("healer_turret"));

    // 🆕 번개 마법사 주민 등록
    public static final RegistryObject<EntityType<LightningWizardEntity>> LIGHTNING_WIZARD =
            ENTITY_TYPES.register("lightning_wizard",
                    () -> EntityType.Builder.of(LightningWizardEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .build("lightning_wizard"));

    // 🆕 마법 구체 투사체 등록
    public static final RegistryObject<EntityType<LightningProjectileEntity>> LIGHTNING_PROJECTILE =
            ENTITY_TYPES.register("lightning_projectile",
                    () -> EntityType.Builder.<LightningProjectileEntity>of(LightningProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .build("lightning_projectile"));

    // 🆕 플라즈마 마법사 터렛 등록
    public static final RegistryObject<EntityType<changmin.myMod.feature.turret.plasma_wizard.PlasmaWizardEntity>> PLASMA_WIZARD =
            ENTITY_TYPES.register("plasma_wizard",
                    () -> EntityType.Builder.of(PlasmaWizardEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .build("plasma_wizard"));

    // 🆕 플라즈마 전자기 마법구 투사체 등록
    public static final RegistryObject<EntityType<changmin.myMod.feature.turret.plasma_wizard.PlasmaOrbEntity>> PLASMA_ORB =
            ENTITY_TYPES.register("plasma_orb",
                    () -> EntityType.Builder.<changmin.myMod.feature.turret.plasma_wizard.PlasmaOrbEntity>of(PlasmaOrbEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .build("plasma_orb"));



    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
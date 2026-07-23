package changmin.changmin_villager_turret.registry;

import changmin.changmin_villager_turret.changmin_villager_turret;
import changmin.changmin_villager_turret.feature.turret.bee_summoner_turret.BeeSummonerTurretEntity;
import changmin.changmin_villager_turret.feature.turret.bee_summoner_turret.SummonedBeeEntity;
import changmin.changmin_villager_turret.feature.turret.goddess_of_flame.GoddessFireballEntity;
import changmin.changmin_villager_turret.feature.turret.goddess_of_flame.GoddessOfFlameEntity;
import changmin.changmin_villager_turret.feature.turret.healer.HealerTurretEntity;
import changmin.changmin_villager_turret.feature.turret.tanker.TankerTurretEntity;
import changmin.changmin_villager_turret.feature.turret.trident_turret.TridentTurretEntity;
import changmin.changmin_villager_turret.feature.turret.villager_turret.VillagerTurretEntity;
import changmin.changmin_villager_turret.feature.turret.resource_villager1.ResourceVillagerEntity;
import changmin.changmin_villager_turret.feature.turret.lightning_wizard.LightningWizardEntity; // 🆕 추가
import changmin.changmin_villager_turret.feature.turret.lightning_wizard.LightningProjectileEntity; // 🆕 추가
import changmin.changmin_villager_turret.feature.zombie.Apostle_of_the_End.ApostleOfTheEndEntity;
import changmin.changmin_villager_turret.feature.zombie.angel_zombie.AngelZombieArrow;
import changmin.changmin_villager_turret.feature.zombie.angel_zombie.AngelZombieEntity;
import changmin.changmin_villager_turret.feature.zombie.angel_zombie.ShockwaveEntity;
import changmin.changmin_villager_turret.feature.zombie.assassin2.Assassin2Entity;
import changmin.changmin_villager_turret.feature.zombie.assassin2.SwordGhoulEntity;
import changmin.changmin_villager_turret.feature.zombie.creaking.CreakingEntity;
import changmin.changmin_villager_turret.feature.zombie.demon1.Demon1BatEntity;
import changmin.changmin_villager_turret.feature.zombie.demon1.Demon1Entity;
import changmin.changmin_villager_turret.feature.zombie.healer_zombie.HealerZombieEntity;
import changmin.changmin_villager_turret.feature.zombie.raged_angel_zombie.RagedAngelZombieEntity;
import changmin.changmin_villager_turret.feature.zombie.raged_angel_zombie.RagedShockwaveEntity;
import changmin.changmin_villager_turret.feature.zombie.zombie1.ZombieBossEntity;

import changmin.changmin_villager_turret.feature.zombie.zombie_commander.ZombieCommanderEntity;
import changmin.changmin_villager_turret.feature.zombie.zombie_commander.ZombieCommanderMaggotEntity;
import changmin.changmin_villager_turret.feature.zombie.zombie_commander.ZombieCommanderProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import changmin.changmin_villager_turret.feature.turret.plasma_wizard.PlasmaWizardEntity;
import changmin.changmin_villager_turret.feature.turret.plasma_wizard.*;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, changmin_villager_turret.MODID);

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
    public static final RegistryObject<EntityType<changmin.changmin_villager_turret.feature.turret.plasma_wizard.PlasmaWizardEntity>> PLASMA_WIZARD =
            ENTITY_TYPES.register("plasma_wizard",
                    () -> EntityType.Builder.of(PlasmaWizardEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .build("plasma_wizard"));

    // 🆕 플라즈마 전자기 마법구 투사체 등록
    public static final RegistryObject<EntityType<changmin.changmin_villager_turret.feature.turret.plasma_wizard.PlasmaOrbEntity>> PLASMA_ORB =
            ENTITY_TYPES.register("plasma_orb",
                    () -> EntityType.Builder.<changmin.changmin_villager_turret.feature.turret.plasma_wizard.PlasmaOrbEntity>of(PlasmaOrbEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .build("plasma_orb"));

    // 🆕 벌 소환사 주민 터렛 등록
    public static final RegistryObject<EntityType<BeeSummonerTurretEntity>> BEE_SUMMONER_TURRET =
            ENTITY_TYPES.register("bee_summoner_turret",
                    () -> EntityType.Builder.of(BeeSummonerTurretEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .build("bee_summoner_turret"));

    // 🆕 소환된 공중 벌 등록
    public static final RegistryObject<EntityType<SummonedBeeEntity>> SUMMONED_BEE =
            ENTITY_TYPES.register("summoned_bee",
                    () -> EntityType.Builder.of(SummonedBeeEntity::new, MobCategory.MISC)
                            .sized(0.7F, 0.7F)
                            .build("summoned_bee"));

    public static final RegistryObject<EntityType<AngelZombieEntity>> ANGEL_ZOMBIE =
            ENTITY_TYPES.register("angel_zombie",
                    () -> EntityType.Builder.of(AngelZombieEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("angel_zombie"));

    public static final RegistryObject<EntityType<AngelZombieArrow>> ANGEL_ZOMBIE_ARROW =
            ENTITY_TYPES.register("angel_zombie_arrow",
                    () -> EntityType.Builder.<AngelZombieArrow>of(AngelZombieArrow::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .build("angel_zombie_arrow"));

    // 🆕 도넛 충격파 엔티티 등록
    public static final RegistryObject<EntityType<ShockwaveEntity>> SHOCKWAVE =
            ENTITY_TYPES.register("shockwave",
                    () -> EntityType.Builder.<ShockwaveEntity>of(ShockwaveEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .build("shockwave"));

    public static final RegistryObject<EntityType<ApostleOfTheEndEntity>> APOSTLE_OF_THE_END =
            ENTITY_TYPES.register("apostle_of_the_end",
                    () -> EntityType.Builder.of(ApostleOfTheEndEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("apostle_of_the_end"));

    public static final RegistryObject<EntityType<RagedAngelZombieEntity>> RAGED_ANGEL_ZOMBIE =
            ENTITY_TYPES.register("raged_angel_zombie",
                    () -> EntityType.Builder.of(RagedAngelZombieEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F) // 히트박스 크기
                            .build("raged_angel_zombie"));

    public static final RegistryObject<EntityType<RagedShockwaveEntity>> RAGED_SHOCKWAVE =
            ENTITY_TYPES.register("raged_shockwave",
                    () -> EntityType.Builder.<RagedShockwaveEntity>of(RagedShockwaveEntity::new, MobCategory.MISC)
                            .sized(1.0F, 1.0F).build("raged_shockwave"));

    // ModEntityTypes.java에 추가
    public static final RegistryObject<EntityType<SwordGhoulEntity>> SWORD_GHOUL =
            ENTITY_TYPES.register("sword_ghoul",
                    () -> EntityType.Builder.<SwordGhoulEntity>of(SwordGhoulEntity::new, MobCategory.MISC)
                            .sized(2.0F, 0.5F) // 검귀 크기에 맞게 설정
                            .build("sword_ghoul"));

    public static final RegistryObject<EntityType<CreakingEntity>> CREAKING =
            ENTITY_TYPES.register("creaking",
                    () -> EntityType.Builder.of(CreakingEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 2.3F) // 크리킹의 키가 크므로 대략 2.3F 설정
                            .build("creaking"));

    // 악마 몹 등록
    public static final RegistryObject<EntityType<Demon1Entity>> DEMON1 =
            ENTITY_TYPES.register("demon1",
                    () -> EntityType.Builder.of(Demon1Entity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("demon1"));

    // 악마의 박쥐 투사체 등록
    public static final RegistryObject<EntityType<Demon1BatEntity>> DEMON1_BAT =
            ENTITY_TYPES.register("demon1_bat",
                    () -> EntityType.Builder.<Demon1BatEntity>of(Demon1BatEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .build("demon1_bat"));

    // 화염의 여신 엔티티 등록
    public static final RegistryObject<EntityType<GoddessOfFlameEntity>> GODDESS_OF_FLAME =
            ENTITY_TYPES.register("goddess_of_flame",
                    () -> EntityType.Builder.of(GoddessOfFlameEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.95F)
                            .build("goddess_of_flame"));

    // 화염 여신의 불덩이 투사체 등록
    public static final RegistryObject<EntityType<GoddessFireballEntity>> GODDESS_FIREBALL =
            ENTITY_TYPES.register("goddess_fireball",
                    () -> EntityType.Builder.<GoddessFireballEntity>of(GoddessFireballEntity::new, MobCategory.MISC)
                            .sized(0.3125F, 0.3125F)
                            .build("goddess_fireball"));

    public static final RegistryObject<EntityType<ZombieCommanderEntity>> ZOMBIE_COMMANDER =
            ENTITY_TYPES.register("zombie_commander",
                    () -> EntityType.Builder.of(ZombieCommanderEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("zombie_commander"));

    public static final RegistryObject<EntityType<ZombieCommanderMaggotEntity>> COMMANDER_MAGGOT =
            ENTITY_TYPES.register("commander_maggot",
                    () -> EntityType.Builder.of(ZombieCommanderMaggotEntity::new, MobCategory.MONSTER)
                            .sized(0.4F, 0.3F)
                            .build("commander_maggot"));

    public static final RegistryObject<EntityType<ZombieCommanderProjectile>> COMMANDER_PROJECTILE =
            ENTITY_TYPES.register("commander_projectile",
                    () -> EntityType.Builder.<ZombieCommanderProjectile>of(ZombieCommanderProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .build("commander_projectile"));

    public static final RegistryObject<EntityType<HealerZombieEntity>> HEALER_ZOMBIE =
            ENTITY_TYPES.register("healer_zombie",
                    () -> EntityType.Builder.of(HealerZombieEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("healer_zombie"));


    public static final RegistryObject<EntityType<Assassin2Entity>> ASSASSIN2 = ENTITY_TYPES.register("assassin2", () ->
            EntityType.Builder.of(Assassin2Entity::new, MobCategory.MONSTER).sized(0.6F, 1.95F).build("assassin2"));



    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
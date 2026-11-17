package changmin.changmin_villager_turret.guide;

import changmin.changmin_villager_turret.changmin_villager_turret;
import changmin.changmin_villager_turret.feature.zombie.Apostle_of_the_End.ApostleOfTheEndEntity;
import changmin.changmin_villager_turret.feature.zombie.zombie1.ZombieBossEntity;
import changmin.changmin_villager_turret.registry.ModEntityTypes;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * 🏆 튜토리얼 발전과제 지급
 *
 * 발전과제 JSON(data/changmin_villager_turret/advancements/guide/*.json)은 모두 "minecraft:impossible" 조건이라
 * 저절로 달성되지 않고, 여기 코드에서 상황을 감지해 직접 지급합니다.
 *
 *   root          : 처음 접속 (GuideBookHandler에서 지급)
 *   first_turret  : 터렛 스폰알을 처음 사용
 *   first_upgrade : 터렛 업그레이드를 처음 구매 (AbstractTurretEntity.notifyTrade)
 *   veteran_turret: 근처 터렛이 Lv.10 달성 (AbstractTurretEntity.celebrateLevelUp)
 *   first_boss    : 근처에서 보스(좀비 보스·종말의 사도) 처치
 *   all_turrets   : 전투 터렛 7종을 모두 설치
 */
@Mod.EventBusSubscriber(modid = changmin_villager_turret.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TurretAdvancements {

    public static final String ROOT = "root";
    public static final String FIRST_TURRET = "first_turret";
    public static final String FIRST_UPGRADE = "first_upgrade";
    public static final String VETERAN = "veteran_turret";
    public static final String FIRST_BOSS = "first_boss";
    public static final String ALL_TURRETS = "all_turrets";

    public static final int VETERAN_LEVEL = 10;
    private static final double BOSS_CREDIT_RANGE = 48.0D;
    private static final String PLACED_KEY = "changmin_turret_placed_types";

    private TurretAdvancements() {}

    /** 발전과제를 지급합니다. 이미 달성했거나 JSON이 없으면 아무 일도 하지 않습니다. */
    public static void award(ServerPlayer player, String id) {
        Advancement advancement = player.server.getAdvancements()
                .getAdvancement(new ResourceLocation(changmin_villager_turret.MODID, "guide/" + id));
        if (advancement == null) return;

        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) return;

        List<String> remaining = new ArrayList<>();
        progress.getRemainingCriteria().forEach(remaining::add);
        for (String criterion : remaining) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    /** 전투 터렛 7종 (all_turrets 발전과제 대상) */
    private static List<EntityType<?>> combatTurretTypes() {
        return List.of(
                ModEntityTypes.VILLAGER_TURRET.get(),
                ModEntityTypes.TRIDENT_TURRET.get(),
                ModEntityTypes.TANKER_TURRET.get(),
                ModEntityTypes.HEALER_TURRET.get(),
                ModEntityTypes.LIGHTNING_WIZARD.get(),
                ModEntityTypes.PLASMA_WIZARD.get(),
                ModEntityTypes.BEE_SUMMONER_TURRET.get());
    }

    private static boolean isAnyTurret(EntityType<?> type) {
        return combatTurretTypes().contains(type)
                || type == ModEntityTypes.RESOURCE_VILLAGER.get()
                || type == ModEntityTypes.RESOURCE_VILLAGER_EASY.get();
    }

    // ── 터렛 스폰알 사용 감지 ──
    @SubscribeEvent
    public static void onUseSpawnEgg(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isClientSide || !(event.getPlayer() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof SpawnEggItem egg)) return;

        EntityType<?> type = egg.getType(stack.getTag());
        if (!isAnyTurret(type)) return;

        award(player, FIRST_TURRET);

        // 설치한 전투 터렛 종류를 플레이어 영구 데이터에 기록 → 7종 모두 모이면 발전과제
        List<EntityType<?>> combat = combatTurretTypes();
        if (!combat.contains(type)) return;

        CompoundTag persisted = getPersisted(player);
        ListTag placed = persisted.getList(PLACED_KEY, Tag.TAG_STRING);
        String id = String.valueOf(type.getRegistryName());
        boolean already = false;
        for (int i = 0; i < placed.size(); i++) {
            if (placed.getString(i).equals(id)) { already = true; break; }
        }
        if (!already) {
            placed.add(StringTag.valueOf(id));
            persisted.put(PLACED_KEY, placed);
            savePersisted(player, persisted);
        }
        if (placed.size() >= combat.size()) {
            award(player, ALL_TURRETS);
        }
    }

    // ── 보스 처치 감지 (터렛이 잡아도 근처 플레이어 모두 인정) ──
    @SubscribeEvent
    public static void onBossDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntityLiving();
        if (!(dead instanceof ZombieBossEntity) && !(dead instanceof ApostleOfTheEndEntity)) return;
        if (!(dead.level instanceof ServerLevel serverLevel)) return;

        double rangeSqr = BOSS_CREDIT_RANGE * BOSS_CREDIT_RANGE;
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(dead) <= rangeSqr) {
                award(player, FIRST_BOSS);
            }
        }
    }

    // ── 플레이어 영구 데이터 (사망해도 유지되는 PlayerPersisted 영역) ──
    static CompoundTag getPersisted(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }

    static void savePersisted(Player player, CompoundTag persisted) {
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
}

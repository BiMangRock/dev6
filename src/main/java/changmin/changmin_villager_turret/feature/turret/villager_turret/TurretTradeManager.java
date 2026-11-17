package changmin.changmin_villager_turret.feature.turret.villager_turret;

import changmin.changmin_villager_turret.config.TurretConfig;
import changmin.changmin_villager_turret.feature.turret.common.TurretTradeUtil;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import changmin.changmin_villager_turret.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class TurretTradeManager {

    // ==========================================
    // 📋 업그레이드 정의 표
    // 새 업그레이드는 여기 한 줄(register)만 추가하면 됩니다.
    //   id          : 인증서의 UpgradeType 값
    //   canApply    : 적용 가능 조건 (최대 레벨 제한 등). false면 적용하지 않음
    //   apply       : 실제 효과 적용 후, 플레이어에게 보여줄 메시지를 반환
    //   maxedMessage: null이 아니면, 적용 불가 시 이 메시지만 띄우고 효과음·화면 갱신 없이 종료
    // ==========================================
    private record Upgrade(Predicate<VillagerTurretEntity> canApply,
                           Function<VillagerTurretEntity, Component> apply,
                           @Nullable Component maxedMessage) {}

    private static final Map<String, Upgrade> UPGRADES = new HashMap<>();

    private static void register(String id, Predicate<VillagerTurretEntity> canApply, Function<VillagerTurretEntity, Component> apply) {
        UPGRADES.put(id, new Upgrade(canApply, apply, null));
    }

    private static void register(String id, Function<VillagerTurretEntity, Component> apply) {
        register(id, t -> true, apply);
    }

    /** 사격 패턴 변경 (0 일반, 1 부채꼴, 2 점사, 3 넉백, 4 강공격) */
    private static void registerPattern(String id, int pattern, Component message) {
        register(id, t -> { t.setArrowPattern(pattern); return message; });
    }

    static {
        // ── 수치형 강화 ──
        register("recharge", t -> t.getRechargeLevel() < TurretConfig.maxRechargeLevel(), t -> {
            t.setRechargeLevel(t.getRechargeLevel() + 1);
            return TurretTradeUtil.tr("changmin_villager_turret.text.villager.1", t.getRechargeLevel());
        });
        register("speed", t -> t.getSpeedLevel() < TurretConfig.maxSpeedLevel(), t -> {
            t.setSpeedLevel(t.getSpeedLevel() + 1);
            return TurretTradeUtil.tr("changmin_villager_turret.text.villager.2", t.getSpeedLevel());
        });
        register("invuln", t -> {
            t.setInvulnerabilityLevel(t.getInvulnerabilityLevel() + 1);
            return TurretTradeUtil.tr("changmin_villager_turret.text.villager.3", t.getInvulnerabilityLevel());
        });
        register("heal", t -> {
            t.setHealLevel(t.getHealLevel() + 1);
            return TurretTradeUtil.tr("changmin_villager_turret.text.villager.4", (t.getHealLevel() * 20));
        });
        register("damage", t -> {
            t.setDamageLevel(t.getDamageLevel() + 1);
            return TurretTradeUtil.tr("changmin_villager_turret.text.villager.5", t.getDamageLevel());
        });
        register("health", t -> {
            t.setMaxHealthBonus(t.getMaxHealthBonus() + 5);
            t.heal(5.0F);
            return TurretTradeUtil.tr("changmin_villager_turret.text.villager.6", (int) t.getMaxHealth());
        });
        register("automation", t -> {
            t.setAutomationLevel(t.getAutomationLevel() + 1);
            return TurretTradeUtil.tr("changmin_villager_turret.text.villager.7", t.getAutomationLevel());
        });

        // ── 상태이상 화살 ──
        register("poison", t -> t.getPoisonLevel() < 255, t -> {
            t.setPoisonLevel(t.getPoisonLevel() + 1);
            return TurretTradeUtil.tr("changmin_villager_turret.text.villager.8", t.getPoisonLevel());
        });
        register("wither", t -> t.getWitherLevel() < 255, t -> {
            t.setWitherLevel(t.getWitherLevel() + 1);
            return TurretTradeUtil.tr("changmin_villager_turret.text.villager.9", t.getWitherLevel());
        });
        register("slowness", t -> t.getSlownessLevel() < 5, t -> {
            t.setSlownessLevel(t.getSlownessLevel() + 1);
            return TurretTradeUtil.tr("changmin_villager_turret.text.villager.10", t.getSlownessLevel());
        });
        register("weakness", t -> t.getWeaknessLevel() < 5, t -> {
            t.setWeaknessLevel(t.getWeaknessLevel() + 1);
            return TurretTradeUtil.tr("changmin_villager_turret.text.villager.11", t.getWeaknessLevel());
        });

        // ── 경험치 할인 ──
        register("xp_discount", t -> {
            int previousNeeded = t.getNeededXp();
            int discounted = (previousNeeded + 1) / 2;
            t.setNeededXp(discounted);
            t.addXp(0);
            return TurretTradeUtil.tr("changmin_villager_turret.text.villager.12", previousNeeded, discounted);
        });

        // ── 화살 개수 (최대 10, 최대치면 안내 후 종료) ──
        UPGRADES.put("arrow_count", new Upgrade(
                t -> t.getTurretArrowCount() < 10,
                t -> {
                    t.setTurretArrowCount(t.getTurretArrowCount() + 1);
                    return TurretTradeUtil.tr("changmin_villager_turret.text.villager.13", t.getTurretArrowCount());
                },
                TurretTradeUtil.tr("changmin_villager_turret.text.villager.14")));

        // ── 사격 패턴 ──
        registerPattern("normal_pattern", 0, TurretTradeUtil.tr("changmin_villager_turret.text.villager.15"));
        registerPattern("fan", 1, TurretTradeUtil.tr("changmin_villager_turret.text.villager.16"));
        registerPattern("burst", 2, TurretTradeUtil.tr("changmin_villager_turret.text.villager.17"));
        registerPattern("knockback_mode", 3, TurretTradeUtil.tr("changmin_villager_turret.text.villager.18"));
        registerPattern("heavy_mode", 4, TurretTradeUtil.tr("changmin_villager_turret.text.villager.19"));

        // ── 토글형 기술 ──
        register("normal_gravity", t -> { t.setNoGravityEnabled(0); return TurretTradeUtil.tr("changmin_villager_turret.text.villager.20"); });
        register("vacuum", t -> { t.setNoGravityEnabled(1); return TurretTradeUtil.tr("changmin_villager_turret.text.villager.21"); });
        register("normal_blocks", t -> { t.setPassBlocksEnabled(0); return TurretTradeUtil.tr("changmin_villager_turret.text.villager.22"); });
        register("pass_blocks", t -> { t.setPassBlocksEnabled(1); return TurretTradeUtil.tr("changmin_villager_turret.text.villager.23"); });
        register("normal_sight", t -> { t.setCanSeeThroughWalls(0); return TurretTradeUtil.tr("changmin_villager_turret.text.villager.24"); });
        register("can_see_through_walls", t -> { t.setCanSeeThroughWalls(1); return TurretTradeUtil.tr("changmin_villager_turret.text.villager.25"); });
    }

    public static void populateOffers(VillagerTurretEntity turret, MerchantOffers offers) {
        offers.clear();

        MerchantOffers tokenOffers = new MerchantOffers();
        MerchantOffers materialOffers = new MerchantOffers(); // 철, 금 등 재화용

        // ==========================================
        // 🟢 9진법 및 27진법 기반 동적 가격 계산 및 만렙 달성 시 목록 말소 처리 적용
        // ==========================================
        addDynamicOffer(turret, tokenOffers, "recharge", TurretTradeUtil.tr("changmin_villager_turret.text.villager.26"), turret.getRechargeLevel(), TurretTradeUtil.tr("changmin_villager_turret.text.villager.27"), TurretConfig.maxRechargeLevel()); // 만렙 (config, 기본 10)
        addDynamicOffer(turret, tokenOffers, "speed", TurretTradeUtil.tr("changmin_villager_turret.text.villager.28"), turret.getSpeedLevel(), TurretTradeUtil.tr("changmin_villager_turret.text.villager.29"), TurretConfig.maxSpeedLevel()); // 탄속 만렙 (config, 기본 15)
        addDynamicOffer(turret, tokenOffers, "invuln", TurretTradeUtil.tr("changmin_villager_turret.text.villager.30"), turret.getInvulnerabilityLevel(), TurretTradeUtil.tr("changmin_villager_turret.text.villager.31"), Integer.MAX_VALUE);
        addDynamicOffer(turret, tokenOffers, "heal", TurretTradeUtil.tr("changmin_villager_turret.text.villager.32"), turret.getHealLevel(), TurretTradeUtil.tr("changmin_villager_turret.text.villager.33"), Integer.MAX_VALUE);
        addDynamicOffer(turret, tokenOffers, "damage", TurretTradeUtil.tr("changmin_villager_turret.text.villager.34"), turret.getDamageLevel(), TurretTradeUtil.tr("changmin_villager_turret.text.villager.35"), Integer.MAX_VALUE);
        addDynamicOffer(turret, tokenOffers, "health", TurretTradeUtil.tr("changmin_villager_turret.text.villager.36"), turret.getMaxHealthBonus() / 5, TurretTradeUtil.tr("changmin_villager_turret.text.villager.37"), Integer.MAX_VALUE); // 스탯 레벨 환산
        addDynamicOffer(turret, tokenOffers, "automation", TurretTradeUtil.tr("changmin_villager_turret.text.villager.38"), turret.getAutomationLevel(), TurretTradeUtil.tr("changmin_villager_turret.text.villager.39"), Integer.MAX_VALUE);
        addDynamicOffer(turret, tokenOffers, "poison", TurretTradeUtil.tr("changmin_villager_turret.text.villager.40"), turret.getPoisonLevel(), TurretTradeUtil.tr("changmin_villager_turret.text.villager.41"), 255); // 만렙 255
        addDynamicOffer(turret, tokenOffers, "wither", TurretTradeUtil.tr("changmin_villager_turret.text.villager.42"), turret.getWitherLevel(), TurretTradeUtil.tr("changmin_villager_turret.text.villager.43"), 255); // 만렙 255
        addDynamicOffer(turret, tokenOffers, "slowness", TurretTradeUtil.tr("changmin_villager_turret.text.villager.44"), turret.getSlownessLevel(), TurretTradeUtil.tr("changmin_villager_turret.text.villager.45"), 5); // 만렙 5
        addDynamicOffer(turret, tokenOffers, "weakness", TurretTradeUtil.tr("changmin_villager_turret.text.villager.46"), turret.getWeaknessLevel(), TurretTradeUtil.tr("changmin_villager_turret.text.villager.47"), 5); // 만렙 5


        // ==========================================
        // ⚙️ 경험치 할인 (고정가)
        // ==========================================
        int currentNeededXp = turret.getNeededXp();
        int discountedXp = (currentNeededXp + 1) / 2;
        ItemStack xpDiscountReceipt = createUpgradeReceipt(turret, "xp_discount", TurretTradeUtil.tr("changmin_villager_turret.text.villager.48"),
                TurretTradeUtil.tr("changmin_villager_turret.text.villager.49"),
                TurretTradeUtil.tr("changmin_villager_turret.text.villager.50"),
                TurretTradeUtil.tr("changmin_villager_turret.text.villager.51", currentNeededXp, discountedXp));
        materialOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), xpDiscountReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_LOW.get(), 1), xpDiscountReceipt, 15, 2, 0.05F));


        // ==========================================
        // ⚙️ 2단계 개조 항목 (중급 토큰 및 금 주괴 사용)
        // ==========================================
        int currentArrowCount = turret.getTurretArrowCount();
        ItemStack arrowCountReceipt;
        MerchantOffer arrowCountEmeraldOffer;
        MerchantOffer arrowCountTokenOffer;

        if (currentArrowCount < 10) {
            arrowCountReceipt = createUpgradeReceipt(turret, "arrow_count", TurretTradeUtil.tr("changmin_villager_turret.text.villager.52"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.49"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.53"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.54", currentArrowCount, (currentArrowCount + 1)));

            arrowCountEmeraldOffer = new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), arrowCountReceipt, 15, 2, 0.05F);
            arrowCountTokenOffer = new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 2), arrowCountReceipt, 15, 2, 0.05F);
        } else {
            arrowCountReceipt = createUpgradeReceipt(turret, "arrow_count", TurretTradeUtil.tr("changmin_villager_turret.text.villager.55"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.56"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.57"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.58"));

            arrowCountEmeraldOffer = new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), arrowCountReceipt, 15, 2, 0.05F);
            arrowCountEmeraldOffer.setToOutOfStock();

            arrowCountTokenOffer = new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 8), arrowCountReceipt, 15, 2, 0.05F);
            arrowCountTokenOffer.setToOutOfStock();
        }
        materialOffers.add(arrowCountEmeraldOffer);
        tokenOffers.add(arrowCountTokenOffer);

        // 발사 형태 5개 모드로 확장 개조
        if (turret.getArrowPattern() != 0) {
            ItemStack normalPatternReceipt = createUpgradeReceipt(turret, "normal_pattern", TurretTradeUtil.tr("changmin_villager_turret.text.villager.59"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.49"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.60"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.61"));
            materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), normalPatternReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 1), normalPatternReceipt, 15, 2, 0.05F));
        }
        if (turret.getArrowPattern() != 1) {
            ItemStack fanReceipt = createUpgradeReceipt(turret, "fan", TurretTradeUtil.tr("changmin_villager_turret.text.villager.62"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.49"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.63"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.64"));
            materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), fanReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 2), fanReceipt, 5, 5, 0.05F));
        }
        if (turret.getArrowPattern() != 2) {
            ItemStack burstReceipt = createUpgradeReceipt(turret, "burst", TurretTradeUtil.tr("changmin_villager_turret.text.villager.65"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.49"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.66"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.67"));
            materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), burstReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 2), burstReceipt, 5, 5, 0.05F));
        }
        if (turret.getArrowPattern() != 3) {
            ItemStack knockbackReceipt = createUpgradeReceipt(turret, "knockback_mode", TurretTradeUtil.tr("changmin_villager_turret.text.villager.68"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.49"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.69"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.70"));
            materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), knockbackReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 3), knockbackReceipt, 5, 5, 0.05F));
        }
        if (turret.getArrowPattern() != 4) {
            ItemStack heavyReceipt = createUpgradeReceipt(turret, "heavy_mode", TurretTradeUtil.tr("changmin_villager_turret.text.villager.71"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.49"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.72"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.73"));
            materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), heavyReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 4), heavyReceipt, 5, 5, 0.05F));
        }

        // [진공 제어 토글 변경]
        if (turret.getNoGravityEnabled() == 1) {
            ItemStack normalGravityReceipt = createUpgradeReceipt(turret, "normal_gravity", TurretTradeUtil.tr("changmin_villager_turret.text.villager.74"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.49"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.75"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.76"));
            materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), normalGravityReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 1), normalGravityReceipt, 15, 2, 0.05F));
        } else {
            ItemStack vacuumReceipt = createUpgradeReceipt(turret, "vacuum", TurretTradeUtil.tr("changmin_villager_turret.text.villager.77"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.49"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.78"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.79"));
            materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), vacuumReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 2), vacuumReceipt, 5, 5, 0.05F));
        }


        // ==========================================
        // 🔮 3단계 개조 항목 (상급 토큰 및 에메랄드 사용)
        // ==========================================

        // [구조물 관통 제어 토글 변경]
        if (turret.getPassBlocksEnabled() == 1) {
            ItemStack normalBlocksReceipt = createUpgradeReceipt(turret, "normal_blocks", TurretTradeUtil.tr("changmin_villager_turret.text.villager.80"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.49"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.81"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.82"));
            materialOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), normalBlocksReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_HIGH.get(), 1), normalBlocksReceipt, 15, 2, 0.05F));
        } else {
            ItemStack passBlocksReceipt = createUpgradeReceipt(turret, "pass_blocks", TurretTradeUtil.tr("changmin_villager_turret.text.villager.83"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.49"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.84"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.85"));
            materialOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), passBlocksReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_HIGH.get(), 5), passBlocksReceipt, 5, 5, 0.05F));
        }

        // [벽 너머 투시 감지 토글 변경]
        if (turret.getCanSeeThroughWalls() == 1) {
            ItemStack normalSightReceipt = createUpgradeReceipt(turret, "normal_sight", TurretTradeUtil.tr("changmin_villager_turret.text.villager.86"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.49"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.87"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.88"));
            materialOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), normalSightReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_HIGH.get(), 1), normalSightReceipt, 15, 2, 0.05F));
        } else {
            ItemStack canSeeThroughWallsReceipt = createUpgradeReceipt(turret, "can_see_through_walls", TurretTradeUtil.tr("changmin_villager_turret.text.villager.89"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.49"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.90"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.villager.91"));
            materialOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), canSeeThroughWallsReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_HIGH.get(), 3), canSeeThroughWallsReceipt, 5, 5, 0.05F));
        }

        offers.addAll(tokenOffers);
        offers.addAll(materialOffers);
    }

    // 9진법 및 27진법 가격 자동 계산 및 만렙 달성 시 상인 거래 목록에서 완벽 영구 제외 기법 적용
    private static void addDynamicOffer(VillagerTurretEntity turret, MerchantOffers offers, String type, Component title, int currentLevel, Component descEffect, int maxLimit) {
        // 🌟 지정한 최대 한계 레벨 도달 시 거래 목록에서 아예 출력되지 않도록 영구 배제
        if (currentLevel >= maxLimit) {
            return;
        }

        int totalLow = 1 + currentLevel;
        ItemStack receipt = createUpgradeReceipt(turret, type, title,
                TurretTradeUtil.tr("changmin_villager_turret.text.villager.49"),
                TurretTradeUtil.tr("changmin_villager_turret.text.villager.92", descEffect),
                TurretTradeUtil.tr("changmin_villager_turret.text.villager.93", currentLevel, (currentLevel + 1)));

        if (totalLow < 27) {
            int midNeeded = totalLow / 9;
            int lowNeeded = totalLow % 9;

            if (midNeeded > 0 && lowNeeded > 0) {
                // 중급 토큰 + 하급 토큰 2슬롯 혼합 요구
                offers.add(new MerchantOffer(
                        new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), midNeeded),
                        new ItemStack(ModItems.TURRET_POINT_TOKEN_LOW.get(), lowNeeded),
                        receipt, 15, 2, 0.05F));
            } else if (midNeeded > 0) {
                // 중급 단독 요구 (9 배수 도달 시)
                offers.add(new MerchantOffer(
                        new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), midNeeded),
                        receipt, 15, 2, 0.05F));
            } else {
                // 하급 단독 요구 (초기 구간)
                offers.add(new MerchantOffer(
                        new ItemStack(ModItems.TURRET_POINT_TOKEN_LOW.get(), lowNeeded),
                        receipt, 15, 2, 0.05F));
            }
        } else {
            // 자릿수 올림 시프트 작동 (상급 + 중급 요구)
            int highNeeded = totalLow / 27;
            int midNeeded = (totalLow % 27) / 9;

            if (highNeeded > 0 && midNeeded > 0) {
                offers.add(new MerchantOffer(
                        new ItemStack(ModItems.TURRET_POINT_TOKEN_HIGH.get(), highNeeded),
                        new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), midNeeded),
                        receipt, 15, 2, 0.05F));
            } else {
                offers.add(new MerchantOffer(
                        new ItemStack(ModItems.TURRET_POINT_TOKEN_HIGH.get(), highNeeded),
                        receipt, 15, 2, 0.05F));
            }
        }
    }

    public static ItemStack getBoundToken(VillagerTurretEntity turret, int count) {
        return TurretTradeUtil.boundToken(ModItems.TURRET_POINT_TOKEN_LOW.get(), turret, count, TurretTradeUtil.tr("changmin_villager_turret.text.villager.94"), 0xFFD700);
    }

    public static ItemStack createUpgradeReceipt(VillagerTurretEntity turret, String type, Component title, Component desc1, Component desc2, Component currentLvlInfo) {
        Item baseItem = switch (type) {
            case "speed" -> ModItems.SPEED_UPGRADE.get();
            case "recharge" -> ModItems.RECHARGE_UPGRADE.get();
            case "burst" -> ModItems.BURST_UPGRADE.get();
            case "fan" -> ModItems.FAN_UPGRADE.get();
            case "poison" -> ModItems.POISON_ARROW_UPGRADE.get();
            default -> Items.PAPER;
        };
        return TurretTradeUtil.upgradeReceipt(baseItem, turret, type, title, 0xFF55FF,
                TurretTradeUtil.line(desc1, TurretTradeUtil.GREEN),
                TurretTradeUtil.line(desc2, TurretTradeUtil.YELLOW),
                TurretTradeUtil.line(currentLvlInfo, TurretTradeUtil.CYAN));
    }

    public static void applyUpgradeDirectly(VillagerTurretEntity turret, String type) {
        Player tradingPlayer = turret.getTradingPlayer();
        Upgrade upgrade = UPGRADES.get(type);

        if (upgrade != null) {
            if (upgrade.canApply().test(turret)) {
                Component message = upgrade.apply().apply(turret);
                if (tradingPlayer != null && message != null) {
                    tradingPlayer.displayClientMessage(message, true);
                }
            } else if (upgrade.maxedMessage() != null) {
                // 최대치 도달 시 안내만 하고 종료 (효과음·화면 갱신 없음)
                if (tradingPlayer != null) {
                    tradingPlayer.displayClientMessage(upgrade.maxedMessage(), true);
                }
                return;
            }
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 1.0F);
        turret.refreshTradeScreen();
    }
}
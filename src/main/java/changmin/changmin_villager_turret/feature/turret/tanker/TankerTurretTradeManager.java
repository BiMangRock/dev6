package changmin.changmin_villager_turret.feature.turret.tanker;

import changmin.changmin_villager_turret.feature.turret.common.TurretTradeUtil;

import changmin.changmin_villager_turret.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class TankerTurretTradeManager {

    public static void populateOffers(TankerTurretEntity turret, MerchantOffers offers) {
        offers.clear();

        MerchantOffers tokenOffers = new MerchantOffers();
        MerchantOffers emeraldOffers = new MerchantOffers();

        // 🛡️ 1. 도발 지속 시간 강화 (탱커 전용 하급 토큰 소요)
        int durationLvl = turret.getTauntDurationLevel();
        ItemStack durationReceipt = createUpgradeReceipt(turret, "duration", TurretTradeUtil.tr("changmin_villager_turret.text.tanker.1"),
                TurretTradeUtil.tr("changmin_villager_turret.text.tanker.2"),
                TurretTradeUtil.tr("changmin_villager_turret.text.bee.3", (4 + durationLvl), (5 + durationLvl)));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), durationReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TANKER_POINT_TOKEN_LOW.get(), 1), durationReceipt, 15, 2, 0.05F));

        // 🛡️ 2. 도발 재장전 쿨타임 단축 (탱커 전용 하급 토큰 소요)
        int rechargeLvl = turret.getRechargeLevel();
        ItemStack rechargeReceipt = createUpgradeReceipt(turret, "recharge", TurretTradeUtil.tr("changmin_villager_turret.text.tanker.3"),
                TurretTradeUtil.tr("changmin_villager_turret.text.tanker.4"),
                TurretTradeUtil.tr("changmin_villager_turret.text.tanker.5", (10.0F - rechargeLvl * 0.5F), (9.5F - rechargeLvl * 0.5F)));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 12), rechargeReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TANKER_POINT_TOKEN_LOW.get(), 1), rechargeReceipt, 15, 2, 0.05F));

        // 🛡️ 3. 가시 반사 데미지 연마 (탱커 전용 중급 토큰 소요)
        int thornsLvl = turret.getThornsLevel();
        ItemStack thornsReceipt = createUpgradeReceipt(turret, "thorns", TurretTradeUtil.tr("changmin_villager_turret.text.tanker.6"),
                TurretTradeUtil.tr("changmin_villager_turret.text.tanker.7"),
                TurretTradeUtil.tr("changmin_villager_turret.text.tanker.8", (2 + thornsLvl), (3 + thornsLvl)));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 24), thornsReceipt, 10, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TANKER_POINT_TOKEN_MID.get(), 1), thornsReceipt, 10, 2, 0.05F));

        // 🛡️ 4. 도발 중 피해 감쇄 업그레이드 (최대 12단계 / 70% 제한 적용, 탱커 전용 상급 토큰 소요)
        int reductionLvl = turret.getReductionLevel();
        if (reductionLvl < 12) {
            ItemStack reductionReceipt = createUpgradeReceipt(turret, "reduction", TurretTradeUtil.tr("changmin_villager_turret.text.tanker.9"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.tanker.10"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.tanker.11", (10 + reductionLvl * 5), (15 + reductionLvl * 5)));

            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 32), reductionReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TANKER_POINT_TOKEN_HIGH.get(), 1), reductionReceipt, 15, 2, 0.05F));
        } else {
            ItemStack maxReceipt = createUpgradeReceipt(turret, "reduction", TurretTradeUtil.tr("changmin_villager_turret.text.tanker.12"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.tanker.13"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.lightning.15"));
            MerchantOffer maxOffer = new MerchantOffer(new ItemStack(Items.EMERALD, 64), maxReceipt, 15, 2, 0.05F);
            maxOffer.setToOutOfStock();
            emeraldOffers.add(maxOffer);
        }

        // 🛡️ 🆕 5. 에메랄드 직접 최대체력 업그레이드 거래 항목 추가
        ItemStack hp1Receipt = createUpgradeReceipt(turret, "hp_1", TurretTradeUtil.tr("changmin_villager_turret.text.tanker.14"),
                TurretTradeUtil.tr("changmin_villager_turret.text.tanker.15"),
                TurretTradeUtil.tr("changmin_villager_turret.text.tanker.16", turret.getExtraMaxHealth()));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), hp1Receipt, 999, 2, 0.05F));

        ItemStack hp64Receipt = createUpgradeReceipt(turret, "hp_64", TurretTradeUtil.tr("changmin_villager_turret.text.tanker.17"),
                TurretTradeUtil.tr("changmin_villager_turret.text.tanker.18"),
                TurretTradeUtil.tr("changmin_villager_turret.text.tanker.16", turret.getExtraMaxHealth()));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 64), hp64Receipt, 999, 2, 0.05F));

        offers.addAll(tokenOffers);
        offers.addAll(emeraldOffers);
    }

    public static ItemStack getBoundToken(TankerTurretEntity turret, int count) {
        return TurretTradeUtil.boundToken(ModItems.TANKER_POINT_TOKEN_LOW.get(), turret, count, TurretTradeUtil.tr("changmin_villager_turret.text.tanker.19"), 0xAAAAAA);
    }

    private static ItemStack createUpgradeReceipt(TankerTurretEntity turret, String type, Component title, Component desc1, Component desc2) {
        return TurretTradeUtil.upgradeReceipt(Items.PAPER, turret, type, title, 0xFF8822,
                TurretTradeUtil.line(TurretTradeUtil.applyNow(), TurretTradeUtil.GREEN),
                TurretTradeUtil.line(desc1, TurretTradeUtil.YELLOW),
                TurretTradeUtil.line(desc2, TurretTradeUtil.CYAN));
    }

    public static void applyUpgradeDirectly(TankerTurretEntity turret, String type) {
        Player player = turret.getTradingPlayer();

        if (type.equals("duration")) {
            turret.setTauntDurationLevel(turret.getTauntDurationLevel() + 1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.tanker.20", (4 + turret.getTauntDurationLevel())), true);
        } else if (type.equals("recharge")) {
            turret.setRechargeLevel(turret.getRechargeLevel() + 1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.tanker.21"), true);
        } else if (type.equals("thorns")) {
            turret.setThornsLevel(turret.getThornsLevel() + 1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.tanker.22", (2 + turret.getThornsLevel())), true);
        } else if (type.equals("reduction")) {
            if (turret.getReductionLevel() < 12) {
                turret.setReductionLevel(turret.getReductionLevel() + 1);
                int finalRed = 10 + turret.getReductionLevel() * 5;
                if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.tanker.23", finalRed), true);
            }
        }
        // 💡 [추가] 최대 체력 강화 적용 시 기획 요구에 맞춰 자가 치유(heal) 관련 연산은 배제하고 한계치만 확장합니다.
        else if (type.equals("hp_1")) {
            turret.setExtraMaxHealth(turret.getExtraMaxHealth() + 4.0D); // 4 HP = 2 Hearts
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.tanker.24"), true);
        } else if (type.equals("hp_64")) {
            turret.setExtraMaxHealth(turret.getExtraMaxHealth() + 150.0D); // 150 HP = 75 Hearts (보너스 적용)
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.tanker.25"), true);
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 0.85F);
        turret.overrideOffers(null);
        if (player != null) {
            turret.openTradingScreen(player, turret.getDisplayName(), 1);
        }
    }
}
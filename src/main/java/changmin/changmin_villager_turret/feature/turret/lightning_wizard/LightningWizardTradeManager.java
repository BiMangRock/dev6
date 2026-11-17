package changmin.changmin_villager_turret.feature.turret.lightning_wizard;

import changmin.changmin_villager_turret.feature.turret.common.TurretTradeUtil;

import changmin.changmin_villager_turret.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class LightningWizardTradeManager {

    public static void populateOffers(LightningWizardEntity turret, MerchantOffers offers) {
        offers.clear();

        MerchantOffers tokenOffers = new MerchantOffers();
        MerchantOffers materialOffers = new MerchantOffers(); // emeraldOffers 명칭을 materialOffers로 통합하여 등급 분류 처리

        // ==========================================
        // 🟢 하급 업그레이드 품목 (하급 토큰 및 철 주괴 1개 사용)
        // ==========================================

        // ⚡ 1. 평타 스턴 지속시간 증대 (번개 전용 하급 토큰 소요)
        int stunLvl = turret.getStunDurationLevel();
        ItemStack stunReceipt = createUpgradeReceipt(turret, "stun", TurretTradeUtil.tr("changmin_villager_turret.text.lightning.1"),
                TurretTradeUtil.tr("changmin_villager_turret.text.lightning.2"),
                TurretTradeUtil.tr("changmin_villager_turret.text.lightning.3", (2.0F + stunLvl * 0.5F), (2.5F + stunLvl * 0.5F)));
        // 🌟 하급에 맞추어 철 주괴 1개 요구로 변경
        materialOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), stunReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_LOW.get(), 1), stunReceipt, 15, 2, 0.05F));

        // ⚡ 2. 평타 적중 시 쿨감 누적 가중치 가속화 (번개 전용 하급 토큰 소요)
        int reductionLvl = turret.getCooldownReductionLevel();
        ItemStack reductionReceipt = createUpgradeReceipt(turret, "reduction", TurretTradeUtil.tr("changmin_villager_turret.text.lightning.4"),
                TurretTradeUtil.tr("changmin_villager_turret.text.lightning.5"),
                TurretTradeUtil.tr("changmin_villager_turret.text.lightning.6", (3 + reductionLvl), (4 + reductionLvl)));
        // 🌟 하급에 맞추어 철 주괴 1개 요구로 변경
        materialOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), reductionReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_LOW.get(), 1), reductionReceipt, 15, 2, 0.05F));


        // ==========================================
        // 🟡 중급 업그레이드 품목 (중급 토큰 및 금 주괴 1개 사용)
        // ==========================================

        // ⚡ 3. 궁극기 폭격 번개 고유 피해 증대 (번개 전용 중급 토큰 소요)
        int dmgLvl = turret.getUltDamageLevel();
        ItemStack dmgReceipt = createUpgradeReceipt(turret, "damage", TurretTradeUtil.tr("changmin_villager_turret.text.lightning.7"),
                TurretTradeUtil.tr("changmin_villager_turret.text.lightning.8"),
                TurretTradeUtil.tr("changmin_villager_turret.text.lightning.9", (5.0D + dmgLvl * 2.5D), (7.5D + dmgLvl * 2.5D)));
        // 🌟 중급에 맞추어 금 주괴 1개 요구로 변경
        materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), dmgReceipt, 10, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_MID.get(), 1), dmgReceipt, 10, 2, 0.05F));


        // ==========================================
        // 🔴 상급 업그레이드 품목 (상급 토큰 및 에메랄드 1개 사용)
        // ==========================================

        // ⚡ 4. 궁극기 뇌우 지속 폭격 시간 증폭 (최대 10단계 제한 / 번개 전용 상급 토큰 소요)
        int durationLvl = turret.getUltDurationLevel();
        if (durationLvl < 7) { // 3초 + 7단계 강화 = 최대 10초 도달 한계
            ItemStack durationReceipt = createUpgradeReceipt(turret, "duration", TurretTradeUtil.tr("changmin_villager_turret.text.lightning.10"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.lightning.11"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.lightning.12", (3 + durationLvl), (4 + durationLvl)));

            // 🌟 상급에 맞추어 에메랄드 1개 요구로 변경
            materialOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), durationReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_HIGH.get(), 1), durationReceipt, 15, 2, 0.05F));
        } else {
            ItemStack maxReceipt = createUpgradeReceipt(turret, "duration", TurretTradeUtil.tr("changmin_villager_turret.text.lightning.13"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.lightning.14"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.lightning.15"));
            MerchantOffer maxOffer = new MerchantOffer(new ItemStack(Items.EMERALD, 1), maxReceipt, 15, 2, 0.05F);
            maxOffer.setToOutOfStock();
            materialOffers.add(maxOffer);
        }

        // ⚡ 5. 궁극기 벼락 범위 확장 (최대 10x10 제한 / 번개 전용 상급 토큰 소요)
        int rangeLvl = turret.getUltRangeLevel();
        if (rangeLvl < 6) { // 기본 4블록 반경 + 6단계 강화 = 최대 10블록 반경(20x20)
            ItemStack rangeReceipt = createUpgradeReceipt(turret, "range", TurretTradeUtil.tr("changmin_villager_turret.text.lightning.16"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.lightning.17"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.lightning.18", (4.0D + rangeLvl), (5.0D + rangeLvl)));

            // 🌟 상급에 맞추어 에메랄드 1개 요구로 변경
            materialOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), rangeReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_HIGH.get(), 1), rangeReceipt, 15, 2, 0.05F));
        }

        offers.addAll(tokenOffers);
        offers.addAll(materialOffers);
    }

    public static ItemStack getBoundToken(LightningWizardEntity turret, int count) {
        return TurretTradeUtil.boundToken(ModItems.LIGHTNING_POINT_TOKEN_LOW.get(), turret, count, TurretTradeUtil.tr("changmin_villager_turret.text.lightning.19"), 0x7E00FF);
    }

    private static ItemStack createUpgradeReceipt(LightningWizardEntity turret, String type, Component title, Component desc1, Component desc2) {
        return TurretTradeUtil.upgradeReceipt(Items.PAPER, turret, type, title, 0x00E1FF,
                TurretTradeUtil.line(TurretTradeUtil.applyNow(), TurretTradeUtil.GREEN),
                TurretTradeUtil.line(desc1, TurretTradeUtil.YELLOW),
                TurretTradeUtil.line(desc2, TurretTradeUtil.CYAN));
    }

    public static void applyUpgradeDirectly(LightningWizardEntity turret, String type) {
        Player player = turret.getTradingPlayer();

        if (type.equals("stun")) {
            turret.setStunDurationLevel(turret.getStunDurationLevel() + 1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.lightning.20", (2.0F + turret.getStunDurationLevel() * 0.5F)), true);
        } else if (type.equals("reduction")) {
            turret.setCooldownReductionLevel(turret.getCooldownReductionLevel() + 1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.lightning.21"), true);
        } else if (type.equals("damage")) {
            turret.setUltDamageLevel(turret.getUltDamageLevel() + 1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.lightning.22", (5.0D + turret.getUltDamageLevel() * 2.5D)), true);
        } else if (type.equals("duration")) {
            if (turret.getUltDurationLevel() < 7) {
                turret.setUltDurationLevel(turret.getUltDurationLevel() + 1);
                if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.lightning.23", (3 + turret.getUltDurationLevel())), true);
            }
        } else if (type.equals("range")) {
            if (turret.getUltRangeLevel() < 6) {
                turret.setUltRangeLevel(turret.getUltRangeLevel() + 1);
                if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.lightning.24", (4.0D + turret.getUltRangeLevel())), true);
            }
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 0.85F);
        turret.overrideOffers(null);
        if (player != null) {
            turret.openTradingScreen(player, turret.getDisplayName(), 1);
        }
    }
}
package changmin.changmin_villager_turret.feature.turret.healer;

import changmin.changmin_villager_turret.feature.turret.common.TurretTradeUtil;

import changmin.changmin_villager_turret.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class HealerTradeManager {

    public static void populateOffers(HealerTurretEntity turret, MerchantOffers offers) {
        offers.clear();

        MerchantOffers tokenOffers = new MerchantOffers();
        MerchantOffers materialOffers = new MerchantOffers();

        // ==========================================
        // 🟢 하급 업그레이드 품목 (하급 토큰 및 철 주괴 1개 사용)
        // ==========================================

        // 1. 쿨타임 단축
        int currentCooldownLvl = turret.getCooldownLevel();
        float currentSec = turret.getCalculatedCooldown() / 20.0F;
        if (currentCooldownLvl < 18) {
            float nextSec = Math.max(20, 200 - (currentCooldownLvl + 1) * 10) / 20.0F;
            ItemStack cooldownReceipt = createUpgradeReceipt(turret, "cooldown", TurretTradeUtil.tr("changmin_villager_turret.text.healer.1"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.healer.2"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.healer.3"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.bee.3", currentSec, nextSec));
            // 🌟 철 주괴 1개 요구로 변경
            materialOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), cooldownReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.HEALER_POINT_TOKEN_LOW.get(), 1), cooldownReceipt, 15, 2, 0.05F));
        }

        // 2. 사거리 확장
        int currentRangeLvl = turret.getRangeLevel();
        ItemStack rangeReceipt = createUpgradeReceipt(turret, "range", TurretTradeUtil.tr("changmin_villager_turret.text.healer.4"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.2"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.5"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.6", (8 + currentRangeLvl), (8 + currentRangeLvl + 1)));
        // 🌟 철 주괴 1개 요구로 변경
        materialOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), rangeReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.HEALER_POINT_TOKEN_LOW.get(), 1), rangeReceipt, 15, 2, 0.05F));


        // ==========================================
        // 🟡 중급 업그레이드 품목 (중급 토큰 및 금 주괴 1개 사용)
        // ==========================================

        // 3. 치유량 강화
        float currentHeal = turret.getHealAmount();
        ItemStack healReceipt = createUpgradeReceipt(turret, "heal_amount", TurretTradeUtil.tr("changmin_villager_turret.text.healer.7"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.2"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.8"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.9", (currentHeal / 2.0F), ((currentHeal + 1.0F) / 2.0F)));
        // 🌟 금 주괴 1개 요구로 변경
        materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), healReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.HEALER_POINT_TOKEN_MID.get(), 1), healReceipt, 15, 2, 0.05F));

        // 4. 과치유 보호막 해금
        ItemStack shieldReceipt = createUpgradeReceipt(turret, "shield", TurretTradeUtil.tr("changmin_villager_turret.text.healer.10"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.11"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.12"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.13", (turret.getShieldLevel() > 0 ? TurretTradeUtil.tr("changmin_villager_turret.text.status.active") : TurretTradeUtil.tr("changmin_villager_turret.text.status.shield_unlock"))));

        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.HEALER_POINT_TOKEN_MID.get(), 1), shieldReceipt, 1, 5, 0.05F));
        // 🌟 금 주괴 1개 요구로 변경
        MerchantOffer shieldMaterialOffer = new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), shieldReceipt, 1, 5, 0.05F);

        if (turret.getShieldLevel() > 0) {
            shieldMaterialOffer.setToOutOfStock();
        }
        materialOffers.add(shieldMaterialOffer);


        // ==========================================
        // 🔴 상급 업그레이드 품목 (상급 토큰 및 에메랄드 1개 사용)
        // ==========================================

        // 5. 광역 치유 비기 해금
        ItemStack aoeReceipt = createUpgradeReceipt(turret, "aoe_heal", TurretTradeUtil.tr("changmin_villager_turret.text.healer.14"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.15"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.16"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.13", (turret.getAoeHealEnabled() > 0 ? TurretTradeUtil.tr("changmin_villager_turret.text.status.active") : TurretTradeUtil.tr("changmin_villager_turret.text.status.aoe_unlock"))));

        MerchantOffer aoeTokenOffer = new MerchantOffer(new ItemStack(ModItems.HEALER_POINT_TOKEN_HIGH.get(), 1), aoeReceipt, 1, 5, 0.05F);
        // 🌟 에메랄드 1개 요구로 변경
        MerchantOffer aoeMaterialOffer = new MerchantOffer(new ItemStack(Items.EMERALD, 1), aoeReceipt, 1, 5, 0.05F);

        if (turret.getTurretLevel() < 3 && turret.getAoeHealEnabled() == 0) {
            aoeTokenOffer.setToOutOfStock();
            aoeMaterialOffer.setToOutOfStock();
        }
        if (turret.getAoeHealEnabled() > 0) {
            aoeTokenOffer.setToOutOfStock();
            aoeMaterialOffer.setToOutOfStock();
        }
        tokenOffers.add(aoeTokenOffer);
        materialOffers.add(aoeMaterialOffer);

        // 6. 상태이상 정화 마법 해금
        ItemStack cleanseReceipt = createUpgradeReceipt(turret, "cleanse", TurretTradeUtil.tr("changmin_villager_turret.text.healer.17"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.18"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.19"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.13", (turret.getCleanseEnabled() > 0 ? TurretTradeUtil.tr("changmin_villager_turret.text.status.active") : TurretTradeUtil.tr("changmin_villager_turret.text.status.cleanse_unlock"))));

        MerchantOffer cleanseTokenOffer = new MerchantOffer(new ItemStack(ModItems.HEALER_POINT_TOKEN_HIGH.get(), 1), cleanseReceipt, 1, 5, 0.05F);
        // 🌟 에메랄드 1개 요구로 변경
        MerchantOffer cleanseMaterialOffer = new MerchantOffer(new ItemStack(Items.EMERALD, 1), cleanseReceipt, 1, 5, 0.05F);

        if (turret.getTurretLevel() < 5 && turret.getCleanseEnabled() == 0) {
            cleanseTokenOffer.setToOutOfStock();
            cleanseMaterialOffer.setToOutOfStock();
        }
        if (turret.getCleanseEnabled() > 0) {
            cleanseTokenOffer.setToOutOfStock();
            cleanseMaterialOffer.setToOutOfStock();
        }
        tokenOffers.add(cleanseTokenOffer);
        materialOffers.add(cleanseMaterialOffer);

        offers.addAll(tokenOffers);
        offers.addAll(materialOffers);
    }

    public static ItemStack getBoundToken(HealerTurretEntity turret, int count, int tier) {
        // tier: 0 하급(골드), 1 중급(녹색), 2 상급(자주)
        if (tier == 1) {
            return TurretTradeUtil.boundToken(ModItems.HEALER_POINT_TOKEN_MID.get(), turret, count, TurretTradeUtil.tr("changmin_villager_turret.text.healer.20"), 0x55FF55);
        } else if (tier == 2) {
            return TurretTradeUtil.boundToken(ModItems.HEALER_POINT_TOKEN_HIGH.get(), turret, count, TurretTradeUtil.tr("changmin_villager_turret.text.healer.21"), 0xFF55FF);
        }
        return TurretTradeUtil.boundToken(ModItems.HEALER_POINT_TOKEN_LOW.get(), turret, count, TurretTradeUtil.tr("changmin_villager_turret.text.healer.22"), 0xFFD700);
    }

    public static ItemStack createUpgradeReceipt(HealerTurretEntity turret, String type, Component title, Component desc1, Component desc2, Component currentLvlInfo) {
        return TurretTradeUtil.upgradeReceipt(Items.PAPER, turret, type, title, 0x55FFFF,
                TurretTradeUtil.line(desc1, TurretTradeUtil.GREEN),
                TurretTradeUtil.line(desc2, TurretTradeUtil.YELLOW),
                TurretTradeUtil.line(currentLvlInfo, TurretTradeUtil.MAGENTA));
    }

    public static void applyUpgradeDirectly(HealerTurretEntity turret, String type) {
        Player tradingPlayer = turret.getTradingPlayer();

        if (type.equals("heal_amount")) {
            turret.setHealAmount(turret.getHealAmount() + 1.0F);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.healer.23", (turret.getHealAmount() / 2.0F)), true);
            }
        } else if (type.equals("cooldown")) {
            if (turret.getCooldownLevel() < 18) {
                turret.setCooldownLevel(turret.getCooldownLevel() + 1);
                if (tradingPlayer != null) {
                    float sec = turret.getCalculatedCooldown() / 20.0F;
                    tradingPlayer.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.healer.24", sec), true);
                }
            }
        } else if (type.equals("range")) {
            turret.setRangeLevel(turret.getRangeLevel() + 1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.healer.25", (8 + turret.getRangeLevel())), true);
            }
        } else if (type.equals("aoe_heal")) {
            turret.setAoeHealEnabled(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.healer.26"), true);
            }
        } else if (type.equals("cleanse")) {
            turret.setCleanseEnabled(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.healer.27"), true);
            }
        } else if (type.equals("shield")) {
            turret.setShieldLevel(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.healer.28"), true);
            }
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 1.0F);

        turret.overrideOffers(null);
        if (tradingPlayer != null) {
            turret.openTradingScreen(tradingPlayer, turret.getDisplayName(), turret.getVillagerXp());
        }
    }
}
package changmin.changmin_villager_turret.feature.turret.plasma_wizard;

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

public class PlasmaWizardTradeManager {

    public static void populateOffers(PlasmaWizardEntity turret, MerchantOffers offers) {
        offers.clear();

        MerchantOffers tokenOffers = new MerchantOffers();
        MerchantOffers materialOffers = new MerchantOffers();

        // ==========================================
        // 🟢 하급 업그레이드 (하급 토큰 및 철 주괴 1개 요구)
        // ==========================================
        int cdLvl = turret.getCooldownLevel();
        if (cdLvl < 15) {
            float curSec = turret.getCalculatedCooldown() / 20.0F;
            float nextSec = Math.max(15, 50 - (cdLvl + 1) * 2) / 20.0F;
            ItemStack cdReceipt = createUpgradeReceipt(turret, "cooldown", TurretTradeUtil.tr("changmin_villager_turret.text.plasma.1"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.plasma.2"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.plasma.3"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.plasma.4", curSec, nextSec));
            // 🌟 철 주괴 요구 수량을 16개에서 1개로 변경
            materialOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), cdReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.PLASMA_POINT_TOKEN_LOW.get(), 1), cdReceipt, 15, 2, 0.05F));
        }

        int szLvl = turret.getSizeLevel();
        if (szLvl < 5) {
            float curScale = turret.getCalculatedScale();
            float nextScale = 1.0F + (szLvl + 1) * 0.25F;
            ItemStack szReceipt = createUpgradeReceipt(turret, "size", TurretTradeUtil.tr("changmin_villager_turret.text.plasma.5"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.plasma.6"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.plasma.7"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.plasma.8", curScale, nextScale));
            // 🌟 철 주괴 요구 수량을 16개에서 1개로 변경
            materialOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), szReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.PLASMA_POINT_TOKEN_LOW.get(), 1), szReceipt, 15, 2, 0.05F));
        }

        // ==========================================
        // 🟡 중급 업그레이드 (중급 토큰 및 금 주괴 1개 요구)
        // ==========================================
        int dmgLvl = turret.getDamageLevel();
        if (dmgLvl < 10) {
            float curDmg = turret.getCalculatedDamage();
            float nextDmg = curDmg + 1.5F;
            ItemStack dmgReceipt = createUpgradeReceipt(turret, "damage", TurretTradeUtil.tr("changmin_villager_turret.text.plasma.9"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.plasma.10"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.plasma.11"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.plasma.12", curDmg, nextDmg));
            // 🌟 금 주괴 요구 수량을 16개에서 1개로 변경
            materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), dmgReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.PLASMA_POINT_TOKEN_MID.get(), 1), dmgReceipt, 15, 2, 0.05F));
        }

        int stnLvl = turret.getStunLevel();
        if (stnLvl < 5) {
            float curSec = turret.getCalculatedStunDuration() / 20.0F;
            float nextSec = curSec + 1.0F;
            ItemStack stnReceipt = createUpgradeReceipt(turret, "stun", TurretTradeUtil.tr("changmin_villager_turret.text.plasma.13"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.plasma.14"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.plasma.15"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.bee.3", curSec, nextSec));
            // 🌟 금 주괴 요구 수량을 16개에서 1개로 변경
            materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), stnReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.PLASMA_POINT_TOKEN_MID.get(), 1), stnReceipt, 15, 2, 0.05F));
        }

        // ==========================================
        // 🔴 상급 업그레이드 (상급 토큰 및 에메랄드 1개 요구)
        // ==========================================
        ItemStack splitReceipt = createUpgradeReceipt(turret, "split_shot", TurretTradeUtil.tr("changmin_villager_turret.text.plasma.16"),
                TurretTradeUtil.tr("changmin_villager_turret.text.plasma.17"),
                TurretTradeUtil.tr("changmin_villager_turret.text.plasma.18"),
                TurretTradeUtil.tr("changmin_villager_turret.text.healer.13", (turret.getSplitShotLevel() == 0 ? TurretTradeUtil.tr("changmin_villager_turret.text.status.split_1") : (turret.getSplitShotLevel() == 1 ? TurretTradeUtil.tr("changmin_villager_turret.text.status.split_2") : TurretTradeUtil.tr("changmin_villager_turret.text.status.split_max")))));

        MerchantOffer splitTokenOffer = new MerchantOffer(new ItemStack(ModItems.PLASMA_POINT_TOKEN_HIGH.get(), 1), splitReceipt, 1, 5, 0.05F);
        // 🌟 에메랄드 요구 수량을 20개에서 1개로 변경
        MerchantOffer splitMaterialOffer = new MerchantOffer(new ItemStack(Items.EMERALD, 1), splitReceipt, 1, 5, 0.05F);

        if (turret.getTurretLevel() < 4 || turret.getSplitShotLevel() >= 2) {
            splitTokenOffer.setToOutOfStock();
            splitMaterialOffer.setToOutOfStock();
        }
        tokenOffers.add(splitTokenOffer);
        materialOffers.add(splitMaterialOffer);

        offers.addAll(tokenOffers);
        offers.addAll(materialOffers);
    }

    public static ItemStack getBoundToken(PlasmaWizardEntity turret, int count, int tier) {
        // tier: 0 하급(골드), 1 중급(녹색), 2 상급(자주)
        if (tier == 1) {
            return TurretTradeUtil.boundToken(ModItems.PLASMA_POINT_TOKEN_MID.get(), turret, count, TurretTradeUtil.tr("changmin_villager_turret.text.plasma.19"), 0x55FF55);
        } else if (tier == 2) {
            return TurretTradeUtil.boundToken(ModItems.PLASMA_POINT_TOKEN_HIGH.get(), turret, count, TurretTradeUtil.tr("changmin_villager_turret.text.plasma.20"), 0xFF55FF);
        }
        return TurretTradeUtil.boundToken(ModItems.PLASMA_POINT_TOKEN_LOW.get(), turret, count, TurretTradeUtil.tr("changmin_villager_turret.text.plasma.21"), 0xFFD700);
    }

    public static ItemStack createUpgradeReceipt(PlasmaWizardEntity turret, String type, Component title, Component desc1, Component desc2, Component currentLvlInfo) {
        return TurretTradeUtil.upgradeReceipt(Items.PAPER, turret, type, title, 0x55FFFF,
                TurretTradeUtil.line(desc1, TurretTradeUtil.GREEN),
                TurretTradeUtil.line(desc2, TurretTradeUtil.YELLOW),
                TurretTradeUtil.line(currentLvlInfo, TurretTradeUtil.MAGENTA));
    }

    public static void applyUpgradeDirectly(PlasmaWizardEntity turret, String type) {
        Player player = turret.getTradingPlayer();

        if (type.equals("cooldown")) {
            if (turret.getCooldownLevel() < 15) {
                turret.setCooldownLevel(turret.getCooldownLevel() + 1);
                if (player != null) {
                    player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.plasma.22", (turret.getCalculatedCooldown() / 20.0F)), true);
                }
            }
        } else if (type.equals("size")) {
            if (turret.getSizeLevel() < 5) {
                turret.setSizeLevel(turret.getSizeLevel() + 1);
                if (player != null) {
                    player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.plasma.23", turret.getCalculatedScale()), true);
                }
            }
        } else if (type.equals("damage")) {
            if (turret.getDamageLevel() < 10) {
                turret.setDamageLevel(turret.getDamageLevel() + 1);
                if (player != null) {
                    player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.plasma.24", turret.getCalculatedDamage()), true);
                }
            }
        } else if (type.equals("stun")) {
            if (turret.getStunLevel() < 5) {
                turret.setStunLevel(turret.getStunLevel() + 1);
                if (player != null) {
                    player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.plasma.25", (turret.getCalculatedStunDuration() / 20.0F)), true);
                }
            }
        } else if (type.equals("split_shot")) {
            if (turret.getSplitShotLevel() < 2) {
                turret.setSplitShotLevel(turret.getSplitShotLevel() + 1);
                if (player != null) {
                    player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.plasma.26", (turret.getSplitShotLevel() + 1)), true);
                }
            }
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 1.0F);
        turret.overrideOffers(null);
        if (player != null) {
            turret.openTradingScreen(player, turret.getDisplayName(), turret.getVillagerXp());
        }
    }
}
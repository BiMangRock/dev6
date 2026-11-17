package changmin.changmin_villager_turret.feature.turret.trident_turret;

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

public class TridentTurretTradeManager {

    public static void populateOffers(TridentTurretEntity turret, MerchantOffers offers) {
        offers.clear();

        MerchantOffers tokenOffers = new MerchantOffers();
        MerchantOffers emeraldOffers = new MerchantOffers();

        // 🔄 사격 모드 실시간 토글 전환권
        int mode = turret.getShootMode();
        if (mode == 0) {
            ItemStack toggleReceipt = createUpgradeReceipt(turret, "toggle_pattern", TurretTradeUtil.tr("changmin_villager_turret.text.trident.1"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.trident.2"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.trident.3"));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), toggleReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_LOW.get(), 1), toggleReceipt, 15, 2, 0.05F));
        } else {
            ItemStack toggleReceipt = createUpgradeReceipt(turret, "toggle_pattern", TurretTradeUtil.tr("changmin_villager_turret.text.trident.4"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.trident.5"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.trident.6"));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), toggleReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_LOW.get(), 1), toggleReceipt, 15, 2, 0.05F));
        }

        // 🔱 1. 삼지창 발사 수 (무제한)
        int currentCount = turret.getTridentCount();
        ItemStack receipt = createUpgradeReceipt(turret, "count", TurretTradeUtil.tr("changmin_villager_turret.text.trident.7"),
                TurretTradeUtil.tr("changmin_villager_turret.text.trident.8"),
                TurretTradeUtil.tr("changmin_villager_turret.text.trident.9", currentCount, (currentCount + 1)));

        int emeraldCost = Math.min(64, 16 + currentCount * 4);
        int tokenCost = Math.min(10, 1 + currentCount / 2);
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, emeraldCost), receipt, 10, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_MID.get(), tokenCost), receipt, 10, 2, 0.05F));

        // 🔱 2. 공격력 강화 (날카로움)
        int dmgLvl = turret.getDamageLevel();
        ItemStack dmgReceipt = createUpgradeReceipt(turret, "damage", TurretTradeUtil.tr("changmin_villager_turret.text.trident.10"),
                TurretTradeUtil.tr("changmin_villager_turret.text.trident.11"),
                TurretTradeUtil.tr("changmin_villager_turret.text.trident.12", dmgLvl, (dmgLvl + 1)));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), dmgReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_LOW.get(), 1), dmgReceipt, 15, 2, 0.05F));

        // 🔱 3. 인식 사거리 강화
        int rangeLvl = turret.getRangeLevel();
        ItemStack rangeReceipt = createUpgradeReceipt(turret, "range", TurretTradeUtil.tr("changmin_villager_turret.text.trident.13"),
                TurretTradeUtil.tr("changmin_villager_turret.text.trident.14"),
                TurretTradeUtil.tr("changmin_villager_turret.text.trident.15", (16 + rangeLvl * 4), (16 + (rangeLvl + 1) * 4)));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 12), rangeReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_LOW.get(), 1), rangeReceipt, 15, 2, 0.05F));

        // 🔱 4. 재장전 속도 단축
        int rechargeLvl = turret.getRechargeLevel();
        ItemStack rechargeReceipt = createUpgradeReceipt(turret, "recharge", TurretTradeUtil.tr("changmin_villager_turret.text.trident.16"),
                TurretTradeUtil.tr("changmin_villager_turret.text.trident.17"),
                TurretTradeUtil.tr("changmin_villager_turret.text.trident.18", rechargeLvl, (rechargeLvl + 1)));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), rechargeReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_LOW.get(), 1), rechargeReceipt, 15, 2, 0.05F));

        // 🔱 5. 집뢰(벼락) 기술 무장 개조
        if (turret.getLightningLevel() == 0) {
            ItemStack lightningReceipt = createUpgradeReceipt(turret, "lightning", TurretTradeUtil.tr("changmin_villager_turret.text.trident.19"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.trident.20"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.trident.21"));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 48), lightningReceipt, 1, 10, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_HIGH.get(), 1), lightningReceipt, 1, 10, 0.05F));
        }

        // ==========================================
        // 🔱 6. [신규 추가] 해류 관통 개조 (최대 10레벨 제한, 상급 토큰 소요)
        // ==========================================
        int pierceLvl = turret.getPierceLevel();
        if (pierceLvl < 10) {
            ItemStack pierceReceipt = createUpgradeReceipt(turret, "pierce", TurretTradeUtil.tr("changmin_villager_turret.text.trident.22"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.trident.23"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.trident.24", pierceLvl, (pierceLvl + 1)));

            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 32), pierceReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_HIGH.get(), 1), pierceReceipt, 15, 2, 0.05F));
        }

        offers.addAll(tokenOffers);
        offers.addAll(emeraldOffers);
    }

    public static ItemStack getBoundToken(TridentTurretEntity turret, int count) {
        return TurretTradeUtil.boundToken(ModItems.TRIDENT_POINT_TOKEN_LOW.get(), turret, count, TurretTradeUtil.tr("changmin_villager_turret.text.trident.25"), 0x33CCFF);
    }

    private static ItemStack createUpgradeReceipt(TridentTurretEntity turret, String type, Component title, Component desc1, Component desc2) {
        return TurretTradeUtil.upgradeReceipt(Items.PAPER, turret, type, title, 0x33CCFF,
                TurretTradeUtil.line(TurretTradeUtil.applyNow(), TurretTradeUtil.GREEN),
                TurretTradeUtil.line(desc1, TurretTradeUtil.YELLOW),
                TurretTradeUtil.line(desc2, TurretTradeUtil.CYAN));
    }

    public static void applyUpgradeDirectly(TridentTurretEntity turret, String type) {
        Player player = turret.getTradingPlayer();

        if (type.equals("count")) {
            turret.setTridentCount(turret.getTridentCount() + 1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.trident.26", turret.getTridentCount()), true);
        } else if (type.equals("damage")) {
            turret.setDamageLevel(turret.getDamageLevel() + 1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.trident.27", turret.getDamageLevel()), true);
        } else if (type.equals("range")) {
            turret.setRangeLevel(turret.getRangeLevel() + 1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.trident.28", (16 + turret.getRangeLevel() * 4)), true);
        } else if (type.equals("recharge")) {
            turret.setRechargeLevel(turret.getRechargeLevel() + 1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.trident.29"), true);
        } else if (type.equals("lightning")) {
            turret.setLightningLevel(1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.trident.30"), true);
        } else if (type.equals("toggle_pattern")) {
            int newMode = turret.getShootMode() == 0 ? 1 : 0;
            turret.setShootMode(newMode);
            Component modeName = newMode == 0 ? TurretTradeUtil.tr("changmin_villager_turret.text.trident.31") : TurretTradeUtil.tr("changmin_villager_turret.text.trident.32");
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.trident.33", modeName), true);
        } else if (type.equals("pierce")) {
            // 🆕 관통 레벨 업그레이드 수령 처리 (최대 10마리 제한 적용)
            if (turret.getPierceLevel() < 10) {
                turret.setPierceLevel(turret.getPierceLevel() + 1);
                if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.trident.34", turret.getPierceLevel()), true);
            }
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 0.85F);
        turret.overrideOffers(null);
        if (player != null) {
            turret.openTradingScreen(player, turret.getDisplayName(), 1);
        }
    }
}
package changmin.changmin_villager_turret.feature.turret.bee_summoner_turret;

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

public class BeeSummonerTradeManager {

    public static void populateOffers(BeeSummonerTurretEntity turret, MerchantOffers offers) {
        offers.clear();

        MerchantOffers tokenOffers = new MerchantOffers();
        MerchantOffers mineralOffers = new MerchantOffers();

        // 🐝 1. 벌 지속시간 강화 (하급: 철주괴 1개 혹은 하급 토큰 1개)
        int durationLvl = turret.getBeeDurationLevel();
        ItemStack durationReceipt = createUpgradeReceipt(turret, "bee_duration", TurretTradeUtil.tr("changmin_villager_turret.text.bee.1"),
                TurretTradeUtil.tr("changmin_villager_turret.text.bee.2"),
                TurretTradeUtil.tr("changmin_villager_turret.text.bee.3", (15 + durationLvl * 5), (15 + (durationLvl + 1) * 5)));
        mineralOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), durationReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_LOW.get(), 1), durationReceipt, 15, 2, 0.05F));

        // 🐝 2. 벌 공격력 강화 (하급: 철주괴 1개 혹은 하급 토큰 1개)
        int dmgLvl = turret.getBeeDamageLevel();
        ItemStack dmgReceipt = createUpgradeReceipt(turret, "bee_damage", TurretTradeUtil.tr("changmin_villager_turret.text.bee.4"),
                TurretTradeUtil.tr("changmin_villager_turret.text.bee.5"),
                TurretTradeUtil.tr("changmin_villager_turret.text.bee.6", (2.0F + dmgLvl * 1.5F), (2.0F + (dmgLvl + 1) * 1.5F)));
        mineralOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), dmgReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_LOW.get(), 1), dmgReceipt, 15, 2, 0.05F));

        // 🐝 3. 적 찾는 정찰 범위 조율 (하급: 철주괴 1개 혹은 하급 토큰 1개)
        int rangeLvl = turret.getRangeLevel();
        ItemStack rangeReceipt = createUpgradeReceipt(turret, "range", TurretTradeUtil.tr("changmin_villager_turret.text.bee.7"),
                TurretTradeUtil.tr("changmin_villager_turret.text.bee.8"),
                TurretTradeUtil.tr("changmin_villager_turret.text.bee.9", (16 + rangeLvl * 4), (16 + (rangeLvl + 1) * 4)));
        mineralOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), rangeReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_LOW.get(), 1), rangeReceipt, 15, 2, 0.05F));

        // 🐝 4. 🆕 글로벌 소환 대기시간(쿨타임) 단축 (하급: 철주괴 1개 혹은 하급 토큰 1개, 최대 4레벨 한정)
        int cooldownLvl = turret.getBeeCooldownLevel();
        if (cooldownLvl < 4) {
            ItemStack cooldownReceipt = createUpgradeReceipt(turret, "bee_cooldown", TurretTradeUtil.tr("changmin_villager_turret.text.bee.10"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.bee.11"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.bee.12", (30 - cooldownLvl * 5), (30 - (cooldownLvl + 1) * 5)));
            mineralOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), cooldownReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_LOW.get(), 1), cooldownReceipt, 15, 2, 0.05F));
        }

        // 🐝 5. 🆕 벌의 생성 최대 체력 강화 (하급: 철주괴 1개 혹은 하급 토큰 1개, 제한 없음)
        int hpLvl = turret.getBeeHealthLevel();
        ItemStack hpReceipt = createUpgradeReceipt(turret, "bee_health", TurretTradeUtil.tr("changmin_villager_turret.text.bee.13"),
                TurretTradeUtil.tr("changmin_villager_turret.text.bee.14"),
                TurretTradeUtil.tr("changmin_villager_turret.text.bee.15", (hpLvl * 2), ((hpLvl + 1) * 2)));
        mineralOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), hpReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_LOW.get(), 1), hpReceipt, 15, 2, 0.05F));

        // 🐝 6. 마릿수 상한 돌파 (중급: 금주괴 1개 혹은 중급 토큰 1개, 최대 15마리 한계 제한 락)
        int maxBees = turret.getMaxBees();
        if (maxBees < 15) {
            ItemStack capReceipt = createUpgradeReceipt(turret, "max_bees", TurretTradeUtil.tr("changmin_villager_turret.text.bee.16"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.bee.17"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.bee.18", maxBees, (maxBees + 1)));
            mineralOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), capReceipt, 10, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_MID.get(), 1), capReceipt, 10, 2, 0.05F));
        }

        // 🐝 7. 벌 슬로우 주입 연구 (중급: 금주괴 1개 혹은 중급 토큰 1개, 최대 10레벨 제한)
        int slowLvl = turret.getBeeSlownessLevel();
        if (slowLvl < 10) {
            ItemStack slowReceipt = createUpgradeReceipt(turret, "bee_slowness", TurretTradeUtil.tr("changmin_villager_turret.text.bee.19"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.bee.20"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.bee.21", slowLvl, (slowLvl + 1)));
            mineralOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), slowReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_MID.get(), 1), slowReceipt, 15, 2, 0.05F));
        }

        // 🐝 8. 벌 약화 효과 주입 연구 (중급: 금주괴 1개 혹은 중급 토큰 1개, 최대 10레벨 제한)
        int weakLvl = turret.getBeeWeaknessLevel();
        if (weakLvl < 10) {
            ItemStack weakReceipt = createUpgradeReceipt(turret, "bee_weakness", TurretTradeUtil.tr("changmin_villager_turret.text.bee.22"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.bee.23"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.bee.24", weakLvl, (weakLvl + 1)));
            mineralOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), weakReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_MID.get(), 1), weakReceipt, 15, 2, 0.05F));
        }

        // 🐝 9. 벌 일반 독성 주입 연구 (상급: 에메랄드 1개 혹은 상급 토큰 1개, 최대 10레벨 제한)
        int poisonLvl = turret.getBeePoisonLevel();
        if (poisonLvl < 10) {
            ItemStack poisonReceipt = createUpgradeReceipt(turret, "bee_poison", TurretTradeUtil.tr("changmin_villager_turret.text.bee.25"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.bee.26"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.bee.27", poisonLvl, (poisonLvl + 1)));
            mineralOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), poisonReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_HIGH.get(), 1), poisonReceipt, 15, 2, 0.05F));
        }

        // 🐝 10. 벌 위더 독성 주입 연구 (상급: 에메랄드 1개 혹은 상급 토큰 1개, 최대 10레벨 제한)
        int witherLvl = turret.getBeeWitherLevel();
        if (witherLvl < 10) {
            ItemStack witherReceipt = createUpgradeReceipt(turret, "bee_wither", TurretTradeUtil.tr("changmin_villager_turret.text.bee.28"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.bee.29"),
                    TurretTradeUtil.tr("changmin_villager_turret.text.bee.30", witherLvl, (witherLvl + 1)));
            mineralOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), witherReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_HIGH.get(), 1), witherReceipt, 15, 2, 0.05F));
        }

        offers.addAll(tokenOffers);
        offers.addAll(mineralOffers);
    }

    public static ItemStack getBoundToken(BeeSummonerTurretEntity turret, int count) {
        return TurretTradeUtil.boundToken(ModItems.BEE_POINT_TOKEN_LOW.get(), turret, count, TurretTradeUtil.tr("changmin_villager_turret.text.bee.31"), 0xFFE066);
    }

    private static ItemStack createUpgradeReceipt(BeeSummonerTurretEntity turret, String type, Component title, Component desc1, Component desc2) {
        return TurretTradeUtil.upgradeReceipt(Items.PAPER, turret, type, title, 0xFFCC33,
                TurretTradeUtil.line(TurretTradeUtil.applyNow(), TurretTradeUtil.GREEN),
                TurretTradeUtil.line(desc1, TurretTradeUtil.YELLOW),
                TurretTradeUtil.line(desc2, TurretTradeUtil.CYAN));
    }

    public static void applyUpgradeDirectly(BeeSummonerTurretEntity turret, String type) {
        Player player = turret.getTradingPlayer();

        if (type.equals("bee_duration")) {
            turret.setBeeDurationLevel(turret.getBeeDurationLevel() + 1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.bee.32", turret.getBeeDurationLevel()), true);
        } else if (type.equals("bee_damage")) {
            turret.setBeeDamageLevel(turret.getBeeDamageLevel() + 1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.bee.33", turret.getBeeDamageLevel()), true);
        } else if (type.equals("range")) {
            turret.setRangeLevel(turret.getRangeLevel() + 1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.bee.34", (16 + turret.getRangeLevel() * 4)), true);
        } else if (type.equals("max_bees")) {
            if (turret.getMaxBees() < 15) {
                turret.setMaxBees(turret.getMaxBees() + 1);
                if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.bee.35", turret.getMaxBees()), true);
            }
        } else if (type.equals("bee_slowness")) {
            if (turret.getBeeSlownessLevel() < 10) {
                turret.setBeeSlownessLevel(turret.getBeeSlownessLevel() + 1);
                if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.bee.36", turret.getBeeSlownessLevel()), true);
            }
        } else if (type.equals("bee_weakness")) {
            if (turret.getBeeWeaknessLevel() < 10) {
                turret.setBeeWeaknessLevel(turret.getBeeWeaknessLevel() + 1);
                if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.bee.37", turret.getBeeWeaknessLevel()), true);
            }
        } else if (type.equals("bee_poison")) {
            if (turret.getBeePoisonLevel() < 10) {
                turret.setBeePoisonLevel(turret.getBeePoisonLevel() + 1);
                if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.bee.38", turret.getBeePoisonLevel()), true);
            }
        } else if (type.equals("bee_wither")) {
            if (turret.getBeeWitherLevel() < 10) {
                turret.setBeeWitherLevel(turret.getBeeWitherLevel() + 1);
                if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.bee.39", turret.getBeeWitherLevel()), true);
            }
        } else if (type.equals("bee_cooldown")) { // 🆕 소환 대기시간 감소 연동
            if (turret.getBeeCooldownLevel() < 4) {
                turret.setBeeCooldownLevel(turret.getBeeCooldownLevel() + 1);
                if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.bee.40", (30 - turret.getBeeCooldownLevel() * 5)), true);
            }
        } else if (type.equals("bee_health")) { // 🆕 벌 생성 최대 체력 강화 연동
            turret.setBeeHealthLevel(turret.getBeeHealthLevel() + 1);
            if (player != null) player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.bee.41", (turret.getBeeHealthLevel() * 2)), true);
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 0.85F);
        turret.overrideOffers(null);
        if (player != null) {
            turret.openTradingScreen(player, turret.getDisplayName(), 1);
        }
    }
}
package changmin.myMod.feature.turret.bee_summoner_turret;

import changmin.myMod.registry.ModItems;
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
        ItemStack durationReceipt = createUpgradeReceipt(turret, "bee_duration", "벌 생물학: 생존력 조절",
                "효과: 소환된 벌의 지상 전장 유지시간이 강화됩니다.",
                "현재 상태: " + (15 + durationLvl * 5) + "초 ➔ " + (15 + (durationLvl + 1) * 5) + "초");
        mineralOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), durationReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_LOW.get(), 1), durationReceipt, 15, 2, 0.05F));

        // 🐝 2. 벌 공격력 강화 (하급: 철주괴 1개 혹은 하급 토큰 1개)
        int dmgLvl = turret.getBeeDamageLevel();
        ItemStack dmgReceipt = createUpgradeReceipt(turret, "bee_damage", "벌 생물학: 강갑 침 정공법",
                "효과: 벌들의 기습 타격 무장 위력이 비약적으로 증가합니다.",
                "현재 공격력: " + (2.0F + dmgLvl * 1.5F) + " ➔ " + (2.0F + (dmgLvl + 1) * 1.5F));
        mineralOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), dmgReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_LOW.get(), 1), dmgReceipt, 15, 2, 0.05F));

        // 🐝 3. 적 찾는 정찰 범위 조율 (하급: 철주괴 1개 혹은 하급 토큰 1개)
        int rangeLvl = turret.getRangeLevel();
        ItemStack rangeReceipt = createUpgradeReceipt(turret, "range", "군집 특성: 정찰 구역 탐색 최적화",
                "효과: 터렛의 감지 영역 및 벌들이 개별적으로 색적하는 범위가 크게 연장됩니다.",
                "현재 범위: " + (16 + rangeLvl * 4) + "m ➔ " + (16 + (rangeLvl + 1) * 4) + "m");
        mineralOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), rangeReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_LOW.get(), 1), rangeReceipt, 15, 2, 0.05F));

        // 🐝 4. 🆕 글로벌 소환 대기시간(쿨타임) 단축 (하급: 철주괴 1개 혹은 하급 토큰 1개, 최대 4레벨 한정)
        int cooldownLvl = turret.getBeeCooldownLevel();
        if (cooldownLvl < 4) {
            ItemStack cooldownReceipt = createUpgradeReceipt(turret, "bee_cooldown", "군집 특성: 소환 주기 단축",
                    "효과: 일시 소환 웨이브의 글로벌 대기시간을 대폭 단축시킵니다.",
                    "현재 상태: " + (30 - cooldownLvl * 5) + "초 ➔ " + (30 - (cooldownLvl + 1) * 5) + "초 (최소 10초)");
            mineralOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), cooldownReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_LOW.get(), 1), cooldownReceipt, 15, 2, 0.05F));
        }

        // 🐝 5. 🆕 벌의 생성 최대 체력 강화 (하급: 철주괴 1개 혹은 하급 토큰 1개, 제한 없음)
        int hpLvl = turret.getBeeHealthLevel();
        ItemStack hpReceipt = createUpgradeReceipt(turret, "bee_health", "벌 생물학: 외골격 갑갑 조율",
                "효과: 소환된 벌들의 최대 체력이 영구적으로 가산됩니다.",
                "현재 가산 체력: +" + (hpLvl * 2) + " HP ➔ +" + ((hpLvl + 1) * 2) + " HP");
        mineralOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), hpReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_LOW.get(), 1), hpReceipt, 15, 2, 0.05F));

        // 🐝 6. 마릿수 상한 돌파 (중급: 금주괴 1개 혹은 중급 토큰 1개, 최대 15마리 한계 제한 락)
        int maxBees = turret.getMaxBees();
        if (maxBees < 15) {
            ItemStack capReceipt = createUpgradeReceipt(turret, "max_bees", "군집 특성: 군락 관리망 개방",
                    "효과: 동시에 유지가 가능한 벌들의 개체 수가 증가합니다.",
                    "현재 한도: " + maxBees + "마리 ➔ " + (maxBees + 1) + "마리 (최대 15마리)");
            mineralOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), capReceipt, 10, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_MID.get(), 1), capReceipt, 10, 2, 0.05F));
        }

        // 🐝 7. 벌 슬로우 주입 연구 (중급: 금주괴 1개 혹은 중급 토큰 1개, 최대 10레벨 제한)
        int slowLvl = turret.getBeeSlownessLevel();
        if (slowLvl < 10) {
            ItemStack slowReceipt = createUpgradeReceipt(turret, "bee_slowness", "벌 생물학: 신경 마비독 주입",
                    "효과: 벌침 피격 대상의 이동 속도를 크게 무너뜨려 둔화시킵니다.",
                    "현재 슬로우 등급: " + slowLvl + "레벨 ➔ " + (slowLvl + 1) + "레벨");
            mineralOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), slowReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_MID.get(), 1), slowReceipt, 15, 2, 0.05F));
        }

        // 🐝 8. 벌 약화 효과 주입 연구 (중급: 금주괴 1개 혹은 중급 토큰 1개, 최대 10레벨 제한)
        int weakLvl = turret.getBeeWeaknessLevel();
        if (weakLvl < 10) {
            ItemStack weakReceipt = createUpgradeReceipt(turret, "bee_weakness", "벌 생물학: 피로 위축독 수혈",
                    "효과: 벌침 피격 대상의 공격 파괴력을 대폭 무력화시킵니다.",
                    "현재 약화 등급: " + weakLvl + "레벨 ➔ " + (weakLvl + 1) + "레벨");
            mineralOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), weakReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_MID.get(), 1), weakReceipt, 15, 2, 0.05F));
        }

        // 🐝 9. 벌 일반 독성 주입 연구 (상급: 에메랄드 1개 혹은 상급 토큰 1개, 최대 10레벨 제한)
        int poisonLvl = turret.getBeePoisonLevel();
        if (poisonLvl < 10) {
            ItemStack poisonReceipt = createUpgradeReceipt(turret, "bee_poison", "벌 생물학: 생화학 독성 수혈",
                    "효과: 적 공격 시 침에 치명적인 신경독 디버프를 중독시킵니다.",
                    "현재 독 등급: " + poisonLvl + "레벨 ➔ " + (poisonLvl + 1) + "레벨");
            mineralOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), poisonReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_HIGH.get(), 1), poisonReceipt, 15, 2, 0.05F));
        }

        // 🐝 10. 벌 위더 독성 주입 연구 (상급: 에메랄드 1개 혹은 상급 토큰 1개, 최대 10레벨 제한)
        int witherLvl = turret.getBeeWitherLevel();
        if (witherLvl < 10) {
            ItemStack witherReceipt = createUpgradeReceipt(turret, "bee_wither", "벌 생물학: 부식 위더침 개조",
                    "효과: 벌침에 직접 대상을 소멸시키는 가공할 부식 위더독을 추가합니다.",
                    "현재 위더 등급: " + witherLvl + "레벨 ➔ " + (witherLvl + 1) + "레벨");
            mineralOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), witherReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.BEE_POINT_TOKEN_HIGH.get(), 1), witherReceipt, 15, 2, 0.05F));
        }

        offers.addAll(tokenOffers);
        offers.addAll(mineralOffers);
    }

    public static ItemStack getBoundToken(BeeSummonerTurretEntity turret, int count) {
        ItemStack token = new ItemStack(ModItems.BEE_POINT_TOKEN_LOW.get(), count);
        CompoundTag tag = token.getOrCreateTag();
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(new TextComponent("벌소환사 터렛 포인트 토큰 (하급)").withStyle(style -> style.withColor(0xFFE066).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("허용 터렛 종류: " + turret.getType().getDescription().getString()).withStyle(style -> style.withColor(0xAAAAAA)))));
        display.put("Lore", lore);
        tag.put("display", display);

        return token;
    }

    private static ItemStack createUpgradeReceipt(BeeSummonerTurretEntity turret, String type, String title, String desc1, String desc2) {
        ItemStack stack = new ItemStack(Items.PAPER);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("UpgradeType", type);
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(new TextComponent(title).withStyle(style -> style.withColor(0xFFCC33).withBold(true).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("■ 구매 즉시 이 터렛에 즉시 적용됩니다.").withStyle(style -> style.withColor(0x55FF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(desc1).withStyle(style -> style.withColor(0xFFFF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(desc2).withStyle(style -> style.withColor(0x55FFFF)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("허용 터렛 종류: " + turret.getType().getDescription().getString()).withStyle(style -> style.withColor(0xAAAAAA)))));

        display.put("Lore", lore);
        tag.put("display", display);

        return stack;
    }

    public static void applyUpgradeDirectly(BeeSummonerTurretEntity turret, String type) {
        Player player = turret.getTradingPlayer();

        if (type.equals("bee_duration")) {
            turret.setBeeDurationLevel(turret.getBeeDurationLevel() + 1);
            if (player != null) player.displayClientMessage(new TextComponent("벌 생체 유지 시스템 연마 완료! (강화도: " + turret.getBeeDurationLevel() + "단계)"), true);
        } else if (type.equals("bee_damage")) {
            turret.setBeeDamageLevel(turret.getBeeDamageLevel() + 1);
            if (player != null) player.displayClientMessage(new TextComponent("벌 물리 공격 무장 강화 완료! (강화도: " + turret.getBeeDamageLevel() + "단계)"), true);
        } else if (type.equals("range")) {
            turret.setRangeLevel(turret.getRangeLevel() + 1);
            if (player != null) player.displayClientMessage(new TextComponent("벌 레이더 조율 연마 완료! (정찰 범위: " + (16 + turret.getRangeLevel() * 4) + "m)"), true);
        } else if (type.equals("max_bees")) {
            if (turret.getMaxBees() < 15) {
                turret.setMaxBees(turret.getMaxBees() + 1);
                if (player != null) player.displayClientMessage(new TextComponent("벌 군락 소환 한도 개방 완료! (총합: " + turret.getMaxBees() + "마리)"), true);
            }
        } else if (type.equals("bee_slowness")) {
            if (turret.getBeeSlownessLevel() < 10) {
                turret.setBeeSlownessLevel(turret.getBeeSlownessLevel() + 1);
                if (player != null) player.displayClientMessage(new TextComponent("벌 슬로우 마비독 연마 완료! (슬로우 등급: " + turret.getBeeSlownessLevel() + "단계)"), true);
            }
        } else if (type.equals("bee_weakness")) {
            if (turret.getBeeWeaknessLevel() < 10) {
                turret.setBeeWeaknessLevel(turret.getBeeWeaknessLevel() + 1);
                if (player != null) player.displayClientMessage(new TextComponent("벌 피로 약화독 연마 완료! (약화 등급: " + turret.getBeeWeaknessLevel() + "단계)"), true);
            }
        } else if (type.equals("bee_poison")) {
            if (turret.getBeePoisonLevel() < 10) {
                turret.setBeePoisonLevel(turret.getBeePoisonLevel() + 1);
                if (player != null) player.displayClientMessage(new TextComponent("벌 일반 독성 개조 완료! (신경독 등급: " + turret.getBeePoisonLevel() + "단계)"), true);
            }
        } else if (type.equals("bee_wither")) {
            if (turret.getBeeWitherLevel() < 10) {
                turret.setBeeWitherLevel(turret.getBeeWitherLevel() + 1);
                if (player != null) player.displayClientMessage(new TextComponent("벌 위더 부식 침 개조 완료! (위더 등급: " + turret.getBeeWitherLevel() + "단계)"), true);
            }
        } else if (type.equals("bee_cooldown")) { // 🆕 소환 대기시간 감소 연동
            if (turret.getBeeCooldownLevel() < 4) {
                turret.setBeeCooldownLevel(turret.getBeeCooldownLevel() + 1);
                if (player != null) player.displayClientMessage(new TextComponent("벌 출격 활주로 제어 시스템 연마 완료! (대기시간: " + (30 - turret.getBeeCooldownLevel() * 5) + "초)"), true);
            }
        } else if (type.equals("bee_health")) { // 🆕 벌 생성 최대 체력 강화 연동
            turret.setBeeHealthLevel(turret.getBeeHealthLevel() + 1);
            if (player != null) player.displayClientMessage(new TextComponent("벌 소환 골격 내구성 연마 완료! (추가 체력: +" + (turret.getBeeHealthLevel() * 2) + " HP)"), true);
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 0.85F);
        turret.overrideOffers(null);
        if (player != null) {
            turret.openTradingScreen(player, turret.getDisplayName(), 1);
        }
    }
}
package changmin.myMod.feature.turret.healer;

import changmin.myMod.registry.ModItems;
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

public class HealerTradeManager {

    public static void populateOffers(HealerTurretEntity turret, MerchantOffers offers) {
        offers.clear();

        MerchantOffers tokenOffers = new MerchantOffers();
        MerchantOffers materialOffers = new MerchantOffers();

        // ==========================================
        // 🟢 하급 업그레이드 품목 (하급 토큰 사용)
        // ==========================================

        // 1. 쿨타임 단축
        int currentCooldownLvl = turret.getCooldownLevel();
        float currentSec = turret.getCalculatedCooldown() / 20.0F;
        if (currentCooldownLvl < 18) {
            float nextSec = Math.max(20, 200 - (currentCooldownLvl + 1) * 10) / 20.0F;
            ItemStack cooldownReceipt = createUpgradeReceipt(turret, "cooldown", "터렛 기술 인증서: 쿨타임 감소",
                    "■ 적용 즉시 해당 터렛의 성능이 강화됩니다.",
                    "효과: 치유 시전 대기시간이 0.5초 감소합니다. (최대 1초 대기시간까지 단축 가능)",
                    "현재 상태: " + currentSec + "초 ➔ " + nextSec + "초");
            materialOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 16), cooldownReceipt, 15, 2, 0.05F));

            // 🌟 일반 터렛과 동일하게 순수 토큰 스택을 요구하도록 변경
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.HEALER_POINT_TOKEN_LOW.get(), 1), cooldownReceipt, 15, 2, 0.05F));
        }

        // 2. 사거리 확장
        int currentRangeLvl = turret.getRangeLevel();
        ItemStack rangeReceipt = createUpgradeReceipt(turret, "range", "터렛 기술 인증서: 사거리 확장",
                "■ 적용 즉시 해당 터렛의 성능이 강화됩니다.",
                "효과: 대상을 인지하여 치유할 수 있는 작동 반경이 1블록 상승합니다.",
                "현재 상태: " + (8 + currentRangeLvl) + "블록 ➔ " + (8 + currentRangeLvl + 1) + "블록");
        materialOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 16), rangeReceipt, 15, 2, 0.05F));

        // 🌟 일반 터렛과 동일하게 순수 토큰 스택을 요구하도록 변경
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.HEALER_POINT_TOKEN_LOW.get(), 1), rangeReceipt, 15, 2, 0.05F));


        // ==========================================
        // 🟡 중급 업그레이드 품목 (중급 토큰 사용)
        // ==========================================

        // 3. 치유량 강화
        float currentHeal = turret.getHealAmount();
        ItemStack healReceipt = createUpgradeReceipt(turret, "heal_amount", "터렛 기술 인증서: 치유량 강화",
                "■ 적용 즉시 해당 터렛의 성능이 강화됩니다.",
                "효과: 1회당 대상 치유 수치가 하트 0.5개(+1 HP) 상승합니다.",
                "현재 상태: " + (currentHeal / 2.0F) + " 하트 ➔ " + ((currentHeal + 1.0F) / 2.0F) + " 하트");
        materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 16), healReceipt, 15, 2, 0.05F));

        // 🌟 일반 터렛과 동일하게 순수 토큰 스택을 요구하도록 변경
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.HEALER_POINT_TOKEN_MID.get(), 1), healReceipt, 15, 2, 0.05F));

        // 4. 과치유 보호막 해금
        ItemStack shieldReceipt = createUpgradeReceipt(turret, "shield", "터렛 연금 비서: 과치유 보호막",
                "■ 구매 즉시 영구 해금되는 일회성 특수 기술입니다.",
                "효과: 치유 시 대상의 최대 체력을 초과하여 오버치유된 분량을 노란색 보호막 하트(흡수 수치)로 누적 전환합니다.",
                "현재 상태: " + (turret.getShieldLevel() > 0 ? "★ 기능 활성화 완료" : "보호막 비활성화 ➔ 영구 보호막 기능 해금"));

        // 🌟 일반 터렛과 동일하게 순수 토큰 스택을 요구하도록 변경
        MerchantOffer shieldTokenOffer = new MerchantOffer(new ItemStack(ModItems.HEALER_POINT_TOKEN_MID.get(), 1), shieldReceipt, 1, 5, 0.05F);
        MerchantOffer shieldMaterialOffer = new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 16), shieldReceipt, 1, 5, 0.05F);

        if (turret.getShieldLevel() > 0) {
            shieldTokenOffer.setToOutOfStock();
            shieldMaterialOffer.setToOutOfStock();
        }
        tokenOffers.add(shieldTokenOffer);
        materialOffers.add(shieldMaterialOffer);


        // ==========================================
        // 🔴 상급 업그레이드 품목 (상급 토큰 사용)
        // ==========================================

        // 5. 광역 치유 비기 해금
        ItemStack aoeReceipt = createUpgradeReceipt(turret, "aoe_heal", "터렛 비기 전수: 광역의 대지",
                "■ 필요 터렛 레벨: 3레벨 이상 일회성 특수 기술입니다.",
                "효과: 범위 안의 모든 아군을 동시 치유합니다. (단, 치유량 -50%, 쿨타임 +50% 패널티 발생)",
                "현재 상태: " + (turret.getAoeHealEnabled() > 0 ? "★ 기능 활성화 완료" : "단일 정밀 치유 ➔ 광역 동시 치유"));

        // 🌟 일반 터렛과 동일하게 순수 토큰 스택을 요구하도록 변경
        MerchantOffer aoeTokenOffer = new MerchantOffer(new ItemStack(ModItems.HEALER_POINT_TOKEN_HIGH.get(), 1), aoeReceipt, 1, 5, 0.05F);
        MerchantOffer aoeMaterialOffer = new MerchantOffer(new ItemStack(Items.EMERALD, 20), aoeReceipt, 1, 5, 0.05F);

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
        ItemStack cleanseReceipt = createUpgradeReceipt(turret, "cleanse", "터렛 연금 비서: 정화의 원천",
                "■ 필요 터렛 레벨: 5레벨 이상 일회성 특수 기술입니다.",
                "효과: 치유 시 대상의 독, 위더, 감속, 나약함 등 핵심 해로운 상태이상을 즉시 소거합니다.",
                "현재 상태: " + (turret.getCleanseEnabled() > 0 ? "★ 기능 활성화 완료" : "기본 체력 회복 ➔ 정화 효과 도입"));

        // 🌟 일반 터렛과 동일하게 순수 토큰 스택을 요구하도록 변경
        MerchantOffer cleanseTokenOffer = new MerchantOffer(new ItemStack(ModItems.HEALER_POINT_TOKEN_HIGH.get(), 1), cleanseReceipt, 1, 5, 0.05F);
        MerchantOffer cleanseMaterialOffer = new MerchantOffer(new ItemStack(Items.EMERALD, 20), cleanseReceipt, 1, 5, 0.05F);

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
        Item tokenItem = ModItems.HEALER_POINT_TOKEN_LOW.get();
        String tierName = "하급";
        int nameColor = 0xFFD700; // 하급: 골드빛

        if (tier == 1) {
            tokenItem = ModItems.HEALER_POINT_TOKEN_MID.get();
            tierName = "중급";
            nameColor = 0x55FF55; // 중급: 녹색빛
        } else if (tier == 2) {
            tokenItem = ModItems.HEALER_POINT_TOKEN_HIGH.get();
            tierName = "상급";
            nameColor = 0xFF55FF; // 상급: 자줏빛
        }

        ItemStack token = new ItemStack(tokenItem, count);
        CompoundTag tag = token.getOrCreateTag();
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        // 🌟 일반 터렛과 완전히 동일한 이름/Lore 생성 방식으로 통일
        CompoundTag display = new CompoundTag();
        final String finalTierName = tierName;
        final int finalColor = nameColor;
        display.putString("Name", Component.Serializer.toJson(
                new TextComponent("치유 터렛 포인트 토큰 (" + finalTierName + ")")
                        .withStyle(style -> style.withColor(finalColor).withItalic(false))
        ));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(
                new TextComponent("허용 터렛 종류: " + turret.getType().getDescription().getString())
                        .withStyle(style -> style.withColor(0xAAAAAA))
        )));
        display.put("Lore", lore);
        tag.put("display", display);

        return token;
    }

    public static ItemStack createUpgradeReceipt(HealerTurretEntity turret, String type, String title, String desc1, String desc2, String currentLvlInfo) {
        ItemStack stack = new ItemStack(Items.PAPER);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("UpgradeType", type);
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(new TextComponent(title).withStyle(style -> style.withColor(0x55FFFF).withBold(true).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(desc1).withStyle(style -> style.withColor(0x55FF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(desc2).withStyle(style -> style.withColor(0xFFFF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(currentLvlInfo).withStyle(style -> style.withColor(0xFF55FF)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("허용 터렛 종류: " + turret.getType().getDescription().getString()).withStyle(style -> style.withColor(0xAAAAAA)))));

        display.put("Lore", lore);
        tag.put("display", display);

        return stack;
    }

    public static void applyUpgradeDirectly(HealerTurretEntity turret, String type) {
        Player tradingPlayer = turret.getTradingPlayer();

        if (type.equals("heal_amount")) {
            turret.setHealAmount(turret.getHealAmount() + 1.0F);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("치유 성능 업그레이드 완료! (현재 치유량: 하트 " + (turret.getHealAmount() / 2.0F) + "개)"), true);
            }
        } else if (type.equals("cooldown")) {
            if (turret.getCooldownLevel() < 18) {
                turret.setCooldownLevel(turret.getCooldownLevel() + 1);
                if (tradingPlayer != null) {
                    float sec = turret.getCalculatedCooldown() / 20.0F;
                    tradingPlayer.displayClientMessage(new TextComponent("치유 대기시간 단축 완료! (현재 대기시간: " + sec + "초)"), true);
                }
            }
        } else if (type.equals("range")) {
            turret.setRangeLevel(turret.getRangeLevel() + 1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("치유 작동 범위 확장 완료! (현재 반경: " + (8 + turret.getRangeLevel()) + "블록)"), true);
            }
        } else if (type.equals("aoe_heal")) {
            turret.setAoeHealEnabled(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("비기 습득 완료: 광역 치유 파동을 방출합니다! (힐량 50% 반감, 쿨타임 50% 증가 패널티 적용)"), true);
            }
        } else if (type.equals("cleanse")) {
            turret.setCleanseEnabled(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("정화 마법 적용 완료: 아군의 해로운 약물 및 질병 디버프를 강제 정화합니다!"), true);
            }
        } else if (type.equals("shield")) {
            turret.setShieldLevel(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("과치유 보호막 해금 완료! 초과 치유량이 흡수 하트(보호막)로 영구 누적됩니다."), true);
            }
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 1.0F);

        turret.overrideOffers(null);
        if (tradingPlayer != null) {
            turret.openTradingScreen(tradingPlayer, turret.getDisplayName(), turret.getVillagerXp());
        }
    }
}
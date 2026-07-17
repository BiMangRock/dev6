package changmin.myMod.feature.turret.healer;

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

public class HealerTradeManager {

    public static void populateOffers(HealerTurretEntity turret, MerchantOffers offers) {
        offers.clear();

        MerchantOffers tokenOffers = new MerchantOffers();
        MerchantOffers emeraldOffers = new MerchantOffers();

        String regName = turret.getType().getRegistryName().toString();

        // 1. 치유량 강화 (상한선 제한 없음)
        float currentHeal = turret.getHealAmount();
        ItemStack healReceipt = createUpgradeReceipt(turret, "heal_amount", "터렛 기술 인증서: 치유량 강화",
                "■ 적용 즉시 해당 터렛의 성능이 강화됩니다.",
                "효과: 1회당 대상 치유 수치가 하트 0.5개(+1 HP) 상승합니다.",
                "현재 상태: " + (currentHeal / 2.0F) + " 하트 ➔ " + ((currentHeal + 1.0F) / 2.0F) + " 하트");
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), healReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(getBoundToken(turret, 1), healReceipt, 15, 2, 0.05F));

        // 2. 쿨타임 단축 (최대 18레벨 제한 = 1초 쿨타임 도달)
        int currentCooldownLvl = turret.getCooldownLevel();
        float currentSec = turret.getCalculatedCooldown() / 20.0F;
        if (currentCooldownLvl < 18) {
            float nextSec = Math.max(20, 200 - (currentCooldownLvl + 1) * 10) / 20.0F;
            ItemStack cooldownReceipt = createUpgradeReceipt(turret, "cooldown", "터렛 기술 인증서: 쿨타임 감소",
                    "■ 적용 즉시 해당 터렛의 성능이 강화됩니다.",
                    "효과: 치유 시전 대기시간이 0.5초 감소합니다. (최대 1초 대기시간까지 단축 가능)",
                    "현재 상태: " + currentSec + "초 ➔ " + nextSec + "초");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 24), cooldownReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(getBoundToken(turret, 1), cooldownReceipt, 15, 2, 0.05F));
        }

        // 3. 치유 사거리 확장
        int currentRangeLvl = turret.getRangeLevel();
        ItemStack rangeReceipt = createUpgradeReceipt(turret, "range", "터렛 기술 인증서: 사거리 확장",
                "■ 적용 즉시 해당 터렛의 성능이 강화됩니다.",
                "효과: 대상을 인지하여 치유할 수 있는 작동 반경이 1블록 상승합니다.",
                "현재 상태: " + (8 + currentRangeLvl) + "블록 ➔ " + (8 + currentRangeLvl + 1) + "블록");
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 12), rangeReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(getBoundToken(turret, 1), rangeReceipt, 15, 2, 0.05F));

        // 4. [특수] 광역 치유 대지 변환 (레벨 3 이상 해금 가능)
        if (turret.getAoeHealEnabled() == 0) {
            ItemStack aoeReceipt = createUpgradeReceipt(turret, "aoe_heal", "터렛 비기 전수: 광역의 대지",
                    "■ 필요 터렛 레벨: 3레벨 이상 전용 개조 항목",
                    "효과: 단일 대상 치유에서 범위 안 부상당한 모든 아군을 동시 치유하도록 전환합니다.",
                    "현재 상태: 단일 정밀 치유 ➔ 광역 동시 치유 (요구 토큰: 3개)");
            MerchantOffer aoeOffer = new MerchantOffer(getBoundToken(turret, 3), aoeReceipt, 1, 5, 0.05F);
            if (turret.getTurretLevel() < 3) {
                aoeOffer.setToOutOfStock(); // 레벨 부족 시 거래 잠금
            }
            tokenOffers.add(aoeOffer);
        }

        // 5. [특수] 상태이상 정화 능력 전수 (레벨 5 이상 해금 가능)
        if (turret.getCleanseEnabled() == 0) {
            ItemStack cleanseReceipt = createUpgradeReceipt(turret, "cleanse", "터렛 연금 비서: 정화의 원천",
                    "■ 필요 터렛 레벨: 5레벨 이상 전용 개조 항목",
                    "효과: 치유 시 대상의 독, 위더, 감속, 나약함 등 핵심 해로운 상태이상을 즉시 소거합니다.",
                    "현재 상태: 기본 체력 회복 ➔ 정화 효과 도입 (요구 토큰: 5개)");
            MerchantOffer cleanseOffer = new MerchantOffer(getBoundToken(turret, 5), cleanseReceipt, 1, 5, 0.05F);
            if (turret.getTurretLevel() < 5) {
                cleanseOffer.setToOutOfStock(); // 레벨 부족 시 거래 잠금
            }
            tokenOffers.add(cleanseOffer);
        }

        offers.addAll(tokenOffers);
        offers.addAll(emeraldOffers);
    }

    public static ItemStack getBoundToken(HealerTurretEntity turret, int count) {
        ItemStack token = new ItemStack(ModItems.TURRET_POINT_TOKEN_LOW.get(), count);
        CompoundTag tag = token.getOrCreateTag();
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(new TextComponent("터렛 포인트 토큰 (치유)").withStyle(style -> style.withColor(0x55FF55).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("허용 터렛 종류: " + turret.getDisplayName().getString()).withStyle(style -> style.withColor(0xAAAAAA)))));
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
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("허용 터렛 종류: " + turret.getDisplayName().getString()).withStyle(style -> style.withColor(0xAAAAAA)))));

        display.put("Lore", lore);
        tag.put("display", display);

        return stack;
    }

    public static void applyUpgradeDirectly(HealerTurretEntity turret, String type) {
        Player tradingPlayer = turret.getTradingPlayer();

        if (type.equals("heal_amount")) {
            turret.setHealAmount(turret.getHealAmount() + 1.0F); // 치유량 +0.5칸 강화
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
                tradingPlayer.displayClientMessage(new TextComponent("비기 습득 완료: 이제 주변의 모든 다친 아군을 일괄 치유합니다!"), true);
            }
        } else if (type.equals("cleanse")) {
            turret.setCleanseEnabled(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("정화 마법 적용 완료: 아군의 해로운 약물 및 질병 디버프를 강제 정화합니다!"), true);
            }
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 1.0F);

        turret.overrideOffers(null);
        if (tradingPlayer != null) {
            turret.openTradingScreen(tradingPlayer, turret.getDisplayName(), turret.getVillagerXp());
        }
    }
}
package changmin.myMod.feature.turret.tanker;

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

public class TankerTurretTradeManager {

    public static void populateOffers(TankerTurretEntity turret, MerchantOffers offers) {
        offers.clear();

        MerchantOffers tokenOffers = new MerchantOffers();
        MerchantOffers emeraldOffers = new MerchantOffers();

        // 🛡️ 1. 도발 지속 시간 강화 (하급 토큰 소요)
        int durationLvl = turret.getTauntDurationLevel();
        ItemStack durationReceipt = createUpgradeReceipt(turret, "duration", "탱커 특성: 도발 주파수 증폭",
                "효과: 도발 스킬의 어그로 유지 시간이 1초씩 늘어납니다.",
                "현재 상태: " + (4 + durationLvl) + "초 ➔ " + (5 + durationLvl) + "초");
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), durationReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_LOW.get(), 1), durationReceipt, 15, 2, 0.05F));

        // 🛡️ 2. 도발 재장전 쿨타임 단축 (하급 토큰 소요)
        int rechargeLvl = turret.getRechargeLevel();
        ItemStack rechargeReceipt = createUpgradeReceipt(turret, "recharge", "탱커 특성: 신속 재도발",
                "효과: 도발을 다시 시전하는 대기 쿨타임이 0.5초 단축됩니다.",
                "현재 쿨타임: " + (10.0F - rechargeLvl * 0.5F) + "초 ➔ " + (9.5F - rechargeLvl * 0.5F) + "초 (최소 5초)");
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 12), rechargeReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_LOW.get(), 1), rechargeReceipt, 15, 2, 0.05F));

        // 🛡️ 3. 가시 반사 데미지 연마 (중급 토큰 소요)
        int thornsLvl = turret.getThornsLevel();
        ItemStack thornsReceipt = createUpgradeReceipt(turret, "thorns", "탱커 특성: 보복용 가시 갑옷",
                "효과: 타격받을 때마다 공격한 적에게 현재 체력에 비례한 가시 반사 피해를 줍니다.",
                "현재 강도: 현재 체력의 " + (2 + thornsLvl) + "% 반사 ➔ " + (3 + thornsLvl) + "% 반사");
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 24), thornsReceipt, 10, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 1), thornsReceipt, 10, 2, 0.05F));

        // 🛡️ 4. 도발 중 피해 감쇄 업그레이드 (최대 12단계 / 70% 제한 적용, 상급 토큰 소요)
        int reductionLvl = turret.getReductionLevel();
        if (reductionLvl < 12) {
            ItemStack reductionReceipt = createUpgradeReceipt(turret, "reduction", "탱커 전설: 강철 방어 태세",
                    "효과: 도발 중 전개되는 본체의 상시 물리 피해 감쇄율이 5%씩 늘어납니다.",
                    "현재 감쇄율: " + (10 + reductionLvl * 5) + "% ➔ " + (15 + reductionLvl * 5) + "% (최대 70%)");

            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 32), reductionReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN_HIGH.get(), 1), reductionReceipt, 15, 2, 0.05F));
        } else {
            ItemStack maxReceipt = createUpgradeReceipt(turret, "reduction", "[최대 한계] 강철 방어 태세",
                    "효과: 밸런스 상한선인 70% 물리 피해 감쇄에 도달했습니다.",
                    "더 이상 강화할 수 없습니다.");
            MerchantOffer maxOffer = new MerchantOffer(new ItemStack(Items.EMERALD, 64), maxReceipt, 15, 2, 0.05F);
            maxOffer.setToOutOfStock();
            emeraldOffers.add(maxOffer);
        }

        offers.addAll(tokenOffers);
        offers.addAll(emeraldOffers);
    }



    public static ItemStack getBoundToken(TankerTurretEntity turret, int count) {
        ItemStack token = new ItemStack(ModItems.TURRET_POINT_TOKEN_LOW.get(), count);
        CompoundTag tag = token.getOrCreateTag();
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(new TextComponent("탱커 터렛 포인트 토큰 (하급)").withStyle(style -> style.withColor(0xAAAAAA).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("허용 터렛 종류: " + turret.getType().getDescription().getString()).withStyle(style -> style.withColor(0xAAAAAA)))));
        display.put("Lore", lore);
        tag.put("display", display);

        return token;
    }

    private static ItemStack createUpgradeReceipt(TankerTurretEntity turret, String type, String title, String desc1, String desc2) {
        ItemStack stack = new ItemStack(Items.PAPER);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("UpgradeType", type);
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(new TextComponent(title).withStyle(style -> style.withColor(0xFF8822).withBold(true).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("■ 구매 즉시 이 터렛에 즉시 적용됩니다.").withStyle(style -> style.withColor(0x55FF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(desc1).withStyle(style -> style.withColor(0xFFFF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(desc2).withStyle(style -> style.withColor(0x55FFFF)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("허용 터렛 종류: " + turret.getType().getDescription().getString()).withStyle(style -> style.withColor(0xAAAAAA)))));

        display.put("Lore", lore);
        tag.put("display", display);

        return stack;
    }

    public static void applyUpgradeDirectly(TankerTurretEntity turret, String type) {
        Player player = turret.getTradingPlayer();

        if (type.equals("duration")) {
            turret.setTauntDurationLevel(turret.getTauntDurationLevel() + 1);
            if (player != null) player.displayClientMessage(new TextComponent("도발 주파수 증폭 완료! (도발 시간: " + (4 + turret.getTauntDurationLevel()) + "초)"), true);
        } else if (type.equals("recharge")) {
            turret.setRechargeLevel(turret.getRechargeLevel() + 1);
            if (player != null) player.displayClientMessage(new TextComponent("신속 재도발 개조 완료!"), true);
        } else if (type.equals("thorns")) {
            turret.setThornsLevel(turret.getThornsLevel() + 1);
            if (player != null) player.displayClientMessage(new TextComponent("보복용 가시 갑옷 장착 완료! (반사강도: 현재 체력의 " + (2 + turret.getThornsLevel()) + "%)"), true);
        } else if (type.equals("reduction")) {
            if (turret.getReductionLevel() < 12) {
                turret.setReductionLevel(turret.getReductionLevel() + 1);
                int finalRed = 10 + turret.getReductionLevel() * 5;
                if (player != null) player.displayClientMessage(new TextComponent("강철 방어 태세 연마 완료! (도발 중 피해 감쇄: " + finalRed + "%)"), true);
            }
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 0.85F);
        turret.overrideOffers(null);
        if (player != null) {
            turret.openTradingScreen(player, turret.getDisplayName(), 1);
        }
    }
}
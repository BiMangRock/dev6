package changmin.changmin_villager_turret.feature.turret.trident_turret;

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
            ItemStack toggleReceipt = createUpgradeReceipt(turret, "toggle_pattern", "삼지창 특성: 부채꼴 사격 전환",
                    "효과: 삼지창을 정방 60도 범위로 넓고 촘촘하게 분산하여 발사합니다.",
                    "현재 상태: [집중 사격] ➔ [부채꼴 사격] 전환");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), toggleReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_LOW.get(), 1), toggleReceipt, 15, 2, 0.05F));
        } else {
            ItemStack toggleReceipt = createUpgradeReceipt(turret, "toggle_pattern", "삼지창 특성: 집중 사격 전환",
                    "효과: 모든 삼지창을 한 방향 표적에게 좁게 집중하여 연속 포격합니다.",
                    "현재 상태: [부채꼴 사격] ➔ [집중 사격] 전환");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), toggleReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_LOW.get(), 1), toggleReceipt, 15, 2, 0.05F));
        }

        // 🔱 1. 삼지창 발사 수 (무제한)
        int currentCount = turret.getTridentCount();
        ItemStack receipt = createUpgradeReceipt(turret, "count", "삼지창 특성: 다중 투척",
                "효과: 동시에 발사되는 삼지창의 개수가 늘어납니다.",
                "현재 투척 수: " + currentCount + "개 ➔ " + (currentCount + 1) + "개 (개수 제한 없음!)");

        int emeraldCost = Math.min(64, 16 + currentCount * 4);
        int tokenCost = Math.min(10, 1 + currentCount / 2);
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, emeraldCost), receipt, 10, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_MID.get(), tokenCost), receipt, 10, 2, 0.05F));

        // 🔱 2. 공격력 강화 (날카로움)
        int dmgLvl = turret.getDamageLevel();
        ItemStack dmgReceipt = createUpgradeReceipt(turret, "damage", "삼지창 특성: 물리 정밀 연마",
                "효과: 삼지창 한 발당 타격 위력이 비약적으로 늘어납니다.",
                "현재 강화 수준: +" + dmgLvl + "레벨 ➔ +" + (dmgLvl + 1) + "레벨");
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), dmgReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_LOW.get(), 1), dmgReceipt, 15, 2, 0.05F));

        // 🔱 3. 인식 사거리 강화
        int rangeLvl = turret.getRangeLevel();
        ItemStack rangeReceipt = createUpgradeReceipt(turret, "range", "삼지창 특성: 원거리 감지 조율",
                "효과: 몬스터 인식 사거리 및 삼지창의 도달 속도가 증대됩니다.",
                "현재 강화 수준: " + (16 + rangeLvl * 4) + "m ➔ " + (16 + (rangeLvl + 1) * 4) + "m");
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 12), rangeReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_LOW.get(), 1), rangeReceipt, 15, 2, 0.05F));

        // 🔱 4. 재장전 속도 단축
        int rechargeLvl = turret.getRechargeLevel();
        ItemStack rechargeReceipt = createUpgradeReceipt(turret, "recharge", "삼지창 특성: 신속 조준",
                "효과: 삼지창을 던진 뒤 다음 투척까지 걸리는 재대기 시간이 빨라집니다.",
                "현재 속도 강화: +" + rechargeLvl + "레벨 ➔ +" + (rechargeLvl + 1) + "레벨");
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), rechargeReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_LOW.get(), 1), rechargeReceipt, 15, 2, 0.05F));

        // 🔱 5. 집뢰(벼락) 기술 무장 개조
        if (turret.getLightningLevel() == 0) {
            ItemStack lightningReceipt = createUpgradeReceipt(turret, "lightning", "삼지창 전설 기술: 무조건적 집뢰",
                    "효과: 날씨에 무관하게 삼지창에 적중된 몹에게 강력한 벼락을 소환합니다.",
                    "현재 상태: [미적용] ➔ [집뢰 무장 탑재]");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 48), lightningReceipt, 1, 10, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_HIGH.get(), 1), lightningReceipt, 1, 10, 0.05F));
        }

        // ==========================================
        // 🔱 6. [신규 추가] 해류 관통 개조 (최대 10레벨 제한, 상급 토큰 소요)
        // ==========================================
        int pierceLvl = turret.getPierceLevel();
        if (pierceLvl < 10) {
            ItemStack pierceReceipt = createUpgradeReceipt(turret, "pierce", "삼지창 전설 기술: 해류 관통 개조",
                    "효과: 던진 삼지창이 적들을 관통하여 지나가며 일직선상의 모든 적을 일타이피로 타격합니다.",
                    "현재 관통도: " + pierceLvl + "마리 관통 ➔ " + (pierceLvl + 1) + "마리 관통 (최대 10마리)");

            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 32), pierceReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TRIDENT_POINT_TOKEN_HIGH.get(), 1), pierceReceipt, 15, 2, 0.05F));
        }

        offers.addAll(tokenOffers);
        offers.addAll(emeraldOffers);
    }

    public static ItemStack getBoundToken(TridentTurretEntity turret, int count) {
        ItemStack token = new ItemStack(ModItems.TRIDENT_POINT_TOKEN_LOW.get(), count);
        CompoundTag tag = token.getOrCreateTag();
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(new TextComponent("삼지창 터렛 포인트 토큰 (하급)").withStyle(style -> style.withColor(0x33CCFF).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("허용 터렛 종류: " + turret.getType().getDescription().getString()).withStyle(style -> style.withColor(0xAAAAAA)))));
        display.put("Lore", lore);
        tag.put("display", display);

        return token;
    }

    private static ItemStack createUpgradeReceipt(TridentTurretEntity turret, String type, String title, String desc1, String desc2) {
        ItemStack stack = new ItemStack(Items.PAPER);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("UpgradeType", type);
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(new TextComponent(title).withStyle(style -> style.withColor(0x33CCFF).withBold(true).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("■ 구매 즉시 이 터렛에 즉시 적용됩니다.").withStyle(style -> style.withColor(0x55FF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(desc1).withStyle(style -> style.withColor(0xFFFF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(desc2).withStyle(style -> style.withColor(0x55FFFF)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("허용 터렛 종류: " + turret.getType().getDescription().getString()).withStyle(style -> style.withColor(0xAAAAAA)))));

        display.put("Lore", lore);
        tag.put("display", display);

        return stack;
    }

    public static void applyUpgradeDirectly(TridentTurretEntity turret, String type) {
        Player player = turret.getTradingPlayer();

        if (type.equals("count")) {
            turret.setTridentCount(turret.getTridentCount() + 1);
            if (player != null) player.displayClientMessage(new TextComponent("삼지창 다중 투척 개조 완료! (발사량: " + turret.getTridentCount() + "개)"), true);
        } else if (type.equals("damage")) {
            turret.setDamageLevel(turret.getDamageLevel() + 1);
            if (player != null) player.displayClientMessage(new TextComponent("물리 관통 데미지 연마 완료! (강화도: " + turret.getDamageLevel() + "단계)"), true);
        } else if (type.equals("range")) {
            turret.setRangeLevel(turret.getRangeLevel() + 1);
            if (player != null) player.displayClientMessage(new TextComponent("원거리 적 조준 범위 조율 완료! (탐지 사거리: " + (16 + turret.getRangeLevel() * 4) + "m)"), true);
        } else if (type.equals("recharge")) {
            turret.setRechargeLevel(turret.getRechargeLevel() + 1);
            if (player != null) player.displayClientMessage(new TextComponent("조준 메커니즘 쿨타임 단축 완료!"), true);
        } else if (type.equals("lightning")) {
            turret.setLightningLevel(1);
            if (player != null) player.displayClientMessage(new TextComponent("삼지창에 집뢰 기술 무장이 탑재되었습니다!"), true);
        } else if (type.equals("toggle_pattern")) {
            int newMode = turret.getShootMode() == 0 ? 1 : 0;
            turret.setShootMode(newMode);
            String modeName = newMode == 0 ? "집중 사격 모드" : "부채꼴 사격 모드";
            if (player != null) player.displayClientMessage(new TextComponent("사격 패턴 변경 완료! (현재: " + modeName + ")"), true);
        } else if (type.equals("pierce")) {
            // 🆕 관통 레벨 업그레이드 수령 처리 (최대 10마리 제한 적용)
            if (turret.getPierceLevel() < 10) {
                turret.setPierceLevel(turret.getPierceLevel() + 1);
                if (player != null) player.displayClientMessage(new TextComponent("삼지창 해류 관통 개조 완료! (관통수: " + turret.getPierceLevel() + "마리)"), true);
            }
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 0.85F);
        turret.overrideOffers(null);
        if (player != null) {
            turret.openTradingScreen(player, turret.getDisplayName(), 1);
        }
    }
}
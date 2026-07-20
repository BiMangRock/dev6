package changmin.myMod.feature.turret.lightning_wizard;

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
        ItemStack stunReceipt = createUpgradeReceipt(turret, "stun", "마법사 특성: 전격 주파수 마비",
                "효과: 평타 공격 명중 시 적의 기절 정지 시간이 0.5초 연장됩니다.",
                "현재 강도: " + (2.0F + stunLvl * 0.5F) + "초 ➔ " + (2.5F + stunLvl * 0.5F) + "초");
        // 🌟 하급에 맞추어 철 주괴 1개 요구로 변경
        materialOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), stunReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_LOW.get(), 1), stunReceipt, 15, 2, 0.05F));

        // ⚡ 2. 평타 적중 시 쿨감 누적 가중치 가속화 (번개 전용 하급 토큰 소요)
        int reductionLvl = turret.getCooldownReductionLevel();
        ItemStack reductionReceipt = createUpgradeReceipt(turret, "reduction", "마법사 특성: 역류 정전기 축전",
                "효과: 평타 명중 성공 시 단축되는 궁극기 쿨타임이 1초씩 더 늘어납니다.",
                "현재 충전 효율: 타격당 -" + (3 + reductionLvl) + "초 ➔ -" + (4 + reductionLvl) + "초");
        // 🌟 하급에 맞추어 철 주괴 1개 요구로 변경
        materialOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), reductionReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_LOW.get(), 1), reductionReceipt, 15, 2, 0.05F));


        // ==========================================
        // 🟡 중급 업그레이드 품목 (중급 토큰 및 금 주괴 1개 사용)
        // ==========================================

        // ⚡ 3. 궁극기 폭격 번개 고유 피해 증대 (번개 전용 중급 토큰 소요)
        int dmgLvl = turret.getUltDamageLevel();
        ItemStack dmgReceipt = createUpgradeReceipt(turret, "damage", "마법사 특성: 과전류 전자기 융해",
                "효과: 영역 내에 떨어지는 번개 한 발당 타격 데미지가 비약적으로 상승합니다.",
                "현재 전격 공격력: " + (5.0D + dmgLvl * 2.5D) + " ➔ " + (7.5D + dmgLvl * 2.5D));
        // 🌟 중급에 맞추어 금 주괴 1개 요구로 변경
        materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), dmgReceipt, 10, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_MID.get(), 1), dmgReceipt, 10, 2, 0.05F));


        // ==========================================
        // 🔴 상급 업그레이드 품목 (상급 토큰 및 에메랄드 1개 사용)
        // ==========================================

        // ⚡ 4. 궁극기 뇌우 지속 폭격 시간 증폭 (최대 10단계 제한 / 번개 전용 상급 토큰 소요)
        int durationLvl = turret.getUltDurationLevel();
        if (durationLvl < 7) { // 3초 + 7단계 강화 = 최대 10초 도달 한계
            ItemStack durationReceipt = createUpgradeReceipt(turret, "duration", "마법사 전설: 폭우 구름 장막 유지",
                    "효과: 시전된 궁극기 범위 내의 벼락 폭격 지속 시간이 1초 늘어납니다.",
                    "현재 지속시간: " + (3 + durationLvl) + "초 ➔ " + (4 + durationLvl) + "초 (최대 10초)");

            // 🌟 상급에 맞추어 에메랄드 1개 요구로 변경
            materialOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), durationReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_HIGH.get(), 1), durationReceipt, 15, 2, 0.05F));
        } else {
            ItemStack maxReceipt = createUpgradeReceipt(turret, "duration", "[최대한계] 뇌우 구름 장막 유지",
                    "효과: 밸런스 상한선인 10초 폭격 지속시간에 도달했습니다.",
                    "더 이상 강화할 수 없습니다.");
            MerchantOffer maxOffer = new MerchantOffer(new ItemStack(Items.EMERALD, 1), maxReceipt, 15, 2, 0.05F);
            maxOffer.setToOutOfStock();
            materialOffers.add(maxOffer);
        }

        // ⚡ 5. 궁극기 벼락 범위 확장 (최대 10x10 제한 / 번개 전용 상급 토큰 소요)
        int rangeLvl = turret.getUltRangeLevel();
        if (rangeLvl < 6) { // 기본 4블록 반경 + 6단계 강화 = 최대 10블록 반경(20x20)
            ItemStack rangeReceipt = createUpgradeReceipt(turret, "range", "마법사 전설: 자성 영역 가오",
                    "효과: 궁극기가 타격하는 벼락 장판의 폭격 원형 범위가 확장됩니다.",
                    "현재 반경 범위: " + (4.0D + rangeLvl) + "m ➔ " + (5.0D + rangeLvl) + "m (최대 반경 10m)");

            // 🌟 상급에 맞추어 에메랄드 1개 요구로 변경
            materialOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), rangeReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_HIGH.get(), 1), rangeReceipt, 15, 2, 0.05F));
        }

        offers.addAll(tokenOffers);
        offers.addAll(materialOffers);
    }

    public static ItemStack getBoundToken(LightningWizardEntity turret, int count) {
        ItemStack token = new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_LOW.get(), count);
        CompoundTag tag = token.getOrCreateTag();
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(new TextComponent("번개 마법사 터렛 포인트 토큰 (하급)").withStyle(style -> style.withColor(0x7E00FF).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("허용 터렛 종류: " + turret.getType().getDescription().getString()).withStyle(style -> style.withColor(0xAAAAAA)))));
        display.put("Lore", lore);
        tag.put("display", display);

        return token;
    }

    private static ItemStack createUpgradeReceipt(LightningWizardEntity turret, String type, String title, String desc1, String desc2) {
        ItemStack stack = new ItemStack(Items.PAPER);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("UpgradeType", type);
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(new TextComponent(title).withStyle(style -> style.withColor(0x00E1FF).withBold(true).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("■ 구매 즉시 이 터렛에 즉시 적용됩니다.").withStyle(style -> style.withColor(0x55FF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(desc1).withStyle(style -> style.withColor(0xFFFF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(desc2).withStyle(style -> style.withColor(0x55FFFF)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("허용 터렛 종류: " + turret.getType().getDescription().getString()).withStyle(style -> style.withColor(0xAAAAAA)))));

        display.put("Lore", lore);
        tag.put("display", display);

        return stack;
    }

    public static void applyUpgradeDirectly(LightningWizardEntity turret, String type) {
        Player player = turret.getTradingPlayer();

        if (type.equals("stun")) {
            turret.setStunDurationLevel(turret.getStunDurationLevel() + 1);
            if (player != null) player.displayClientMessage(new TextComponent("전격 주파수 마비 연마 완료! (스턴 시간: " + (2.0F + turret.getStunDurationLevel() * 0.5F) + "초)"), true);
        } else if (type.equals("reduction")) {
            turret.setCooldownReductionLevel(turret.getCooldownReductionLevel() + 1);
            if (player != null) player.displayClientMessage(new TextComponent("역류 정전기 축전 충전율 상향 완료!"), true);
        } else if (type.equals("damage")) {
            turret.setUltDamageLevel(turret.getUltDamageLevel() + 1);
            if (player != null) player.displayClientMessage(new TextComponent("벼락 피해 관통 개조 완료! (번개 파괴력: " + (5.0D + turret.getUltDamageLevel() * 2.5D) + ")"), true);
        } else if (type.equals("duration")) {
            if (turret.getUltDurationLevel() < 7) {
                turret.setUltDurationLevel(turret.getUltDurationLevel() + 1);
                if (player != null) player.displayClientMessage(new TextComponent("구름 장막 연장 완료! (궁극기 시간: " + (3 + turret.getUltDurationLevel()) + "초)"), true);
            }
        } else if (type.equals("range")) {
            if (turret.getUltRangeLevel() < 6) {
                turret.setUltRangeLevel(turret.getUltRangeLevel() + 1);
                if (player != null) player.displayClientMessage(new TextComponent("벼락 폭격 반사 영역 확장 완료! (자기장 반경: " + (4.0D + turret.getUltRangeLevel()) + "m)"), true);
            }
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 0.85F);
        turret.overrideOffers(null);
        if (player != null) {
            turret.openTradingScreen(player, turret.getDisplayName(), 1);
        }
    }
}
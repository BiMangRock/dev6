package changmin.myMod.feature.turret.villager_turret;

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

public class TurretTradeManager {

    public static void populateOffers(VillagerTurretEntity turret, MerchantOffers offers) {
        offers.clear();

        MerchantOffers tokenOffers = new MerchantOffers();
        MerchantOffers emeraldOffers = new MerchantOffers();

        // 1. 재장전 속도
        ItemStack rechargeReceipt = createUpgradeReceipt(turret, "recharge", "터렛 강화 인증서: 재장전 속도",
                "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                "효과: 화살 발사 대기시간(쿨타임)이 0.25초 감소합니다.",
                "현재 강화 수준: " + turret.getRechargeLevel() + " ➔ " + (turret.getRechargeLevel() + 1));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), rechargeReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), rechargeReceipt, 15, 2, 0.05F));

        // 2. 화살 비행 속도
        ItemStack speedReceipt = createUpgradeReceipt(turret, "speed", "터렛 강화 인증서: 화살 비행 속도",
                "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                "효과: 화살의 날아가는 속도가 더 신속하게 증가합니다.",
                "현재 강화 수준: " + turret.getSpeedLevel() + " ➔ " + (turret.getSpeedLevel() + 1));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 12), speedReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), speedReceipt, 15, 2, 0.05F));

        // 3. 피격 후 무적 시간
        int currentInvuln = turret.getInvulnerabilityLevel();
        ItemStack invulnReceipt = createUpgradeReceipt(turret, "invuln", "터렛 방어 인증서: 피격 무적 증가",
                "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                "효과: 몹에게 공격받았을 때 발생하는 무적 시간이 1초 늘어납니다.",
                "현재 무적 수준: " + currentInvuln + "초 ➔ " + (currentInvuln + 1) + "초 (필요 토큰: 1개)");
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), invulnReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), invulnReceipt, 15, 2, 0.05F));

        // 4. 자가 회복력
        int currentHeal = turret.getHealLevel();
        ItemStack healReceipt = createUpgradeReceipt(turret, "heal", "터렛 방어 인증서: 자가 회복력 강화",
                "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                "효과: 5초마다 체력 1씩 회복 및 레벨업 시 최대 체력 비례 즉시 회복을 강화합니다.",
                "현재 회복 수준: +" + currentHeal + " ➔ +" + (currentHeal + 1) + " (필요 토큰: 1개)");
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 12), healReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), healReceipt, 15, 2, 0.05F));

        // 5. 경험치 50% 할인
        int currentNeededXp = turret.getNeededXp();
        int discountedXp = (currentNeededXp + 1) / 2;
        ItemStack xpDiscountReceipt = createUpgradeReceipt(turret, "xp_discount", "터렛 학습 인증서: 속성 훈련(50% 할인)",
                "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                "효과: 레벨업에 필요한 총 요구 경험치가 절반(소수점 올림)으로 감소합니다.",
                "현재 요구 경험치: " + currentNeededXp + " ➔ " + discountedXp + " (필요 토큰: 1개)");
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), xpDiscountReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), xpDiscountReceipt, 15, 2, 0.05F));

        // ==========================================
        // 🏹 6. 화살 발사 개수 강화 품목 및 상한선(10개) 잠금 장치
        // ==========================================
        int currentArrowCount = turret.getTurretArrowCount(); // 고유 메소드로 변경
        ItemStack arrowCountReceipt;
        MerchantOffer arrowCountEmeraldOffer;
        MerchantOffer arrowCountTokenOffer;

        if (currentArrowCount < 10) {
            arrowCountReceipt = createUpgradeReceipt(turret, "arrow_count", "터렛 기술 인증서: 화살 발사 개수",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 한 번에 격발되는 총 화살 수가 1개 증가합니다.",
                    "현재 사격 수: " + currentArrowCount + "개 ➔ " + (currentArrowCount + 1) + "개 (최대 10개)");

            arrowCountEmeraldOffer = new MerchantOffer(new ItemStack(Items.EMERALD, 24), arrowCountReceipt, 15, 2, 0.05F);
            arrowCountTokenOffer = new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 2), arrowCountReceipt, 15, 2, 0.05F);
        } else {
            // 상한선인 10개 달성 시 '품절 및 거래 불가 비활성화' 잠금 처리 진행
            arrowCountReceipt = createUpgradeReceipt(turret, "arrow_count", "[최대 강화 완료] 터렛 기술 인증서",
                    "■ 터렛이 한계 용량까지 개조되었습니다.",
                    "효과: 최대 격발 수 한계 도달 (10개)",
                    "현재 사격 수: 10개 (더 이상 업그레이드할 수 없습니다)");

            arrowCountEmeraldOffer = new MerchantOffer(new ItemStack(Items.EMERALD, 64), arrowCountReceipt, 15, 2, 0.05F);
            arrowCountEmeraldOffer.setToOutOfStock(); // setToOutOfStock 적용 완료

            arrowCountTokenOffer = new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 8), arrowCountReceipt, 15, 2, 0.05F);
            arrowCountTokenOffer.setToOutOfStock(); // setToOutOfStock 적용 완료
        }
        emeraldOffers.add(arrowCountEmeraldOffer);
        tokenOffers.add(arrowCountTokenOffer);

        // ==========================================
        // ⚙️ 7. 발사 형태 5개 모드로 확장 개조
        // ==========================================
        if (turret.getArrowPattern() != 0) {
            ItemStack normalPatternReceipt = createUpgradeReceipt(turret, "normal_pattern", "터렛 기술 인증서: 단발 패턴 복원",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 정밀하게 조준된 안전한 단발 사격 형태로 모드를 전환합니다.",
                    "현재 패턴 ➔ 일반 패턴 복구 (필요 토큰: 1개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), normalPatternReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), normalPatternReceipt, 15, 2, 0.05F));
        }
        if (turret.getArrowPattern() != 1) {
            ItemStack fanReceipt = createUpgradeReceipt(turret, "fan", "터렛 기술 인증서: 부채꼴 확산",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 업그레이드된 화살 수 만큼 넓은 범위로 부채꼴 광범위 사격을 가합니다.",
                    "현재 패턴 ➔ 부채꼴 확산 패턴 (필요 토큰: 2개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 32), fanReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 2), fanReceipt, 5, 5, 0.05F));
        }
        if (turret.getArrowPattern() != 2) {
            ItemStack burstReceipt = createUpgradeReceipt(turret, "burst", "터렛 기술 인증서: 일직선 속사(점사)",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 0.1초 연사 간격으로 화살을 일직선 점사로 뿜어냅니다.",
                    "현재 패턴 ➔ 일직선 점사 패턴 (필요 토큰: 2개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 32), burstReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 2), burstReceipt, 5, 5, 0.05F));
        }
        if (turret.getArrowPattern() != 3) {
            ItemStack knockbackReceipt = createUpgradeReceipt(turret, "knockback_mode", "터렛 기술 인증서: 넉백 전용 속사",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 데미지는 1/10로 줄어들지만, 몹의 무적 시간을 리셋하며 파괴적인 연속 밀치기를 가합니다.",
                    "현재 패턴 ➔ 넉백 특화 모드 (필요 토큰: 3개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 48), knockbackReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 3), knockbackReceipt, 5, 5, 0.05F));
        }
        if (turret.getArrowPattern() != 4) {
            ItemStack heavyReceipt = createUpgradeReceipt(turret, "heavy_mode", "터렛 기술 인증서: 동시 강공격 격발",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 모든 화살을 완전 동시(0초)에 격발해 무적 프레임을 무시하는 한 방 극딜을 쏟아냅니다.",
                    "현재 패턴 ➔ 동시 강공격 모드 (필요 토큰: 4개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 64), heavyReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 4), heavyReceipt, 5, 5, 0.05F));
        }

        // [진공 제어 토글 변경]
        if (turret.getNoGravityEnabled() == 1) {
            ItemStack normalGravityReceipt = createUpgradeReceipt(turret, "normal_gravity", "터렛 기술 인증서: 일반 중력 환경 복원",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 무중력을 리셋하고, 투사체가 마찰과 중력의 보정을 받아 낙하 포물선 궤적으로 발사됩니다.",
                    "현재 상태: 무중력 ➔ 일반 중력 전환 (필요 토큰: 1개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), normalGravityReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), normalGravityReceipt, 15, 2, 0.05F));
        } else {
            ItemStack vacuumReceipt = createUpgradeReceipt(turret, "vacuum", "터렛 기술 인증서: 진공 환경 제어",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 중력을 배제하여 투사체가 낙하 없이 영구적 일직선으로 비행합니다.",
                    "현재 상태: 일반 중력 ➔ 무중력 전환 (필요 토큰: 2개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 24), vacuumReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 2), vacuumReceipt, 5, 5, 0.05F));
        }

        // [구조물 관통 제어 토글 변경]
        if (turret.getPassBlocksEnabled() == 1) {
            ItemStack normalBlocksReceipt = createUpgradeReceipt(turret, "normal_blocks", "터렛 기술 인증서: 일반 장애물 충돌 복구",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 관통 기능을 끄고 화살이 벽이나 블록 등의 장애물 구조물에 정상적으로 충돌해 박히도록 합니다.",
                    "현재 상태: 구조물 관통 ➔ 충돌 활성 (필요 토큰: 1개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), normalBlocksReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), normalBlocksReceipt, 15, 2, 0.05F));
        } else {
            ItemStack passBlocksReceipt = createUpgradeReceipt(turret, "pass_blocks", "터렛 기술 인증서: 물질 관통 제어 (벽 투과)",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 화살이 벽이나 지형 블록 등의 구조물 방해를 받지 않고 그대로 뚫고 통과하여 적에게 도달합니다.",
                    "현재 상태: 충돌 활성 ➔ 구조물 관통 (필요 토큰: 5개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 80), passBlocksReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 5), passBlocksReceipt, 5, 5, 0.05F));
        }

        // [벽 너머 투시 감지 토글 변경]
        if (turret.getCanSeeThroughWalls() == 1) {
            ItemStack normalSightReceipt = createUpgradeReceipt(turret, "normal_sight", "터렛 기술 인증서: 표준 시야 감지 복구",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 투시 능력을 해제하고, 터렛 앞에 표적이 가려짐 없이 직접 노출되어 보일 때만 추적 및 공격하도록 합니다.",
                    "현재 상태: 투시 감지 ➔ 표준 시야 (필요 토큰: 1개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), normalSightReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), normalSightReceipt, 15, 2, 0.05F));
        } else {
            ItemStack canSeeThroughWallsReceipt = createUpgradeReceipt(turret, "can_see_through_walls", "터렛 기술 인증서: 투시 감지 제어 (벽 너머 조준)",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 장애물이나 장벽 너머 보이지 않는 구역에 대기 중인 적을 감지하여 월핵처럼 표적을 정확히 조준합니다.",
                    "현재 상태: 표준 시야 ➔ 투시 감지 (필요 토큰: 3개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 48), canSeeThroughWallsReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 3), canSeeThroughWallsReceipt, 5, 5, 0.05F));
        }

        // [연금 화살 종류 토글 변경]
        if (turret.getArrowType() != 0) {
            ItemStack normalArrowReceipt = createUpgradeReceipt(turret, "normal_arrow", "터렛 연금 인증서: 일반 화살 무장 전환",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 모든 상태이상 약물 효과를 초기화하고 깨끗한 표준 무효과 화살로 교체합니다.",
                    "현재 장착: " + getArrowTypeName(turret) + " ➔ 일반 화살 (필요 토큰: 1개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), normalArrowReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), normalArrowReceipt, 15, 2, 0.05F));
        }
        if (turret.getArrowType() != 1) {
            ItemStack poisonReceipt = createUpgradeReceipt(turret, "poison", "터렛 연금 인증서: 치명적인 독 화살",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 모든 발사체 화살에 강력한 치명상 독을 묻힙니다.",
                    "현재 장착: " + getArrowTypeName(turret) + " ➔ 독 화살 (필요 토큰: 3개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 48), poisonReceipt, 5, 10, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 3), poisonReceipt, 5, 10, 0.05F));
        }
        if (turret.getArrowType() != 2) {
            ItemStack weaknessReceipt = createUpgradeReceipt(turret, "weakness", "터렛 연금 인증서: 나약함의 쇠퇴 화살",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 타격한 몹의 공격력을 대폭 영구 약화시킵니다.",
                    "현재 장착: " + getArrowTypeName(turret) + " ➔ 나약함 화살 (필요 토큰: 3개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 48), weaknessReceipt, 5, 10, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 3), weaknessReceipt, 5, 10, 0.05F));
        }
        if (turret.getArrowType() != 3) {
            ItemStack slownessReceipt = createUpgradeReceipt(turret, "slowness", "터렛 연금 인증서: 감속의 제동 화살",
                    "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                    "효과: 타격한 몹의 이동 속도를 크게 둔화시켜 저격 타임을 확보합니다.",
                    "현재 장착: " + getArrowTypeName(turret) + " ➔ 감속 화살 (필요 토큰: 2개)");
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 32), slownessReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 2), slownessReceipt, 5, 5, 0.05F));
        }

        offers.addAll(tokenOffers);
        offers.addAll(emeraldOffers);
    }

    private static String getArrowTypeName(VillagerTurretEntity turret) {
        switch(turret.getArrowType()) {
            case 1: return "독 화살";
            case 2: return "나약함 화살";
            case 3: return "감속 화살";
            default: return "일반 화살";
        }
    }

    public static ItemStack getBoundToken(VillagerTurretEntity turret, int count) {
        ItemStack token = new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), count);
        CompoundTag tag = token.getOrCreateTag();
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(new TextComponent("터렛 포인트 토큰").withStyle(style -> style.withColor(0xFFD700).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("허용 터렛 종류: " + turret.getType().getDescription().getString()).withStyle(style -> style.withColor(0xAAAAAA)))));
        display.put("Lore", lore);

        tag.put("display", display);
        return token;
    }

    public static ItemStack createUpgradeReceipt(VillagerTurretEntity turret, String type, String title, String desc1, String desc2, String currentLvlInfo) {
        Item baseItem;
        if (type.equals("speed")) baseItem = ModItems.SPEED_UPGRADE.get();
        else if (type.equals("recharge")) baseItem = ModItems.RECHARGE_UPGRADE.get();
        else if (type.equals("burst")) baseItem = ModItems.BURST_UPGRADE.get();
        else if (type.equals("fan")) baseItem = ModItems.FAN_UPGRADE.get();
        else if (type.equals("poison")) baseItem = ModItems.POISON_ARROW_UPGRADE.get();
        else baseItem = Items.PAPER;

        ItemStack stack = new ItemStack(baseItem);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("UpgradeType", type);
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(new TextComponent(title).withStyle(style -> style.withColor(0xFF55FF).withBold(true).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(desc1).withStyle(style -> style.withColor(0x55FF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(desc2).withStyle(style -> style.withColor(0xFFFF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(currentLvlInfo).withStyle(style -> style.withColor(0x55FFFF)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("허용 터렛 종류: " + turret.getType().getDescription().getString()).withStyle(style -> style.withColor(0xAAAAAA)))));

        display.put("Lore", lore);
        tag.put("display", display);

        return stack;
    }

    public static void applyUpgradeDirectly(VillagerTurretEntity turret, String type) {
        Player tradingPlayer = turret.getTradingPlayer();

        if (type.equals("recharge")) {
            turret.setRechargeLevel(turret.getRechargeLevel() + 1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("재장전 속도 업그레이드 완료! (현재: " + turret.getRechargeLevel() + "레벨)"), true);
            }
        } else if (type.equals("speed")) {
            turret.setSpeedLevel(turret.getSpeedLevel() + 1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("화살 비행 속도 업그레이드 완료! (현재: " + turret.getSpeedLevel() + "레벨)"), true);
            }
        } else if (type.equals("invuln")) {
            turret.setInvulnerabilityLevel(turret.getInvulnerabilityLevel() + 1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("무적 시간 업그레이드 완료! (현재 피격 시: " + turret.getInvulnerabilityLevel() + "초 무적)"), true);
            }
        } else if (type.equals("heal")) {
            turret.setHealLevel(turret.getHealLevel() + 1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("자가 회복력 업그레이드 완료! (현재: 5초당 +1 치유 / 레벨업 시 최대 체력의 " + (turret.getHealLevel() * 20) + "% 회복)"), true);
            }
        } else if (type.equals("xp_discount")) {
            int previousNeeded = turret.getNeededXp();
            int discounted = (previousNeeded + 1) / 2;
            turret.setNeededXp(discounted);
            turret.addXp(0);

            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("경험치 속성 훈련 완료! (레벨업 요구량: " + previousNeeded + " ➔ " + discounted + ")"), true);
            }
        } else if (type.equals("arrow_count")) {
            if (turret.getTurretArrowCount() < 10) { // 고유 메소드로 변경
                turret.setTurretArrowCount(turret.getTurretArrowCount() + 1); // 고유 메소드로 변경
                if (tradingPlayer != null) {
                    tradingPlayer.displayClientMessage(new TextComponent("화살 발사 개수 업그레이드 완료! (현재: " + turret.getTurretArrowCount() + "개 격발)"), true);
                }
            } else {
                if (tradingPlayer != null) {
                    tradingPlayer.displayClientMessage(new TextComponent("이미 최대 업그레이드 상태입니다!"), true);
                }
                return;
            }
        } else if (type.equals("normal_pattern")) {
            turret.setArrowPattern(0);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("사격 패턴 리셋 완료: 표준 정밀 사격 장착"), true);
            }
        } else if (type.equals("burst")) {
            turret.setArrowPattern(2);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("발사 패턴 변경 완료: 일직선 속사(점사)"), true);
            }
        } else if (type.equals("fan")) {
            turret.setArrowPattern(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("발사 패턴 변경 완료: 부채꼴 확산형"), true);
            }
        } else if (type.equals("knockback_mode")) {
            turret.setArrowPattern(3);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("발사 패턴 변경 완료: 넉백 특화 모드(연사 및 넉백 최대화)"), true);
            }
        } else if (type.equals("heavy_mode")) {
            turret.setArrowPattern(4);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("발사 패턴 변경 완료: 동시 강공격 격발 모드 장착!"), true);
            }
        } else if (type.equals("normal_gravity")) {
            turret.setNoGravityEnabled(0);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("중력 복구 완료: 마찰 및 곡사 낙하 비행 활성화"), true);
            }
        } else if (type.equals("vacuum")) {
            turret.setNoGravityEnabled(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("진공 환경 제어 장치 장착 완료: 무중력 투사체 적용!"), true);
            }
        } else if (type.equals("normal_blocks")) {
            turret.setPassBlocksEnabled(0);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("물질 충돌 활성화 완료: 화살이 블록에 부딪혀 고정됩니다."), true);
            }
        } else if (type.equals("pass_blocks")) {
            turret.setPassBlocksEnabled(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("물질 통과 제어 완료! 화살이 구조물 벽을 뚫고 통과합니다."), true);
            }
        } else if (type.equals("normal_sight")) {
            turret.setCanSeeThroughWalls(0);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("시야 제어 정상화 완료: 표준 탐색 및 조준 적용"), true);
            }
        } else if (type.equals("can_see_through_walls")) {
            turret.setCanSeeThroughWalls(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("투시 감지 세팅 완료! 벽 너머에 가려진 몹까지 포착합니다."), true);
            }
        } else if (type.equals("normal_arrow")) {
            turret.setArrowType(0);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("특수 포션 세팅 초기화 완료: 일반 표준 화살 장착"), true);
            }
        } else if (type.equals("poison")) {
            turret.setArrowType(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("화살 특성 변경 완료: 치명적인 독 화살 탑재"), true);
            }
        } else if (type.equals("weakness")) {
            turret.setArrowType(2);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("화살 특성 변경 완료: 나약함의 쇠퇴 화살 탑재"), true);
            }
        } else if (type.equals("slowness")) {
            turret.setArrowType(3);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TextComponent("화살 특성 변경 완료: 감속의 제동 화살 탑재"), true);
            }
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 1.0F);

        turret.overrideOffers(null);
        if (tradingPlayer != null) {
            turret.openTradingScreen(tradingPlayer, turret.getDisplayName(), turret.getVillagerXp());
        }
    }
}
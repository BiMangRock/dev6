package changmin.changmin_villager_turret.feature.turret.plasma_wizard;

import changmin.changmin_villager_turret.registry.ModItems;
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
            ItemStack cdReceipt = createUpgradeReceipt(turret, "cooldown", "터렛 기술 인증서: 초과 충전 코일",
                    "■ 터렛의 전도성 회로 구조를 개선합니다.",
                    "효과: 플라즈마 충전 및 쿨타임 대기시간이 0.1초 감소합니다.",
                    "현재 대기시간: " + curSec + "초 ➔ " + nextSec + "초");
            // 🌟 철 주괴 요구 수량을 16개에서 1개로 변경
            materialOffers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 1), cdReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.PLASMA_POINT_TOKEN_LOW.get(), 1), cdReceipt, 15, 2, 0.05F));
        }

        int szLvl = turret.getSizeLevel();
        if (szLvl < 5) {
            float curScale = turret.getCalculatedScale();
            float nextScale = 1.0F + (szLvl + 1) * 0.25F;
            ItemStack szReceipt = createUpgradeReceipt(turret, "size", "터렛 기술 인증서: 플라즈마 압축 필드",
                    "■ 발사하는 플라즈마 구체의 팽창 계수를 올립니다.",
                    "효과: 플라즈마 구체의 물리적 피격 한계선 및 크기가 25% 거대화됩니다.",
                    "현재 상태: " + curScale + "배 ➔ " + nextScale + "배");
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
            ItemStack dmgReceipt = createUpgradeReceipt(turret, "damage", "터렛 기술 인증서: 고출력 가속 발전기",
                    "■ 플라즈마 입자의 충돌 밀도를 극대화합니다.",
                    "효과: 기본 타격 시 입히는 마법 마찰 피해량이 +1.5 증가합니다.",
                    "현재 상태: " + curDmg + " ➔ " + nextDmg);
            // 🌟 금 주괴 요구 수량을 16개에서 1개로 변경
            materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), dmgReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.PLASMA_POINT_TOKEN_MID.get(), 1), dmgReceipt, 15, 2, 0.05F));
        }

        int stnLvl = turret.getStunLevel();
        if (stnLvl < 5) {
            float curSec = turret.getCalculatedStunDuration() / 20.0F;
            float nextSec = curSec + 1.0F;
            ItemStack stnReceipt = createUpgradeReceipt(turret, "stun", "터렛 기술 인증서: 고압 정전기 충격기",
                    "■ 전자기 마비 파동을 고전압 주파수로 조정합니다.",
                    "효과: 타격한 대상을 경직시키는 정전기 기절 상태이상 지속 시간이 1.0초 연장됩니다.",
                    "현재 상태: " + curSec + "초 ➔ " + nextSec + "초");
            // 🌟 금 주괴 요구 수량을 16개에서 1개로 변경
            materialOffers.add(new MerchantOffer(new ItemStack(Items.GOLD_INGOT, 1), stnReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.PLASMA_POINT_TOKEN_MID.get(), 1), stnReceipt, 15, 2, 0.05F));
        }

        // ==========================================
        // 🔴 상급 업그레이드 (상급 토큰 및 에메랄드 1개 요구)
        // ==========================================
        ItemStack splitReceipt = createUpgradeReceipt(turret, "split_shot", "터렛 비기 전수: 다중 갈래 발사",
                "■ 전방 부채꼴 공간에 동시 타격을 실행하는 고대 제어 기술입니다.",
                "효과: 구체를 한 번에 부채꼴로 2~3발 동시 사격합니다.",
                "현재 상태: " + (turret.getSplitShotLevel() == 0 ? "1발 사격 ➔ 2발 부채꼴" : (turret.getSplitShotLevel() == 1 ? "2발 부채꼴 ➔ 3발 부채꼴" : "★ 비기 전수 완료")));

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
        Item tokenItem = ModItems.PLASMA_POINT_TOKEN_LOW.get();
        String tierName = "하급";
        int nameColor = 0xFFD700;

        if (tier == 1) {
            tokenItem = ModItems.PLASMA_POINT_TOKEN_MID.get();
            tierName = "중급";
            nameColor = 0x55FF55;
        } else if (tier == 2) {
            tokenItem = ModItems.PLASMA_POINT_TOKEN_HIGH.get();
            tierName = "상급";
            nameColor = 0xFF55FF;
        }

        ItemStack token = new ItemStack(tokenItem, count);
        CompoundTag tag = token.getOrCreateTag();
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        final String finalTierName = tierName;
        final int finalColor = nameColor;

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(
                new TextComponent("플라즈마 터렛 포인트 토큰 (" + finalTierName + ")")
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

    public static ItemStack createUpgradeReceipt(PlasmaWizardEntity turret, String type, String title, String desc1, String desc2, String currentLvlInfo) {
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

    public static void applyUpgradeDirectly(PlasmaWizardEntity turret, String type) {
        Player player = turret.getTradingPlayer();

        if (type.equals("cooldown")) {
            if (turret.getCooldownLevel() < 15) {
                turret.setCooldownLevel(turret.getCooldownLevel() + 1);
                if (player != null) {
                    player.displayClientMessage(new TextComponent("플라즈마 재충전 회로 단축 완료! (현재 발사 속도: " + (turret.getCalculatedCooldown() / 20.0F) + "초)"), true);
                }
            }
        } else if (type.equals("size")) {
            if (turret.getSizeLevel() < 5) {
                turret.setSizeLevel(turret.getSizeLevel() + 1);
                if (player != null) {
                    player.displayClientMessage(new TextComponent("플라즈마 구체 압축 팽창율 증가 완료! (현재 크기: " + turret.getCalculatedScale() + "배)"), true);
                }
            }
        } else if (type.equals("damage")) {
            if (turret.getDamageLevel() < 10) {
                turret.setDamageLevel(turret.getDamageLevel() + 1);
                if (player != null) {
                    player.displayClientMessage(new TextComponent("고전압 전자기 마찰 데미지 강화 완료! (현재 대미지: " + turret.getCalculatedDamage() + ")"), true);
                }
            }
        } else if (type.equals("stun")) {
            if (turret.getStunLevel() < 5) {
                turret.setStunLevel(turret.getStunLevel() + 1);
                if (player != null) {
                    player.displayClientMessage(new TextComponent("기절 전자기 방출 마비 지속 시간 증가 완료! (현재 지속 시간: " + (turret.getCalculatedStunDuration() / 20.0F) + "초)"), true);
                }
            }
        } else if (type.equals("split_shot")) {
            if (turret.getSplitShotLevel() < 2) {
                turret.setSplitShotLevel(turret.getSplitShotLevel() + 1);
                if (player != null) {
                    player.displayClientMessage(new TextComponent("플라즈마 부채꼴 다중 분사 비기 업그레이드 완료! (" + (turret.getSplitShotLevel() + 1) + "갈래 분사)"), true);
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
package changmin.myMod.feature.turret.villager_turret;

import changmin.myMod.feature.turret.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent; // 변경된 임포트
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
        ItemStack rechargeReceipt = createUpgradeReceipt(turret, "recharge",
                "mymod.upgrade.recharge.title",
                "mymod.upgrade.recharge.desc1",
                "mymod.upgrade.recharge.desc2",
                new TranslatableComponent("mymod.upgrade.recharge.info", turret.getRechargeLevel(), turret.getRechargeLevel() + 1));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), rechargeReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), rechargeReceipt, 15, 2, 0.05F));

        // 2. 화살 비행 속도
        ItemStack speedReceipt = createUpgradeReceipt(turret, "speed",
                "mymod.upgrade.speed.title",
                "mymod.upgrade.speed.desc1",
                "mymod.upgrade.speed.desc2",
                new TranslatableComponent("mymod.upgrade.speed.info", turret.getSpeedLevel(), turret.getSpeedLevel() + 1));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 12), speedReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), speedReceipt, 15, 2, 0.05F));

        // 3. 피격 후 무적 시간
        int currentInvuln = turret.getInvulnerabilityLevel();
        ItemStack invulnReceipt = createUpgradeReceipt(turret, "invuln",
                "mymod.upgrade.invuln.title",
                "mymod.upgrade.invuln.desc1",
                "mymod.upgrade.invuln.desc2",
                new TranslatableComponent("mymod.upgrade.invuln.info", currentInvuln, currentInvuln + 1));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), invulnReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), invulnReceipt, 15, 2, 0.05F));

        // 4. 자가 회복력
        int currentHeal = turret.getHealLevel();
        ItemStack healReceipt = createUpgradeReceipt(turret, "heal",
                "mymod.upgrade.heal.title",
                "mymod.upgrade.heal.desc1",
                "mymod.upgrade.heal.desc2",
                new TranslatableComponent("mymod.upgrade.heal.info", currentHeal, currentHeal + 1));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 12), healReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), healReceipt, 15, 2, 0.05F));

        // 5. 경험치 50% 할인
        int currentNeededXp = turret.getNeededXp();
        int discountedXp = (currentNeededXp + 1) / 2;
        ItemStack xpDiscountReceipt = createUpgradeReceipt(turret, "xp_discount",
                "mymod.upgrade.xp_discount.title",
                "mymod.upgrade.xp_discount.desc1",
                "mymod.upgrade.xp_discount.desc2",
                new TranslatableComponent("mymod.upgrade.xp_discount.info", currentNeededXp, discountedXp));
        emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), xpDiscountReceipt, 15, 2, 0.05F));
        tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), xpDiscountReceipt, 15, 2, 0.05F));

        // 사격 패턴 변경
        if (turret.getArrowPattern() != 0) {
            ItemStack normalPatternReceipt = createUpgradeReceipt(turret, "normal_pattern",
                    "mymod.upgrade.normal_pattern.title",
                    "mymod.upgrade.normal_pattern.desc1",
                    "mymod.upgrade.normal_pattern.desc2",
                    new TranslatableComponent("mymod.upgrade.normal_pattern.info"));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), normalPatternReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), normalPatternReceipt, 15, 2, 0.05F));
        }
        if (turret.getArrowPattern() != 1) {
            ItemStack fanReceipt = createUpgradeReceipt(turret, "fan",
                    "mymod.upgrade.fan.title",
                    "mymod.upgrade.fan.desc1",
                    "mymod.upgrade.fan.desc2",
                    new TranslatableComponent("mymod.upgrade.fan.info"));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 32), fanReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 2), fanReceipt, 5, 5, 0.05F));
        }
        if (turret.getArrowPattern() != 2) {
            ItemStack burstReceipt = createUpgradeReceipt(turret, "burst",
                    "mymod.upgrade.burst.title",
                    "mymod.upgrade.burst.desc1",
                    "mymod.upgrade.burst.desc2",
                    new TranslatableComponent("mymod.upgrade.burst.info"));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 32), burstReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 2), burstReceipt, 5, 5, 0.05F));
        }

        // 중력 제어
        if (turret.getNoGravityEnabled() == 1) {
            ItemStack normalGravityReceipt = createUpgradeReceipt(turret, "normal_gravity",
                    "mymod.upgrade.normal_gravity.title",
                    "mymod.upgrade.normal_gravity.desc1",
                    "mymod.upgrade.normal_gravity.desc2",
                    new TranslatableComponent("mymod.upgrade.normal_gravity.info"));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), normalGravityReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), normalGravityReceipt, 15, 2, 0.05F));
        } else {
            ItemStack vacuumReceipt = createUpgradeReceipt(turret, "vacuum",
                    "mymod.upgrade.vacuum.title",
                    "mymod.upgrade.vacuum.desc1",
                    "mymod.upgrade.vacuum.desc2",
                    new TranslatableComponent("mymod.upgrade.vacuum.info"));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 24), vacuumReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 2), vacuumReceipt, 5, 5, 0.05F));
        }

        // 구조물 관통
        if (turret.getPassBlocksEnabled() == 1) {
            ItemStack normalBlocksReceipt = createUpgradeReceipt(turret, "normal_blocks",
                    "mymod.upgrade.normal_blocks.title",
                    "mymod.upgrade.normal_blocks.desc1",
                    "mymod.upgrade.normal_blocks.desc2",
                    new TranslatableComponent("mymod.upgrade.normal_blocks.info"));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), normalBlocksReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), normalBlocksReceipt, 15, 2, 0.05F));
        } else {
            ItemStack passBlocksReceipt = createUpgradeReceipt(turret, "pass_blocks",
                    "mymod.upgrade.pass_blocks.title",
                    "mymod.upgrade.pass_blocks.desc1",
                    "mymod.upgrade.pass_blocks.desc2",
                    new TranslatableComponent("mymod.upgrade.pass_blocks.info"));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 80), passBlocksReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 5), passBlocksReceipt, 5, 5, 0.05F));
        }

        // 벽 너머 투시
        if (turret.getCanSeeThroughWalls() == 1) {
            ItemStack normalSightReceipt = createUpgradeReceipt(turret, "normal_sight",
                    "mymod.upgrade.normal_sight.title",
                    "mymod.upgrade.normal_sight.desc1",
                    "mymod.upgrade.normal_sight.desc2",
                    new TranslatableComponent("mymod.upgrade.normal_sight.info"));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), normalSightReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), normalSightReceipt, 15, 2, 0.05F));
        } else {
            ItemStack canSeeThroughWallsReceipt = createUpgradeReceipt(turret, "can_see_through_walls",
                    "mymod.upgrade.can_see_through_walls.title",
                    "mymod.upgrade.can_see_through_walls.desc1",
                    "mymod.upgrade.can_see_through_walls.desc2",
                    new TranslatableComponent("mymod.upgrade.can_see_through_walls.info"));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 48), canSeeThroughWallsReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 3), canSeeThroughWallsReceipt, 5, 5, 0.05F));
        }

        // 연금 화살
        if (turret.getArrowType() != 0) {
            ItemStack normalArrowReceipt = createUpgradeReceipt(turret, "normal_arrow",
                    "mymod.upgrade.normal_arrow.title",
                    "mymod.upgrade.normal_arrow.desc1",
                    "mymod.upgrade.normal_arrow.desc2",
                    new TranslatableComponent("mymod.upgrade.normal_arrow.info", getArrowTypeComponent(turret)));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), normalArrowReceipt, 15, 2, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 1), normalArrowReceipt, 15, 2, 0.05F));
        }
        if (turret.getArrowType() != 1) {
            ItemStack poisonReceipt = createUpgradeReceipt(turret, "poison",
                    "mymod.upgrade.poison.title",
                    "mymod.upgrade.poison.desc1",
                    "mymod.upgrade.poison.desc2",
                    new TranslatableComponent("mymod.upgrade.poison.info", getArrowTypeComponent(turret)));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 48), poisonReceipt, 5, 10, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 3), poisonReceipt, 5, 10, 0.05F));
        }
        if (turret.getArrowType() != 2) {
            ItemStack weaknessReceipt = createUpgradeReceipt(turret, "weakness",
                    "mymod.upgrade.weakness.title",
                    "mymod.upgrade.weakness.desc1",
                    "mymod.upgrade.weakness.desc2",
                    new TranslatableComponent("mymod.upgrade.weakness.info", getArrowTypeComponent(turret)));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 48), weaknessReceipt, 5, 10, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 3), weaknessReceipt, 5, 10, 0.05F));
        }
        if (turret.getArrowType() != 3) {
            ItemStack slownessReceipt = createUpgradeReceipt(turret, "slowness",
                    "mymod.upgrade.slowness.title",
                    "mymod.upgrade.slowness.desc1",
                    "mymod.upgrade.slowness.desc2",
                    new TranslatableComponent("mymod.upgrade.slowness.info", getArrowTypeComponent(turret)));
            emeraldOffers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 32), slownessReceipt, 5, 5, 0.05F));
            tokenOffers.add(new MerchantOffer(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), 2), slownessReceipt, 5, 5, 0.05F));
        }

        offers.addAll(tokenOffers);
        offers.addAll(emeraldOffers);
    }

    private static Component getArrowTypeComponent(VillagerTurretEntity turret) {
        switch(turret.getArrowType()) {
            case 1: return new TranslatableComponent("mymod.arrow_type.poison");
            case 2: return new TranslatableComponent("mymod.arrow_type.weakness");
            case 3: return new TranslatableComponent("mymod.arrow_type.slowness");
            default: return new TranslatableComponent("mymod.arrow_type.normal");
        }
    }

    public static ItemStack getBoundToken(VillagerTurretEntity turret, int count) {
        ItemStack token = new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), count);
        CompoundTag tag = token.getOrCreateTag();
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        // 번역 가능한 아이템 이름을 적용
        display.putString("Name", Component.Serializer.toJson(new TranslatableComponent("item.mymod.turret_point_token").withStyle(style -> style.withColor(0xFFD700).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("mymod.upgrade.allowed_turret", turret.getType().getDescription()).withStyle(style -> style.withColor(0xAAAAAA)))));
        display.put("Lore", lore);

        tag.put("display", display);
        return token;
    }

    public static ItemStack createUpgradeReceipt(VillagerTurretEntity turret, String type, String titleKey, String desc1Key, String desc2Key, Component currentLvlInfo) {
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
        // 클라이언트에서 해석할 수 있는 JSON 형태로 TranslatableComponent 저장
        display.putString("Name", Component.Serializer.toJson(new TranslatableComponent(titleKey).withStyle(style -> style.withColor(0xFF55FF).withBold(true).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent(desc1Key).withStyle(style -> style.withColor(0x55FF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent(desc2Key).withStyle(style -> style.withColor(0xFFFF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(currentLvlInfo.copy().withStyle(style -> style.withColor(0x55FFFF)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TranslatableComponent("mymod.upgrade.allowed_turret", turret.getType().getDescription()).withStyle(style -> style.withColor(0xAAAAAA)))));

        display.put("Lore", lore);
        tag.put("display", display);

        return stack;
    }

    public static void applyUpgradeDirectly(VillagerTurretEntity turret, String type) {
        Player tradingPlayer = turret.getTradingPlayer();

        if (type.equals("recharge")) {
            turret.setRechargeLevel(turret.getRechargeLevel() + 1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.recharge.success", turret.getRechargeLevel()), true);
            }
        } else if (type.equals("speed")) {
            turret.setSpeedLevel(turret.getSpeedLevel() + 1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.speed.success", turret.getSpeedLevel()), true);
            }
        } else if (type.equals("invuln")) {
            turret.setInvulnerabilityLevel(turret.getInvulnerabilityLevel() + 1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.invuln.success", turret.getInvulnerabilityLevel()), true);
            }
        } else if (type.equals("heal")) {
            turret.setHealLevel(turret.getHealLevel() + 1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.heal.success", turret.getHealLevel() * 20), true);
            }
        } else if (type.equals("xp_discount")) {
            int previousNeeded = turret.getNeededXp();
            int discounted = (previousNeeded + 1) / 2;
            turret.setNeededXp(discounted);
            turret.addXp(0);

            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.xp_discount.success", previousNeeded, discounted), true);
            }
        } else if (type.equals("normal_pattern")) {
            turret.setArrowPattern(0);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.normal_pattern.success"), true);
            }
        } else if (type.equals("burst")) {
            turret.setArrowPattern(2);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.burst.success"), true);
            }
        } else if (type.equals("fan")) {
            turret.setArrowPattern(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.fan.success"), true);
            }
        } else if (type.equals("normal_gravity")) {
            turret.setNoGravityEnabled(0);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.normal_gravity.success"), true);
            }
        } else if (type.equals("vacuum")) {
            turret.setNoGravityEnabled(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.vacuum.success"), true);
            }
        } else if (type.equals("normal_blocks")) {
            turret.setPassBlocksEnabled(0);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.normal_blocks.success"), true);
            }
        } else if (type.equals("pass_blocks")) {
            turret.setPassBlocksEnabled(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.pass_blocks.success"), true);
            }
        } else if (type.equals("normal_sight")) {
            turret.setCanSeeThroughWalls(0);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.normal_sight.success"), true);
            }
        } else if (type.equals("can_see_through_walls")) {
            turret.setCanSeeThroughWalls(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.can_see_through_walls.success"), true);
            }
        } else if (type.equals("normal_arrow")) {
            turret.setArrowType(0);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.normal_arrow.success"), true);
            }
        } else if (type.equals("poison")) {
            turret.setArrowType(1);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.poison.success"), true);
            }
        } else if (type.equals("weakness")) {
            turret.setArrowType(2);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.weakness.success"), true);
            }
        } else if (type.equals("slowness")) {
            turret.setArrowType(3);
            if (tradingPlayer != null) {
                tradingPlayer.displayClientMessage(new TranslatableComponent("mymod.upgrade.slowness.success"), true);
            }
        }

        turret.playSound(SoundEvents.ANVIL_USE, 1.0F, 1.0F);

        turret.overrideOffers(null);
        if (tradingPlayer != null) {
            turret.openTradingScreen(tradingPlayer, turret.getDisplayName(), turret.getVillagerXp());
        }
    }
}
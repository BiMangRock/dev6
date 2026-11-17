package changmin.changmin_villager_turret.feature.turret.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 🧾 터렛 거래용 아이템(포인트 토큰, 업그레이드 인증서) 생성 공통 함수
 *
 * 7개 TradeManager가 각자 복사해 쓰던 getBoundToken / createUpgradeReceipt 본문을 한 곳으로 모았습니다.
 * 각 TradeManager의 기존 메서드 이름·인자는 그대로 두고 이 함수를 호출만 하므로, 다른 코드는 바꿀 필요가 없습니다.
 */
public final class TurretTradeUtil {

    // 자주 쓰는 설명 줄 색상
    public static final int GREEN = 0x55FF55;
    public static final int YELLOW = 0xFFFF55;
    public static final int CYAN = 0x55FFFF;
    public static final int MAGENTA = 0xFF55FF;
    public static final int GRAY = 0xAAAAAA;

    public static final String APPLY_NOW = "■ 구매 즉시 이 터렛에 즉시 적용됩니다.";

    /** 설명 한 줄 (텍스트 + 색상) */
    public record Line(String text, int color) {}

    public static Line line(String text, int color) { return new Line(text, color); }

    private TurretTradeUtil() {}

    /** 특정 터렛 종류에 귀속된 포인트 토큰 */
    public static ItemStack boundToken(Item tokenItem, Entity turret, int count, String name, int nameColor) {
        ItemStack token = new ItemStack(tokenItem, count);
        CompoundTag tag = token.getOrCreateTag();
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(
                new TextComponent(name).withStyle(style -> style.withColor(nameColor).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(loreLine(allowedTurretText(turret), GRAY));
        display.put("Lore", lore);

        tag.put("display", display);
        return token;
    }

    /**
     * 업그레이드 인증서. lines 순서대로 설명이 붙고, 마지막에 "허용 터렛 종류" 줄이 자동으로 추가됩니다.
     */
    public static ItemStack upgradeReceipt(Item baseItem, Entity turret, String type, String title, int titleColor, Line... lines) {
        ItemStack stack = new ItemStack(baseItem);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("UpgradeType", type);
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(
                new TextComponent(title).withStyle(style -> style.withColor(titleColor).withBold(true).withItalic(false))));

        ListTag lore = new ListTag();
        for (Line l : lines) {
            lore.add(loreLine(l.text(), l.color()));
        }
        lore.add(loreLine(allowedTurretText(turret), GRAY));
        display.put("Lore", lore);

        tag.put("display", display);
        return stack;
    }

    private static String allowedTurretText(Entity turret) {
        return "허용 터렛 종류: " + turret.getType().getDescription().getString();
    }

    private static StringTag loreLine(String text, int color) {
        return StringTag.valueOf(Component.Serializer.toJson(
                new TextComponent(text).withStyle(style -> style.withColor(color))));
    }
}

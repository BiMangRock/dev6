package changmin.changmin_villager_turret.feature.turret.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 🧾 터렛 거래용 아이템(포인트 토큰, 업그레이드 인증서) 생성 공통 함수
 *
 * 7개 TradeManager가 각자 복사해 쓰던 getBoundToken / createUpgradeReceipt 본문을 한 곳으로 모았습니다.
 *
 * 🌐 모든 문구는 번역 키로 저장됩니다. (tr("키", 값...))
 *    아이템 이름·설명(lore)에 번역 키가 그대로 들어가므로, 같은 아이템도 보는 사람의 게임 언어에 맞게 표시됩니다.
 *    문구 수정은 ko_kr.json / en_us.json 의 changmin_villager_turret.text.* 값을 고치면 됩니다.
 */
public final class TurretTradeUtil {

    // 자주 쓰는 설명 줄 색상
    public static final int GREEN = 0x55FF55;
    public static final int YELLOW = 0xFFFF55;
    public static final int CYAN = 0x55FFFF;
    public static final int MAGENTA = 0xFF55FF;
    public static final int GRAY = 0xAAAAAA;

    /** 설명 한 줄 (문구 + 색상) */
    public record Line(Component text, int color) {}

    public static Line line(Component text, int color) { return new Line(text, color); }

    private TurretTradeUtil() {}

    /** 번역 문구. args는 문구 안의 %s 자리에 순서대로 들어갑니다. (숫자, 문자열, 다른 Component 모두 가능) */
    public static MutableComponent tr(String key, Object... args) {
        return new TranslatableComponent(key, args);
    }

    /** "■ 구매 즉시 이 터렛에 즉시 적용됩니다." */
    public static MutableComponent applyNow() {
        return tr("changmin_villager_turret.text.apply_now");
    }

    /** 이 인증서/토큰이 귀속된 터렛 종류 안내 줄 */
    private static Component allowedTurret(Entity turret) {
        return tr("changmin_villager_turret.upgrade.allowed_turret", turret.getType().getDescription());
    }

    /** 이 아이템이 업그레이드 인증서(거래로 받은 영수증)인지. 예전에 만들어진 인증서도 포함 */
    public static boolean isUpgradeReceipt(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("UpgradeType") && tag.contains("TurretType");
    }

    /** 특정 터렛 종류에 귀속된 포인트 토큰 */
    public static ItemStack boundToken(Item tokenItem, Entity turret, int count, Component name, int nameColor) {
        ItemStack token = new ItemStack(tokenItem, count);
        CompoundTag tag = token.getOrCreateTag();
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(
                name.copy().withStyle(style -> style.withColor(nameColor).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(loreLine(allowedTurret(turret), GRAY));
        display.put("Lore", lore);

        tag.put("display", display);
        return token;
    }

    /**
     * 업그레이드 인증서. lines 순서대로 설명이 붙고, 마지막에 "허용 터렛 종류" 줄이 자동으로 추가됩니다.
     */
    public static ItemStack upgradeReceipt(Item baseItem, Entity turret, String type, Component title, int titleColor, Line... lines) {
        ItemStack stack = new ItemStack(baseItem);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("UpgradeType", type);
        tag.putString("TurretType", turret.getType().getRegistryName().toString());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(
                title.copy().withStyle(style -> style.withColor(titleColor).withBold(true).withItalic(false))));

        ListTag lore = new ListTag();
        for (Line l : lines) {
            lore.add(loreLine(l.text(), l.color()));
        }
        lore.add(loreLine(allowedTurret(turret), GRAY));
        display.put("Lore", lore);

        tag.put("display", display);
        return stack;
    }

    private static StringTag loreLine(Component text, int color) {
        return StringTag.valueOf(Component.Serializer.toJson(
                text.copy().withStyle(style -> style.withColor(color))));
    }
}

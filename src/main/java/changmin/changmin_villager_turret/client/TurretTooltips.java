package changmin.changmin_villager_turret.client;

import changmin.changmin_villager_turret.changmin_villager_turret;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

/**
 * 💬 아이템 툴팁 도움말 (클라이언트 전용)
 *
 * 문구는 전부 언어 파일(ko_kr.json / en_us.json)에 있습니다.
 *   tooltip.changmin_villager_turret.<아이템ID>          : 항상 보이는 한 줄 요약
 *   tooltip.changmin_villager_turret.<아이템ID>.detail   : Shift를 누르면 보이는 상세 설명 (\n 으로 줄바꿈)
 * 키가 없는 아이템은 아무것도 추가하지 않으므로, 새 아이템은 언어 파일에 키만 추가하면 됩니다.
 *
 * 포인트 토큰 21종은 아이템마다 키를 만들지 않고 등급(low/mid/high) 공통 문구에 터렛 이름을 끼워 넣습니다.
 */
@Mod.EventBusSubscriber(modid = changmin_villager_turret.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class TurretTooltips {

    private static final String PREFIX = "tooltip." + changmin_villager_turret.MODID + ".";

    /** 토큰 아이템 ID 앞부분 → 해당 터렛의 엔티티 이름 번역 키 */
    private static final Map<String, String> TOKEN_OWNER = Map.of(
            "turret", "entity.changmin_villager_turret.villager_turret",
            "healer", "entity.changmin_villager_turret.healer_turret",
            "trident", "entity.changmin_villager_turret.trident_turret",
            "tanker", "entity.changmin_villager_turret.tanker_turret",
            "lightning", "entity.changmin_villager_turret.lightning_wizard",
            "plasma", "entity.changmin_villager_turret.plasma_wizard",
            "bee", "entity.changmin_villager_turret.bee_summoner_turret");

    private TurretTooltips() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null || !id.getNamespace().equals(changmin_villager_turret.MODID)) return;

        List<Component> tooltip = event.getToolTip();
        String path = id.getPath();

        // 포인트 토큰: "<터렛>_point_token_<등급>"
        int tokenIdx = path.indexOf("_point_token_");
        if (tokenIdx > 0) {
            String owner = TOKEN_OWNER.get(path.substring(0, tokenIdx));
            String tier = path.substring(tokenIdx + "_point_token_".length());
            if (owner != null) {
                addLines(tooltip, PREFIX + "token." + tier, new TranslatableComponent(owner));
            }
            return;
        }

        addLines(tooltip, PREFIX + path);
    }

    /** 요약 한 줄 + (Shift 시) 상세 여러 줄 */
    private static void addLines(List<Component> tooltip, String key, Object... args) {
        if (!I18n.exists(key)) return;

        tooltip.add(new TranslatableComponent(key, args).withStyle(ChatFormatting.GRAY));

        String detailKey = key + ".detail";
        if (!I18n.exists(detailKey)) return;

        if (Screen.hasShiftDown()) {
            // 상세 문구에는 인자(%s)를 쓰지 않습니다. (줄바꿈을 직접 나누기 위해 문자열로 받기 때문)
            for (String line : I18n.get(detailKey).split("\n")) {
                tooltip.add(new TextComponent(line).withStyle(ChatFormatting.DARK_AQUA));
            }
        } else {
            tooltip.add(new TranslatableComponent(PREFIX + "hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}

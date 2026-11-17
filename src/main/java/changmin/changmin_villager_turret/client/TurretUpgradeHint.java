package changmin.changmin_villager_turret.client;

import changmin.changmin_villager_turret.changmin_villager_turret;
import changmin.changmin_villager_turret.feature.turret.common.AbstractTurretEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.HashMap;
import java.util.Map;

/**
 * ★ 이름표 "업그레이드 가능" 표시 판정 (클라이언트 전용)
 *
 * 내 인벤토리에 있는 **포인트 토큰**만으로 살 수 있는 업그레이드가 하나라도 있으면 true.
 * 철·금·에메랄드 같은 재료 결제는 제외합니다. (철 1개만 있어도 항상 ★가 떠 버리므로)
 *
 * 매 프레임 거래 목록을 다시 만들면 무거우므로, 터렛마다 1초(20틱)에 한 번만 계산해 캐시합니다.
 */
public final class TurretUpgradeHint {

    public static final Component STAR = new TextComponent("★");
    public static final int STAR_COLOR = 0xFFFFD700;

    private static final int RECHECK_TICKS = 20;
    private static final Map<Integer, Entry> CACHE = new HashMap<>();

    private record Entry(long gameTime, boolean result) {}

    private TurretUpgradeHint() {}

    public static boolean canUpgrade(AbstractTurretEntity turret) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;

        long now = turret.level.getGameTime();
        Entry cached = CACHE.get(turret.getId());
        if (cached != null && now - cached.gameTime() >= 0 && now - cached.gameTime() < RECHECK_TICKS) {
            return cached.result();
        }

        boolean result;
        try {
            result = compute(turret, player);
        } catch (RuntimeException e) {
            result = false; // 미리보기 중 예외가 나도 렌더링이 멈추지 않도록
        }

        if (CACHE.size() > 512) CACHE.clear(); // 언로드된 터렛 기록이 계속 쌓이지 않도록
        CACHE.put(turret.getId(), new Entry(now, result));
        return result;
    }

    private static boolean compute(AbstractTurretEntity turret, Player player) {
        MerchantOffers offers = turret.previewOffers();
        for (MerchantOffer offer : offers) {
            if (offer.isOutOfStock()) continue;

            ItemStack costA = offer.getCostA();
            ItemStack costB = offer.getCostB();
            if (!isToken(costA.getItem())) continue;
            if (!costB.isEmpty() && !isToken(costB.getItem())) continue;

            if (countInInventory(player, costA.getItem()) >= costA.getCount()
                    && (costB.isEmpty() || countInInventory(player, costB.getItem()) >= costB.getCount())) {
                return true;
            }
        }
        return false;
    }

    /** 이 모드의 "*_point_token_low/mid/high" 아이템인지 */
    public static boolean isToken(Item item) {
        ResourceLocation id = item.getRegistryName();
        return id != null
                && id.getNamespace().equals(changmin_villager_turret.MODID)
                && id.getPath().contains("_point_token_");
    }

    private static int countInInventory(Player player, Item item) {
        int total = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) total += stack.getCount();
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }
}

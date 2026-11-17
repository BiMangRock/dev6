package changmin.changmin_villager_turret.feature.turret.common;

import changmin.changmin_villager_turret.ally.IAlly;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * 🏗️ 거래(업그레이드) UI를 가진 모든 주민 터렛의 공통 부모 클래스
 *
 * 주민 터렛·삼지창·탱커·치유 토템·번개 마법사·플라즈마 마법사·벌 소환사 7종이
 * 똑같이 복사해 쓰던 코드를 한 곳으로 모았습니다.
 *   - Merchant(거래 화면) 필수 메서드 9개
 *   - 거래 플레이어 / 거래 목록 필드
 *   - 밀리지 않는 고정형 터렛 설정
 *   - 레벨·경험치 공통 API (렌더러·타겟팅 등에서 터렛 종류 상관없이 사용 가능)
 *
 * 새 터렛을 만들 때는 이 클래스를 상속하고 getOffers()/notifyTrade()와
 * 레벨 관련 4개 메서드만 구현하면 됩니다.
 */
public abstract class AbstractTurretEntity extends PathfinderMob implements IAlly, Merchant {

    @Nullable
    protected Player tradingPlayer;
    @Nullable
    protected MerchantOffers offers;

    protected AbstractTurretEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    // ── 레벨·경험치 공통 API ──
    public abstract int getTurretLevel();
    public abstract int getXp();
    public abstract int getNeededXp();
    public abstract void addXp(int amount);

    /** 업그레이드 적용 후 거래 목록을 다시 만들고, 열려 있는 거래 화면을 새로고침합니다. */
    public void refreshTradeScreen() {
        this.overrideOffers(null);
        Player player = this.getTradingPlayer();
        if (player != null) {
            this.openTradingScreen(player, this.getDisplayName(), this.getVillagerXp());
        }
    }

    // ── Merchant 필수 구현 (모든 터렛 공통) ──
    @Override public void setTradingPlayer(@Nullable Player player) { this.tradingPlayer = player; }
    @Override @Nullable public Player getTradingPlayer() { return this.tradingPlayer; }
    @Override public void overrideOffers(@Nullable MerchantOffers offers) { this.offers = offers; }
    @Override public void notifyTradeUpdated(ItemStack stack) {}
    @Override public int getVillagerXp() { return 0; }
    @Override public void overrideXp(int xp) {}
    @Override public boolean showProgressBar() { return false; }
    @Override public SoundEvent getNotifyTradeSound() { return SoundEvents.VILLAGER_YES; }
    @Override public boolean isClientSide() { return this.level.isClientSide; }

    // ── 고정형 터렛: 다른 엔티티에 밀리지 않음 ──
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(Entity entity) {}
}

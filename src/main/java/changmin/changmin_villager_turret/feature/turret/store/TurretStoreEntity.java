package changmin.changmin_villager_turret.feature.turret.store;

import changmin.changmin_villager_turret.registry.ModItems;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class TurretStoreEntity extends PathfinderMob implements Merchant {
    @Nullable
    private Player tradingPlayer;
    @Nullable
    private MerchantOffers offers;

    public TurretStoreEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCustomName(new TextComponent("상점"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired(); // 디스폰 방지
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D) // 체력 30 설정
                .add(Attributes.MOVEMENT_SPEED, 0.0D); // 이동 속도 제로화
    }

    @Override
    public void travel(Vec3 travelVector) {
        super.travel(Vec3.ZERO);
    }

    @Override
    public boolean isPushable() {
        return false; // 밀쳐짐 방지
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // 💡 [수정됨] isTrading() 대신 getTradingPlayer() == null 을 활용하여 컴파일 에러 해결
        if (this.isAlive() && this.getTradingPlayer() == null && !this.level.isClientSide) {
            this.setTradingPlayer(player);
            this.openTradingScreen(player, this.getDisplayName(), 1);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    // --- Merchant 인터페이스 구현 ---

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();

            // 1. 에메랄드 블록 9개 -> 응축된 에메랄드 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.EMERALD_BLOCK, 9),
                    new ItemStack(ModItems.CONDENSED_EMERALD.get(), 1),
                    999, 2, 0.05F
            ));

            // 2. 응축된 에메랄드 1개 -> 에메랄드 블록 9개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.CONDENSED_EMERALD.get(), 1),
                    new ItemStack(Items.EMERALD_BLOCK, 9),
                    999, 2, 0.05F
            ));

            // 3. 조약돌 3개 -> 주민 터렛 스폰알 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.COBBLESTONE, 3),
                    new ItemStack(ModItems.VILLAGER_TURRET_SPAWN_EGG.get(), 1),
                    999, 2, 0.05F
            ));

            // 4. 조약돌 3개 -> 자원 수확 주민 터렛 스폰알 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.COBBLESTONE, 3),
                    new ItemStack(ModItems.RESOURCE_VILLAGER_SPAWN_EGG.get(), 1),
                    999, 2, 0.05F
            ));

            // 4. 조약돌 3개 -> 자원 수확 주민 터렛 스폰알 1개(쉬움버전)
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.COBBLESTONE, 3),
                    new ItemStack(ModItems.RESOURCE_VILLAGER_EASY_SPAWN_EGG.get(), 1),
                    999, 2, 0.05F
            ));

            // 5. 금 블록 1개 -> 치유 토템 터렛 스폰알 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.GOLD_BLOCK, 1),
                    new ItemStack(ModItems.HEALER_TURRET_SPAWN_EGG.get(), 1),
                    999, 2, 0.05F
            ));

            // 6. 치유 하급 토큰 9개 -> 중급 토큰 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.HEALER_POINT_TOKEN_LOW.get(), 9),
                    new ItemStack(ModItems.HEALER_POINT_TOKEN_MID.get(), 1),
                    999, 2, 0.05F
            ));

            // 7. 치유 중급 토큰 3개 -> 상급 토큰 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.HEALER_POINT_TOKEN_MID.get(), 3),
                    new ItemStack(ModItems.HEALER_POINT_TOKEN_HIGH.get(), 1),
                    999, 2, 0.05F
            ));

            // 8. 일반 하급 토큰 9개 -> 중급 토큰 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.TURRET_POINT_TOKEN_LOW.get(), 9),
                    new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 1),
                    999, 2, 0.05F
            ));

            // 9. 일반 중급 토큰 3개 -> 상급 토큰 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 3),
                    new ItemStack(ModItems.TURRET_POINT_TOKEN_HIGH.get(), 1),
                    999, 2, 0.05F
            ));

            // 10. 다이아몬드 4개 -> 삼지창 주민 터렛 스폰알 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.DIAMOND, 4),
                    new ItemStack(ModItems.TRIDENT_TURRET_SPAWN_EGG.get(), 1),
                    999, 2, 0.05F
            ));

            // 11. 삼지창 하급 토큰 9개 -> 중급 토큰 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.TRIDENT_POINT_TOKEN_LOW.get(), 9),
                    new ItemStack(ModItems.TRIDENT_POINT_TOKEN_MID.get(), 1),
                    999, 2, 0.05F
            ));

            // 12. 삼지창 중급 토큰 3개 -> 상급 토큰 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.TRIDENT_POINT_TOKEN_MID.get(), 3),
                    new ItemStack(ModItems.TRIDENT_POINT_TOKEN_HIGH.get(), 1),
                    999, 2, 0.05F
            ));

            // 13. 철 블록 1개 -> 탱커 주민 터렛 스폰알 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.IRON_BLOCK, 1),
                    new ItemStack(ModItems.TANKER_TURRET_SPAWN_EGG.get(), 1),
                    999, 2, 0.05F
            ));

            // 14. 탱커 하급 토큰 9개 -> 중급 토큰 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.TANKER_POINT_TOKEN_LOW.get(), 9),
                    new ItemStack(ModItems.TANKER_POINT_TOKEN_MID.get(), 1),
                    999, 2, 0.05F
            ));

            // 15. 탱커 중급 토큰 3개 -> 상급 토큰 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.TANKER_POINT_TOKEN_MID.get(), 3),
                    new ItemStack(ModItems.TANKER_POINT_TOKEN_HIGH.get(), 1),
                    999, 2, 0.05F
            ));

            // 16. 자수정 블록 1개 -> 번개 마법사 주민 터렛 스폰알 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.AMETHYST_BLOCK, 1),
                    new ItemStack(ModItems.LIGHTNING_WIZARD_SPAWN_EGG.get(), 1),
                    999, 2, 0.05F
            ));

            // 17. 번개 마법사 하급 토큰 9개 -> 중급 토큰 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_LOW.get(), 9),
                    new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_MID.get(), 1),
                    999, 2, 0.05F
            ));

            // 18. 번개 마법사 중급 토큰 3개 -> 상급 토큰 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_MID.get(), 3),
                    new ItemStack(ModItems.LIGHTNING_POINT_TOKEN_HIGH.get(), 1),
                    999, 2, 0.05F
            ));

            // 19. 다이아몬드 4개 -> 플라즈마 마법사 주민 터렛 스폰알 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.DIAMOND, 4),
                    new ItemStack(ModItems.PLASMA_WIZARD_SPAWN_EGG.get(), 1),
                    999, 2, 0.05F
            ));

            // 20. 플라즈마 마법사 하급 토큰 9개 -> 중급 토큰 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.PLASMA_POINT_TOKEN_LOW.get(), 9),
                    new ItemStack(ModItems.PLASMA_POINT_TOKEN_MID.get(), 1),
                    999, 2, 0.05F
            ));

            // 21. 플라즈마 마법사 중급 토큰 3개 -> 상급 토큰 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.PLASMA_POINT_TOKEN_MID.get(), 3),
                    new ItemStack(ModItems.PLASMA_POINT_TOKEN_HIGH.get(), 1),
                    999, 2, 0.05F
            ));

            // 22. 금 블록 1개 -> 주민 벌 소환사 터렛 스폰알 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.GOLD_BLOCK, 1),
                    new ItemStack(ModItems.BEE_TURRET_SPAWN_EGG.get(), 1),
                    999, 2, 0.05F
            ));

            // 23. 벌 소환사 하급 토큰 9개 -> 중급 토큰 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.BEE_POINT_TOKEN_LOW.get(), 9),
                    new ItemStack(ModItems.BEE_POINT_TOKEN_MID.get(), 1),
                    999, 2, 0.05F
            ));

            // 24. 벌 소환사 중급 토큰 3개 -> 상급 토큰 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(ModItems.BEE_POINT_TOKEN_MID.get(), 3),
                    new ItemStack(ModItems.BEE_POINT_TOKEN_HIGH.get(), 1),
                    999, 2, 0.05F
            ));

            // 25. 에메랄드 1개 -> 철 주괴 4개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.EMERALD, 1),
                    new ItemStack(Items.IRON_INGOT, 4),
                    999, 2, 0.05F
            ));

            // 26. 에메랄드 1개 -> 금 주괴 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.EMERALD, 1),
                    new ItemStack(Items.GOLD_INGOT, 1),
                    999, 2, 0.05F
            ));

            // 27. 경험치 병 4개 -> 에메랄드 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.EXPERIENCE_BOTTLE, 4),
                    new ItemStack(Items.EMERALD, 1),
                    999, 2, 0.05F
            ));

            // 28. 경험치 병 64개 -> 에메랄드 16개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.EXPERIENCE_BOTTLE, 64),
                    new ItemStack(Items.EMERALD, 16),
                    999, 2, 0.05F
            ));

            // 29. 에메랄드 4개 -> 다이아몬드 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.EMERALD, 4),
                    new ItemStack(Items.DIAMOND, 1),
                    999, 2, 0.05F
            ));

            // 30. 에메랄드 1개 -> 자수정 블록 1개
            this.offers.add(new MerchantOffer(
                    new ItemStack(Items.EMERALD, 1),
                    new ItemStack(Items.AMETHYST_BLOCK, 1),
                    999, 2, 0.05F
            ));

            // 1. 썩은 고기 32개 ➔ 에메랄드 1개
            this.offers.add(new MerchantOffer(new ItemStack(Items.ROTTEN_FLESH, 32), new ItemStack(Items.EMERALD, 1), 999, 2, 0.05F));

// 2. 에메랄드 1개 ➔ 조야약돌 16개
            this.offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.COBBLESTONE, 16), 999, 2, 0.05F));

// 3. 에메랄드 1개 ➔ 참나무 판자 16개
            this.offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.OAK_PLANKS, 16), 999, 2, 0.05F));
        }
        return this.offers;
    }

    @Override
    public void overrideOffers(@Nullable MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return this.level.isClientSide();
    }
}
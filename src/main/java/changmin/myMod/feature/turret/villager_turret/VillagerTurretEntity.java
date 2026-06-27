package changmin.myMod.feature.turret.villager_turret;

import changmin.myMod.ally.IAlly;
import changmin.myMod.feature.turret.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class VillagerTurretEntity extends PathfinderMob implements RangedAttackMob, IAlly, Merchant {

    public static final float ARROW_SPEED = 0.8F;
    public static final double ARROW_DAMAGE = 2.0D;

    private static final EntityDataAccessor<Integer> TURRET_LEVEL = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> XP = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> SPEED_LEVEL = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RECHARGE_LEVEL = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ARROW_PATTERN = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ARROW_TYPE = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT);

    private Player tradingPlayer;
    private MerchantOffers offers;
    private int attackCooldown = 0;

    public VillagerTurretEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TURRET_LEVEL, 1);
        this.entityData.define(XP, 0);
        this.entityData.define(SPEED_LEVEL, 0);
        this.entityData.define(RECHARGE_LEVEL, 0);
        this.entityData.define(ARROW_PATTERN, 0);
        this.entityData.define(ARROW_TYPE, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Zombie.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level.isClientSide) {
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive() && this.distanceToSqr(target) <= 400.0D) {
                this.getLookControl().setLookAt(target, 30.0F, 30.0F);

                if (this.attackCooldown <= 0) {
                    this.shootTarget(target);

                    int baseCooldown = 40;
                    int reduction = this.getRechargeLevel() * 5;
                    this.attackCooldown = Math.max(10, baseCooldown - reduction);
                }
            }
        }
    }

    @Override
    public void killed(ServerLevel level, LivingEntity killedEntity) {
        super.killed(level, killedEntity);
        if (killedEntity instanceof Zombie) {
            this.addXp(1);
        }
    }

    public void addXp(int amount) {
        int currentXp = this.entityData.get(XP) + amount;
        int currentLvl = this.getTurretLevel();
        int neededXp = currentLvl;

        if (currentXp >= neededXp) {
            currentXp -= neededXp;
            currentLvl++;

            this.setTurretLevel(currentLvl);
            this.entityData.set(XP, currentXp);

            this.onLevelUp(currentLvl);
        } else {
            this.entityData.set(XP, currentXp);
        }
    }

    private void onLevelUp(int newLevel) {
        double newMaxHealth = 10.0D + (newLevel - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);

        if (!this.level.isClientSide) {
            this.spawnAtLocation(this.getBoundToken(1), 0.5F);
        }
    }

    // 🆕 헬퍼: 터렛 소속 라벨링이 붙은 포인트 토큰 생성
    public ItemStack getBoundToken(int count) {
        ItemStack token = new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), count);
        CompoundTag tag = token.getOrCreateTag();
        tag.putUUID("TurretUUID", this.getUUID());

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(new TextComponent("터렛 포인트 토큰").withStyle(style -> style.withColor(0xFFD700).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("소속 터렛 ID: " + this.getId()).withStyle(style -> style.withColor(0xAAAAAA)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(String.format("위치: X=%d, Y=%d, Z=%d", this.getBlockX(), this.getBlockY(), this.getBlockZ())).withStyle(style -> style.withColor(0xAAAAAA)))));
        display.put("Lore", lore);

        tag.put("display", display);
        return token;
    }

    // 🆕 헬퍼: 상점에 띄워줄 상세 설명 전용 동적 인증서(영수증) 생성
    private ItemStack createUpgradeReceipt(String type, String title, String desc1, String desc2, String currentLvlInfo) {
        ItemStack paper = new ItemStack(Items.PAPER); // 마크 기본 종이를 영수증으로 활용
        CompoundTag tag = paper.getOrCreateTag();
        tag.putString("UpgradeType", type); // 종류 구분용 태그
        tag.putUUID("TurretUUID", this.getUUID()); // 귀속 정보 주입

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(new TextComponent(title).withStyle(style -> style.withColor(0xFF55FF).withBold(true).withItalic(false))));

        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(desc1).withStyle(style -> style.withColor(0x55FF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(desc2).withStyle(style -> style.withColor(0xFFFF55)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(currentLvlInfo).withStyle(style -> style.withColor(0x55FFFF)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent("소속 터렛 ID: " + this.getId()).withStyle(style -> style.withColor(0xAAAAAA)))));

        display.put("Lore", lore);
        tag.put("display", display);

        return paper;
    }

    // 터렛 주민 우클릭 시 즉시 거래 상점 화면 오픈
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level.isClientSide) {
            this.setTradingPlayer(player);
            this.openTradingScreen(player, this.getDisplayName(), 1);
        }
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

    // ==========================================
    // 🏪 주민 상점 인터페이스 구현부
    // ==========================================
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
            this.populateOffers();
        }
        return this.offers;
    }

    // 동적 설명서가 내장된 상점 레이아웃 구성
    private void populateOffers() {
        this.offers.clear();

        // 1. 재장전 속도 업그레이드
        ItemStack rechargeReceipt = createUpgradeReceipt("recharge", "터렛 강화 인증서: 재장전 속도",
                "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                "효과: 화살 발사 대기시간(쿨타임)이 0.25초 감소합니다.",
                "현재 강화 수준: " + this.getRechargeLevel() + " ➔ " + (this.getRechargeLevel() + 1));
        this.offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 16), rechargeReceipt, 15, 2, 0.05F));
        this.offers.add(new MerchantOffer(this.getBoundToken(1), rechargeReceipt, 15, 2, 0.05F));

        // 2. 화살 비행 속도 업그레이드
        ItemStack speedReceipt = createUpgradeReceipt("speed", "터렛 강화 인증서: 화살 비행 속도",
                "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                "효과: 화살의 날아가는 속도가 더 신속하게 증가합니다.",
                "현재 강화 수준: " + this.getSpeedLevel() + " ➔ " + (this.getSpeedLevel() + 1));
        this.offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 12), speedReceipt, 15, 2, 0.05F));
        this.offers.add(new MerchantOffer(this.getBoundToken(1), speedReceipt, 15, 2, 0.05F));

        // 3. 일직선 점사 패턴 업그레이드
        ItemStack burstReceipt = createUpgradeReceipt("burst", "터렛 기술 인증서: 일직선 속사(점사)",
                "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                "효과: 화살을 부채꼴 대신 앞을 향해 일렬 연사로 뿜어냅니다.",
                "현재 발사 형태: " + (this.getArrowPattern() == 2 ? "일직선 속사" : "기타 패턴"));
        this.offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 32), burstReceipt, 5, 5, 0.05F));
        this.offers.add(new MerchantOffer(this.getBoundToken(2), burstReceipt, 5, 5, 0.05F));

        // 4. 부채꼴 확산 패턴 업그레이드
        ItemStack fanReceipt = createUpgradeReceipt("fan", "터렛 기술 인증서: 부채꼴 확산",
                "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                "효과: 여러 줄로 화살을 부채꼴 형태로 광범위 분사합니다.",
                "현재 발사 형태: " + (this.getArrowPattern() == 1 ? "부채꼴 확산" : "기타 패턴"));
        this.offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 32), fanReceipt, 5, 5, 0.05F));
        this.offers.add(new MerchantOffer(this.getBoundToken(2), fanReceipt, 5, 5, 0.05F));

        // 5. 독 화살 업그레이드
        ItemStack poisonReceipt = createUpgradeReceipt("poison", "터렛 연금 인증서: 치명적인 독 화살",
                "■ 구매 즉시 이 터렛에 즉시 적용됩니다.",
                "효과: 모든 발사체 화살에 강력한 치명상 독을 묻힙니다.",
                "현재 장착 화살: " + (this.getArrowType() == 1 ? "독 화살" : "일반 화살"));
        this.offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 48), poisonReceipt, 5, 10, 0.05F));
        this.offers.add(new MerchantOffer(this.getBoundToken(3), poisonReceipt, 5, 10, 0.05F));
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    // 🆕 플레이어가 슬롯에서 결과물(인증서)을 꺼내 거래가 성사된 즉시 강화 연동
    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();

        if (!this.level.isClientSide) {
            ItemStack result = offer.getResult();

            // 결과물 아이템의 NBT를 추출해 구매 즉시 스탯 가산 처리
            if (result.hasTag() && result.getTag().contains("UpgradeType")) {
                String upgradeType = result.getTag().getString("UpgradeType");
                this.applyUpgradeDirectly(upgradeType);
            }
        }
    }

    // 🆕 즉각적인 강화를 실행하는 제어 메소드
    private void applyUpgradeDirectly(String type) {
        if (type.equals("recharge")) {
            this.setRechargeLevel(this.getRechargeLevel() + 1);
            if (this.tradingPlayer != null) {
                this.tradingPlayer.displayClientMessage(new TextComponent("재장전 속도 업그레이드 완료! (현재: " + this.getRechargeLevel() + "레벨)"), true);
            }
        } else if (type.equals("speed")) {
            this.setSpeedLevel(this.getSpeedLevel() + 1);
            if (this.tradingPlayer != null) {
                this.tradingPlayer.displayClientMessage(new TextComponent("화살 비행 속도 업그레이드 완료! (현재: " + this.getSpeedLevel() + "레벨)"), true);
            }
        } else if (type.equals("burst")) {
            this.setArrowPattern(2);
            if (this.tradingPlayer != null) {
                this.tradingPlayer.displayClientMessage(new TextComponent("발사 패턴 변경 완료: 일직선 속사(점사)"), true);
            }
        } else if (type.equals("fan")) {
            this.setArrowPattern(1);
            if (this.tradingPlayer != null) {
                this.tradingPlayer.displayClientMessage(new TextComponent("발사 패턴 변경 완료: 부채꼴 확산형"), true);
            }
        } else if (type.equals("poison")) {
            this.setArrowType(1);
            if (this.tradingPlayer != null) {
                this.tradingPlayer.displayClientMessage(new TextComponent("화살 특성 변경 완료: 치명적인 독 화살 탑재"), true);
            }
        }

        // 효과음 및 거래 정보 실시간 새로고침
        this.playSound(SoundEvents.ANVIL_USE, 1.0F, 1.0F);

        // 🔄 중요: 상점 품목 툴팁 갱신을 위해 상점을 재생성하고 창을 다시 호출
        this.offers = null;
        if (this.tradingPlayer != null) {
            this.openTradingScreen(this.tradingPlayer, this.getDisplayName(), this.getVillagerXp());
        }
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {}
    @Override
    public int getVillagerXp() { return 0; }
    @Override
    public void overrideXp(int xp) {}
    @Override
    public boolean showProgressBar() { return false; }

    @Override
    public net.minecraft.sounds.SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return this.level.isClientSide;
    }

    // ==========================================
    // 🏹 공격 연사 및 투사체 로직 (동일)
    // ==========================================
    private void shootTarget(LivingEntity target) {
        int pattern = this.getArrowPattern();
        int arrowCount = this.getTurretLevel();

        if (pattern == 2) {
            for (int i = 0; i < arrowCount; i++) {
                this.shootSingleArrow(target, ARROW_SPEED + (i * 0.15F), 0.0F);
            }
        } else if (pattern == 1) {
            for (int i = 0; i < arrowCount; i++) {
                float baseSpread = (float)(14 - this.level.getDifficulty().getId() * 4);
                float angleOffset = (arrowCount > 1) ? (i - (arrowCount - 1) / 2.0F) * 4.0F : 0.0F;
                this.shootSingleArrow(target, ARROW_SPEED, baseSpread + angleOffset);
            }
        } else {
            this.shootSingleArrow(target, ARROW_SPEED, (float)(14 - this.level.getDifficulty().getId() * 4));
        }

        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    private void shootSingleArrow(LivingEntity target, float speed, float spread) {
        ItemStack arrowStack;
        if (this.getArrowType() == 1) {
            arrowStack = new ItemStack(Items.TIPPED_ARROW);
            PotionUtils.setPotion(arrowStack, Potions.POISON);
        } else {
            arrowStack = new ItemStack(Items.ARROW);
        }

        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, arrowStack, 1.0F);
        arrow.setBaseDamage(ARROW_DAMAGE);
        arrow.setNoGravity(true);

        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333D) - arrow.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        float finalSpeed = speed + (this.getSpeedLevel() * 0.15F);

        arrow.shoot(d0, d1 + d3 * 0.2D, d2, finalSpeed, spread);
        this.level.addFreshEntity(arrow);
    }

    public int getTurretLevel() { return this.entityData.get(TURRET_LEVEL); }
    public void setTurretLevel(int level) { this.entityData.set(TURRET_LEVEL, level); }
    public int getXp() { return this.entityData.get(XP); }
    public int getSpeedLevel() { return this.entityData.get(SPEED_LEVEL); }
    public void setSpeedLevel(int level) { this.entityData.set(SPEED_LEVEL, level); }
    public int getRechargeLevel() { return this.entityData.get(RECHARGE_LEVEL); }
    public void setRechargeLevel(int level) { this.entityData.set(RECHARGE_LEVEL, level); }
    public int getArrowPattern() { return this.entityData.get(ARROW_PATTERN); }
    public void setArrowPattern(int pattern) { this.entityData.set(ARROW_PATTERN, pattern); }
    public int getArrowType() { return this.entityData.get(ARROW_TYPE); }
    public void setArrowType(int type) { this.entityData.set(ARROW_TYPE, type); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TurretLevel", this.getTurretLevel());
        tag.putInt("TurretXP", this.getXp());
        tag.putInt("SpeedLevel", this.getSpeedLevel());
        tag.putInt("RechargeLevel", this.getRechargeLevel());
        tag.putInt("ArrowPattern", this.getArrowPattern());
        tag.putInt("ArrowType", this.getArrowType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TurretLevel")) this.setTurretLevel(tag.getInt("TurretLevel"));
        if (tag.contains("TurretXP")) this.entityData.set(XP, tag.getInt("TurretXP"));
        if (tag.contains("SpeedLevel")) this.setSpeedLevel(tag.getInt("SpeedLevel"));
        if (tag.contains("RechargeLevel")) this.setRechargeLevel(tag.getInt("RechargeLevel"));
        if (tag.contains("ArrowPattern")) this.setArrowPattern(tag.getInt("ArrowPattern"));
        if (tag.contains("ArrowType")) this.setArrowType(tag.getInt("ArrowType"));

        double loadedMax = 10.0D + (this.getTurretLevel() - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(loadedMax);
    }

    @Override
    public Component getDisplayName() { return new TextComponent("주민 터렛"); }
    @Override
    public boolean isPushable() { return false; }
    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {}
    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {}
}
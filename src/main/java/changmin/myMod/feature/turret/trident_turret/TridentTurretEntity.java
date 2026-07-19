package changmin.myMod.feature.turret.trident_turret;

import changmin.myMod.ally.IAlly;
import changmin.myMod.feature.turret.villager_turret.TurretTargetGoal;
import changmin.myMod.registry.ModItems;
import changmin.myMod.zombieTribe.IZombieTribe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import changmin.myMod.feature.turret.trident_turret.TridentTurretTargetGoal;
import javax.annotation.Nullable;

public class TridentTurretEntity extends PathfinderMob implements RangedAttackMob, IAlly, Merchant {

    private static final EntityDataAccessor<Integer> TURRET_LEVEL = SynchedEntityData.defineId(TridentTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> XP = SynchedEntityData.defineId(TridentTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEEDED_XP = SynchedEntityData.defineId(TridentTurretEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> TRIDENT_COUNT = SynchedEntityData.defineId(TridentTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DAMAGE_LEVEL = SynchedEntityData.defineId(TridentTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RANGE_LEVEL = SynchedEntityData.defineId(TridentTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RECHARGE_LEVEL = SynchedEntityData.defineId(TridentTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIGHTNING_LEVEL = SynchedEntityData.defineId(TridentTurretEntity.class, EntityDataSerializers.INT);
    // 🆕 사격 모드 데이터 키 추가 (0: 집중 사격, 1: 부채꼴 사격)
    private static final EntityDataAccessor<Integer> SHOOT_MODE = SynchedEntityData.defineId(TridentTurretEntity.class, EntityDataSerializers.INT);

    private Player tradingPlayer;
    private MerchantOffers offers;
    private int attackCooldown = 0;

    public TridentTurretEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TURRET_LEVEL, 1);
        this.entityData.define(XP, 0);
        this.entityData.define(NEEDED_XP, 1);

        this.entityData.define(TRIDENT_COUNT, 1);
        this.entityData.define(DAMAGE_LEVEL, 0);
        this.entityData.define(RANGE_LEVEL, 0);
        this.entityData.define(RECHARGE_LEVEL, 0);
        this.entityData.define(LIGHTNING_LEVEL, 0);
        this.entityData.define(SHOOT_MODE, 0); // 기본 상태는 좁게 쏘는 집중 사격 모드
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new TridentTurretTargetGoal<>(this, LivingEntity.class));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
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
            if (target != null && target.isAlive()) {
                double distanceSqr = this.distanceToSqr(target);
                double maxRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);

                if (distanceSqr <= maxRange * maxRange) {
                    this.getLookControl().setLookAt(target, 30.0F, 30.0F);
                    if (this.attackCooldown <= 0) {
                        TridentTurretShooter.shootTarget(this, target);
                        this.attackCooldown = this.getCalculatedCooldown();
                    }
                }
            }
        }
    }

    public int getCalculatedCooldown() {
        int baseCooldown = 70;
        int reduction = this.getRechargeLevel() * 6;
        return Math.max(15, baseCooldown - reduction);
    }

    @Override
    public void killed(ServerLevel level, LivingEntity killedEntity) {
        super.killed(level, killedEntity);
        if (IZombieTribe.isZombieTribe(killedEntity)) {
            this.addXp(1);
        }
    }

    public void addXp(int amount) {
        int currentXp = this.entityData.get(XP) + amount;
        int currentLvl = this.getTurretLevel();
        int neededXp = this.getNeededXp();

        if (currentXp >= neededXp) {
            currentXp -= neededXp;
            currentLvl++;
            this.setTurretLevel(currentLvl);
            this.entityData.set(XP, currentXp);
            this.setNeededXp(neededXp + 1);
            this.onLevelUp(currentLvl);
        } else {
            this.entityData.set(XP, currentXp);
        }
    }

    private void onLevelUp(int newLevel) {
        double newMaxHealth = 15.0D + (newLevel - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 0.8F);
        this.heal((float)(newMaxHealth * 0.3F));

        if (!this.level.isClientSide) {
            this.spawnAtLocation(TridentTurretTradeManager.getBoundToken(this, 1), 0.5F);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty() && heldItem.hasTag() && heldItem.getTag().contains("UpgradeType")) {
            String upgradeType = heldItem.getTag().getString("UpgradeType");
            if (!this.level.isClientSide) {
                TridentTurretTradeManager.applyUpgradeDirectly(this, upgradeType);
                if (!player.getAbilities().instabuild) heldItem.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }

        if (!this.level.isClientSide) {
            this.setTradingPlayer(player);
            this.openTradingScreen(player, this.getDisplayName(), 1);
        }
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

    public void updateFollowRange() {
        double newRange = 16.0D + (this.getRangeLevel() * 4.0D);
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(newRange);
    }

    public int getTurretLevel() { return this.entityData.get(TURRET_LEVEL); }
    public void setTurretLevel(int level) { this.entityData.set(TURRET_LEVEL, level); }
    public int getXp() { return this.entityData.get(XP); }
    public int getNeededXp() { return this.entityData.get(NEEDED_XP); }
    public void setNeededXp(int xp) { this.entityData.set(NEEDED_XP, xp); }

    public int getTridentCount() { return this.entityData.get(TRIDENT_COUNT); }
    public void setTridentCount(int count) { this.entityData.set(TRIDENT_COUNT, count); }
    public int getDamageLevel() { return this.entityData.get(DAMAGE_LEVEL); }
    public void setDamageLevel(int level) { this.entityData.set(DAMAGE_LEVEL, level); }
    public int getRangeLevel() { return this.entityData.get(RANGE_LEVEL); }
    public void setRangeLevel(int level) { this.entityData.set(RANGE_LEVEL, level); this.updateFollowRange(); }
    public int getRechargeLevel() { return this.entityData.get(RECHARGE_LEVEL); }
    public void setRechargeLevel(int level) { this.entityData.set(RECHARGE_LEVEL, level); }
    public int getLightningLevel() { return this.entityData.get(LIGHTNING_LEVEL); }
    public void setLightningLevel(int enabled) { this.entityData.set(LIGHTNING_LEVEL, enabled); }

    // 🆕 사격 모드 게터 및 세터
    public int getShootMode() { return this.entityData.get(SHOOT_MODE); }
    public void setShootMode(int mode) { this.entityData.set(SHOOT_MODE, mode); }

    @Override public void setTradingPlayer(@Nullable Player player) { this.tradingPlayer = player; }
    @Override @Nullable public Player getTradingPlayer() { return this.tradingPlayer; }
    @Override public void overrideOffers(MerchantOffers offers) { this.offers = offers; }
    @Override public void notifyTradeUpdated(ItemStack stack) {}
    @Override public int getVillagerXp() { return 0; }
    @Override public void overrideXp(int xp) {}
    @Override public boolean showProgressBar() { return false; }
    @Override public net.minecraft.sounds.SoundEvent getNotifyTradeSound() { return SoundEvents.VILLAGER_YES; }
    @Override public boolean isClientSide() { return this.level.isClientSide; }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            TridentTurretTradeManager.populateOffers(this, this.offers);
        }
        return this.offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        if (!this.level.isClientSide && offer.getResult().hasTag()) {
            String upgradeType = offer.getResult().getTag().getString("UpgradeType");
            TridentTurretTradeManager.applyUpgradeDirectly(this, upgradeType);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TurretLevel", this.getTurretLevel());
        tag.putInt("TurretXP", this.getXp());
        tag.putInt("NeededXP", this.getNeededXp());
        tag.putInt("TridentCount", this.getTridentCount());
        tag.putInt("DamageLevel", this.getDamageLevel());
        tag.putInt("RangeLevel", this.getRangeLevel());
        tag.putInt("RechargeLevel", this.getRechargeLevel());
        tag.putInt("LightningLevel", this.getLightningLevel());
        tag.putInt("ShootMode", this.getShootMode()); // NBT 저장
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TurretLevel")) this.setTurretLevel(tag.getInt("TurretLevel"));
        if (tag.contains("TurretXP")) this.entityData.set(XP, tag.getInt("TurretXP"));
        if (tag.contains("NeededXP")) this.setNeededXp(tag.getInt("NeededXP"));
        if (tag.contains("TridentCount")) this.setTridentCount(tag.getInt("TridentCount"));
        if (tag.contains("DamageLevel")) this.setDamageLevel(tag.getInt("DamageLevel"));
        if (tag.contains("RangeLevel")) this.setRangeLevel(tag.getInt("RangeLevel"));
        if (tag.contains("RechargeLevel")) this.setRechargeLevel(tag.getInt("RechargeLevel"));
        if (tag.contains("LightningLevel")) this.setLightningLevel(tag.getInt("LightningLevel"));
        if (tag.contains("ShootMode")) this.setShootMode(tag.getInt("ShootMode")); // NBT 로드

        double loadedMax = 15.0D + (this.getTurretLevel() - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(loadedMax);
        this.updateFollowRange();
    }

    @Override public Component getDisplayName() { return new TextComponent("삼지창 주민 터렛"); }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(net.minecraft.world.entity.Entity entity) {}
    @Override public void performRangedAttack(LivingEntity target, float velocity) {}
}
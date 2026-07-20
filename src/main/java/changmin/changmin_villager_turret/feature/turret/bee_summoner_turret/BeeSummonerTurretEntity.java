package changmin.changmin_villager_turret.feature.turret.bee_summoner_turret;

import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BeeSummonerTurretEntity extends PathfinderMob implements IAlly, Merchant {

    private static final EntityDataAccessor<Integer> TURRET_LEVEL = SynchedEntityData.defineId(BeeSummonerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> XP = SynchedEntityData.defineId(BeeSummonerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEEDED_XP = SynchedEntityData.defineId(BeeSummonerTurretEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> MAX_BEES = SynchedEntityData.defineId(BeeSummonerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BEE_DAMAGE_LEVEL = SynchedEntityData.defineId(BeeSummonerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BEE_DURATION_LEVEL = SynchedEntityData.defineId(BeeSummonerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RANGE_LEVEL = SynchedEntityData.defineId(BeeSummonerTurretEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> BEE_POISON_LEVEL = SynchedEntityData.defineId(BeeSummonerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BEE_WITHER_LEVEL = SynchedEntityData.defineId(BeeSummonerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BEE_SLOWNESS_LEVEL = SynchedEntityData.defineId(BeeSummonerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BEE_WEAKNESS_LEVEL = SynchedEntityData.defineId(BeeSummonerTurretEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> BEE_COOLDOWN_LEVEL = SynchedEntityData.defineId(BeeSummonerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BEE_HEALTH_LEVEL = SynchedEntityData.defineId(BeeSummonerTurretEntity.class, EntityDataSerializers.INT);

    // 🆕 렌더러 표기를 위해 남은 쿨타임을 네트워크 상에서 동기화하는 데이터 키 추가
    private static final EntityDataAccessor<Integer> SUMMON_COOLDOWN = SynchedEntityData.defineId(BeeSummonerTurretEntity.class, EntityDataSerializers.INT);

    private Player tradingPlayer;
    private MerchantOffers offers;

    private final List<SummonedBeeEntity> activeBees = new ArrayList<>();

    public static EntityType<SummonedBeeEntity> SUMMONED_BEE_TYPE = null;

    public BeeSummonerTurretEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TURRET_LEVEL, 1);
        this.entityData.define(XP, 0);
        this.entityData.define(NEEDED_XP, 1);

        this.entityData.define(MAX_BEES, 1);
        this.entityData.define(BEE_DAMAGE_LEVEL, 0);
        this.entityData.define(BEE_DURATION_LEVEL, 0);
        this.entityData.define(RANGE_LEVEL, 0);

        this.entityData.define(BEE_POISON_LEVEL, 0);
        this.entityData.define(BEE_WITHER_LEVEL, 0);
        this.entityData.define(BEE_SLOWNESS_LEVEL, 0);
        this.entityData.define(BEE_WEAKNESS_LEVEL, 0);

        this.entityData.define(BEE_COOLDOWN_LEVEL, 0);
        this.entityData.define(BEE_HEALTH_LEVEL, 0);
        this.entityData.define(SUMMON_COOLDOWN, 0); // 🆕 동기화 쿨타임 초기화
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new RetaliateGoal(this));
        this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                this, LivingEntity.class, 10, false, false, SummonedBeeEntity::isHostile
        ));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level.isClientSide) {
            net.minecraft.world.entity.Entity attacker = source.getEntity();
            if (attacker instanceof LivingEntity livingAttacker && SummonedBeeEntity.isHostile(livingAttacker)) {
                this.broadcastAlertToBees(livingAttacker);
            }
        }
        return result;
    }

    private void broadcastAlertToBees(LivingEntity attacker) {
        for (SummonedBeeEntity bee : this.activeBees) {
            if (bee.isAlive()) {
                bee.setPriorityDefenseTarget(attacker);
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            this.activeBees.removeIf(bee -> !bee.isAlive());

            // ⏳ 서버단에서 남은 쿨타임을 감소시키고 실시간으로 네트워크 상에 업데이트
            int currentCD = this.entityData.get(SUMMON_COOLDOWN);
            if (currentCD > 0) {
                this.entityData.set(SUMMON_COOLDOWN, currentCD - 1);
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double distanceSqr = this.distanceToSqr(target);
                double maxRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);

                if (distanceSqr <= maxRange * maxRange) {
                    this.getLookControl().setLookAt(target, 30.0F, 30.0F);

                    // ⏳ 동기화 데이터의 남은 쿨타임 검사
                    if (this.entityData.get(SUMMON_COOLDOWN) <= 0 && this.activeBees.size() < this.getMaxBees()) {
                        int beesToSummon = this.getMaxBees() - this.activeBees.size();
                        for (int i = 0; i < beesToSummon; i++) {
                            this.summonBee();
                        }
                        // 쿨타임을 동기화 저장 공간에 새겨 클라이언트에게 패킷 송신
                        this.entityData.set(SUMMON_COOLDOWN, this.getCalculatedCooldown());

                        this.dealFixedDamageToNearbyHostiles();
                    }
                }
            }
        }
    }

    private void dealFixedDamageToNearbyHostiles() {
        double range = 12.0D;
        List<LivingEntity> nearbyEnemies = this.level.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(range),
                SummonedBeeEntity::isHostile
        );

        for (LivingEntity enemy : nearbyEnemies) {
            enemy.hurt(DamageSource.GENERIC, 1.0F);
            enemy.setLastHurtByMob(this);
            if (enemy instanceof Mob mob) {
                mob.setTarget(this);
            }
        }
    }

    public int getCalculatedCooldown() {
        int baseCooldown = 600;
        int reduction = this.getBeeCooldownLevel() * 100;
        return Math.max(200, baseCooldown - reduction);
    }

    private void summonBee() {
        if (SUMMONED_BEE_TYPE == null) {
            return;
        }

        SummonedBeeEntity bee = new SummonedBeeEntity(SUMMONED_BEE_TYPE, this.level);
        bee.setParentTurret(this);

        double beeMaxHealth = 10.0D + this.getBeeHealthLevel() * 2.0D;
        bee.getAttribute(Attributes.MAX_HEALTH).setBaseValue(beeMaxHealth);
        bee.setHealth((float)beeMaxHealth);

        bee.setRemainingLife(300 + this.getBeeDurationLevel() * 100);
        bee.setAttackDamage(2.0F + this.getBeeDamageLevel() * 1.5F);
        bee.setSearchRange(16.0D + this.getRangeLevel() * 4.0D);

        bee.setPoisonLevel(this.getBeePoisonLevel());
        bee.setWitherLevel(this.getBeeWitherLevel());
        bee.setSlownessLevel(this.getBeeSlownessLevel());
        bee.setWeaknessLevel(this.getBeeWeaknessLevel());

        bee.moveTo(this.getX(), this.getY() + 1.2D, this.getZ(), this.getYRot(), this.getXRot());
        this.level.addFreshEntity(bee);
        this.activeBees.add(bee);

        this.playSound(SoundEvents.BEE_LOOP_AGGRESSIVE, 1.0F, 1.2F);
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
            this.spawnAtLocation(BeeSummonerTradeManager.getBoundToken(this, 1), 0.5F);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty() && heldItem.hasTag() && heldItem.getTag().contains("UpgradeType")) {
            String upgradeType = heldItem.getTag().getString("UpgradeType");
            if (!this.level.isClientSide) {
                BeeSummonerTradeManager.applyUpgradeDirectly(this, upgradeType);
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

    public int getMaxBees() { return this.entityData.get(MAX_BEES); }
    public void setMaxBees(int count) { this.entityData.set(MAX_BEES, count); }
    public int getBeeDamageLevel() { return this.entityData.get(BEE_DAMAGE_LEVEL); }
    public void setBeeDamageLevel(int level) { this.entityData.set(BEE_DAMAGE_LEVEL, level); }
    public int getBeeDurationLevel() { return this.entityData.get(BEE_DURATION_LEVEL); }
    public void setBeeDurationLevel(int level) { this.entityData.set(BEE_DURATION_LEVEL, level); }
    public int getRangeLevel() { return this.entityData.get(RANGE_LEVEL); }
    public void setRangeLevel(int level) { this.entityData.set(RANGE_LEVEL, level); this.updateFollowRange(); }

    public int getBeePoisonLevel() { return this.entityData.get(BEE_POISON_LEVEL); }
    public void setBeePoisonLevel(int level) { this.entityData.set(BEE_POISON_LEVEL, level); }
    public int getBeeWitherLevel() { return this.entityData.get(BEE_WITHER_LEVEL); }
    public void setBeeWitherLevel(int level) { this.entityData.set(BEE_WITHER_LEVEL, level); }
    public int getBeeSlownessLevel() { return this.entityData.get(BEE_SLOWNESS_LEVEL); }
    public void setBeeSlownessLevel(int level) { this.entityData.set(BEE_SLOWNESS_LEVEL, level); }
    public int getBeeWeaknessLevel() { return this.entityData.get(BEE_WEAKNESS_LEVEL); }
    public void setBeeWeaknessLevel(int level) { this.entityData.set(BEE_WEAKNESS_LEVEL, level); }

    public int getBeeCooldownLevel() { return this.entityData.get(BEE_COOLDOWN_LEVEL); }
    public void setBeeCooldownLevel(int level) { this.entityData.set(BEE_COOLDOWN_LEVEL, level); }
    public int getBeeHealthLevel() { return this.entityData.get(BEE_HEALTH_LEVEL); }
    public void setBeeHealthLevel(int level) { this.entityData.set(BEE_HEALTH_LEVEL, level); }

    // 🆕 클라이언트 렌더용 남은 쿨타임(CD) getter 추가
    public int getSummonCooldown() { return this.entityData.get(SUMMON_COOLDOWN); }

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
            BeeSummonerTradeManager.populateOffers(this, this.offers);
        }
        return this.offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        if (!this.level.isClientSide && offer.getResult().hasTag()) {
            String upgradeType = offer.getResult().getTag().getString("UpgradeType");
            BeeSummonerTradeManager.applyUpgradeDirectly(this, upgradeType);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TurretLevel", this.getTurretLevel());
        tag.putInt("TurretXP", this.getXp());
        tag.putInt("NeededXP", this.getNeededXp());
        tag.putInt("MaxBees", this.getMaxBees());
        tag.putInt("BeeDamageLevel", this.getBeeDamageLevel());
        tag.putInt("BeeDurationLevel", this.getBeeDurationLevel());
        tag.putInt("RangeLevel", this.getRangeLevel());

        tag.putInt("BeePoisonLevel", this.getBeePoisonLevel());
        tag.putInt("BeeWitherLevel", this.getBeeWitherLevel());
        tag.putInt("BeeSlownessLevel", this.getBeeSlownessLevel());
        tag.putInt("BeeWeaknessLevel", this.getBeeWeaknessLevel());

        tag.putInt("BeeCooldownLevel", this.getBeeCooldownLevel());
        tag.putInt("BeeHealthLevel", this.getBeeHealthLevel());

        tag.putInt("SummonCooldown", this.getSummonCooldown()); // 🆕 쿨타임 데이터 저장공간 쓰기
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TurretLevel")) this.setTurretLevel(tag.getInt("TurretLevel"));
        if (tag.contains("TurretXP")) this.entityData.set(XP, tag.getInt("TurretXP"));
        if (tag.contains("NeededXP")) this.setNeededXp(tag.getInt("NeededXP"));
        if (tag.contains("MaxBees")) this.setMaxBees(tag.getInt("MaxBees"));
        if (tag.contains("BeeDamageLevel")) this.setBeeDamageLevel(tag.getInt("BeeDamageLevel"));
        if (tag.contains("BeeDurationLevel")) this.setBeeDurationLevel(tag.getInt("BeeDurationLevel"));
        if (tag.contains("RangeLevel")) this.setRangeLevel(tag.getInt("RangeLevel"));

        if (tag.contains("BeePoisonLevel")) this.setBeePoisonLevel(tag.getInt("BeePoisonLevel"));
        if (tag.contains("BeeWitherLevel")) this.setBeeWitherLevel(tag.getInt("BeeWitherLevel"));
        if (tag.contains("BeeSlownessLevel")) this.setBeeSlownessLevel(tag.getInt("BeeSlownessLevel"));
        if (tag.contains("BeeWeaknessLevel")) this.setBeeWeaknessLevel(tag.getInt("BeeWeaknessLevel"));

        if (tag.contains("BeeCooldownLevel")) this.setBeeCooldownLevel(tag.getInt("BeeCooldownLevel"));
        if (tag.contains("BeeHealthLevel")) this.setBeeHealthLevel(tag.getInt("BeeHealthLevel"));

        if (tag.contains("SummonCooldown")) this.entityData.set(SUMMON_COOLDOWN, tag.getInt("SummonCooldown")); // 🆕 쿨타임 데이터 읽기

        double loadedMax = 15.0D + (this.getTurretLevel() - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(loadedMax);
        this.updateFollowRange();
    }

    @Override public Component getDisplayName() { return new TextComponent("주민 벌 소환사 터렛"); }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(net.minecraft.world.entity.Entity entity) {}

    private static class RetaliateGoal extends net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal {
        public RetaliateGoal(BeeSummonerTurretEntity turret) {
            super(turret);
        }
        @Override
        public boolean canUse() {
            if (super.canUse()) {
                LivingEntity attacker = this.mob.getLastHurtByMob();
                return attacker != null && !IAlly.isAllyEntity(attacker);
            }
            return false;
        }
    }
}
package changmin.myMod.feature.turret.lightning_wizard;

import changmin.myMod.ally.IAlly;
import changmin.myMod.registry.ModEntityTypes;
import changmin.myMod.zombieTribe.IZombieTribe;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
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

public class LightningWizardEntity extends PathfinderMob implements IAlly, Merchant {

    private static final EntityDataAccessor<Integer> TURRET_LEVEL = SynchedEntityData.defineId(LightningWizardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> XP = SynchedEntityData.defineId(LightningWizardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEEDED_XP = SynchedEntityData.defineId(LightningWizardEntity.class, EntityDataSerializers.INT);

    // ⚡ 전용 업그레이드 연동 데이터 키
    private static final EntityDataAccessor<Integer> STUN_DURATION_LEVEL = SynchedEntityData.defineId(LightningWizardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COOLDOWN_REDUCTION_LEVEL = SynchedEntityData.defineId(LightningWizardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ULT_DAMAGE_LEVEL = SynchedEntityData.defineId(LightningWizardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ULT_DURATION_LEVEL = SynchedEntityData.defineId(LightningWizardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ULT_RANGE_LEVEL = SynchedEntityData.defineId(LightningWizardEntity.class, EntityDataSerializers.INT);

    // ⚡ 궁극기 물리적 쿨타임 및 작동 체크 데이터 키
    private static final EntityDataAccessor<Integer> ULT_COOLDOWN = SynchedEntityData.defineId(LightningWizardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ULT_ACTIVE_TIME = SynchedEntityData.defineId(LightningWizardEntity.class, EntityDataSerializers.INT);

    private Player tradingPlayer;
    private MerchantOffers offers;
    private int attackCooldown = 0;

    public LightningWizardEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TURRET_LEVEL, 1);
        this.entityData.define(XP, 0);
        this.entityData.define(NEEDED_XP, 1);

        this.entityData.define(STUN_DURATION_LEVEL, 0);
        this.entityData.define(COOLDOWN_REDUCTION_LEVEL, 0);
        this.entityData.define(ULT_DAMAGE_LEVEL, 0);
        this.entityData.define(ULT_DURATION_LEVEL, 0);
        this.entityData.define(ULT_RANGE_LEVEL, 0);

        this.entityData.define(ULT_COOLDOWN, 1200); // 궁극기 기본 60초(1200틱) 대기
        this.entityData.define(ULT_ACTIVE_TIME, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new LightningWizardTargetGoal<>(this, LivingEntity.class));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            // 쿨타임 및 궁극기 연산
            int ultActive = this.getUltActiveTime();
            int ultCD = this.getUltCooldown();

            if (ultActive > 0) {
                this.setUltActiveTime(ultActive - 1);
                tickUltimateStorm(); // 궁극기 폭격 틱 실행
            } else if (ultCD > 0) {
                this.setUltCooldown(ultCD - 1);
            }

            // 평타 및 궁극기 자동 발동 판정
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }

            // 궁극기가 활성화 중이 아닐 때만 평타 발사
            if (ultActive <= 0) {
                LivingEntity target = this.getTarget();
                if (target != null && target.isAlive()) {
                    double distanceSqr = this.distanceToSqr(target);
                    double maxRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);

                    if (distanceSqr <= maxRange * maxRange) {
                        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
                        if (this.attackCooldown <= 0) {
                            shootStunBolt(target);
                            this.attackCooldown = 50; // 기본 평타 대기시간 2.5초(50틱)
                        }
                    }
                }
            }

            // 궁극기가 충전 완료되었고 비활성 상태일 때 자동 시전 판단
            if (ultCD <= 0 && ultActive <= 0) {
                double range = 4.0D + (this.getUltRangeLevel() * 1.0D);
                boolean hasZombies = false;
                for (LivingEntity enemy : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(range))) {
                    if (IZombieTribe.isZombieTribe(enemy) && enemy.isAlive()) {
                        hasZombies = true;
                        break;
                    }
                }

                if (hasZombies) {
                    // 궁극기 가동 시작!
                    // 기본 지속시간 3초(60틱) + 레벨당 1초(20틱) (최대 10초)
                    int duration = 60 + (this.getUltDurationLevel() * 20);
                    this.setUltActiveTime(duration);
                    this.setUltCooldown(1200); // 쿨타임 60초로 리셋
// LightningWizardEntity.java 내부 수정
//SoundSource.MOBS ➔ SoundSource.NEUTRAL로 변경합니다.
                    this.level.playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.NEUTRAL, 2.0F, 0.5F);
                    this.addXp(1); // 전설 기술 시전 성공으로 경험치 1 획득
                }
            }
        }
    }

    private void shootStunBolt(LivingEntity target) {
        LightningProjectileEntity projectile = new LightningProjectileEntity(this.level, this);
        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333D) - projectile.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        projectile.shoot(d0, d1 + d3 * 0.18D, d2, 1.5F, 0.0F);
        this.level.addFreshEntity(projectile);
        this.playSound(SoundEvents.TRIDENT_THROW, 1.0F, 1.5F);
    }

    private void tickUltimateStorm() {
        if (this.tickCount % 5 == 0) { // 0.25초에 한 번씩 영역 방전 연산
            double range = 4.0D + (this.getUltRangeLevel() * 1.0D);
            double damage = 5.0D + (this.getUltDamageLevel() * 2.5D); // 기본 5데미지 + 레벨당 2.5 증가

            if (this.level instanceof ServerLevel serverLevel) {
                for (LivingEntity enemy : serverLevel.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(range))) {
                    if (IZombieTribe.isZombieTribe(enemy) && enemy.isAlive()) {
                        // 1. 좀비에게 가짜 번개(Visual Only)를 내리침 (불연산 제거로 렉 방지!)
                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                        if (bolt != null) {
                            bolt.moveTo(enemy.position());
                            bolt.setVisualOnly(true); // 시각 및 소리만 구현되고 연소 연산은 무시됨
                            serverLevel.addFreshEntity(bolt);
                        }

                        // 2. 가짜 번개 타격 대상에게 코드로 정밀 번개 데미지 적용
                        enemy.hurt(DamageSource.LIGHTNING_BOLT, (float) damage);
                    }
                }
            }
        }
    }

    public void reduceUltCooldown(int ticks) {
        int cdReduction = ticks + (this.getCooldownReductionLevel() * 20); // 레벨당 한 대 맞출 때마다 1초씩 쿨감 누적 가중치 추가
        int currentCD = this.getUltCooldown();
        if (currentCD > 0 && this.getUltActiveTime() <= 0) {
            this.setUltCooldown(Math.max(0, currentCD - cdReduction));
        }
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
            this.spawnAtLocation(LightningWizardTradeManager.getBoundToken(this, 1), 0.5F);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty() && heldItem.hasTag() && heldItem.getTag().contains("UpgradeType")) {
            String upgradeType = heldItem.getTag().getString("UpgradeType");
            if (!this.level.isClientSide) {
                LightningWizardTradeManager.applyUpgradeDirectly(this, upgradeType);
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

    public int getTurretLevel() { return this.entityData.get(TURRET_LEVEL); }
    public void setTurretLevel(int level) { this.entityData.set(TURRET_LEVEL, level); }
    public int getXp() { return this.entityData.get(XP); }
    public int getNeededXp() { return this.entityData.get(NEEDED_XP); }
    public void setNeededXp(int xp) { this.entityData.set(NEEDED_XP, xp); }

    public int getStunDurationLevel() { return this.entityData.get(STUN_DURATION_LEVEL); }
    public void setStunDurationLevel(int level) { this.entityData.set(STUN_DURATION_LEVEL, level); }
    public int getCooldownReductionLevel() { return this.entityData.get(COOLDOWN_REDUCTION_LEVEL); }
    public void setCooldownReductionLevel(int level) { this.entityData.set(COOLDOWN_REDUCTION_LEVEL, level); }
    public int getUltDamageLevel() { return this.entityData.get(ULT_DAMAGE_LEVEL); }
    public void setUltDamageLevel(int level) { this.entityData.set(ULT_DAMAGE_LEVEL, level); }
    public int getUltDurationLevel() { return this.entityData.get(ULT_DURATION_LEVEL); }
    public void setUltDurationLevel(int level) { this.entityData.set(ULT_DURATION_LEVEL, level); }
    public int getUltRangeLevel() { return this.entityData.get(ULT_RANGE_LEVEL); }
    public void setUltRangeLevel(int level) { this.entityData.set(ULT_RANGE_LEVEL, level); }

    public int getUltCooldown() { return this.entityData.get(ULT_COOLDOWN); }
    public void setUltCooldown(int cd) { this.entityData.set(ULT_COOLDOWN, cd); }
    public int getUltActiveTime() { return this.entityData.get(ULT_ACTIVE_TIME); }
    public void setUltActiveTime(int active) { this.entityData.set(ULT_ACTIVE_TIME, active); }

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
            LightningWizardTradeManager.populateOffers(this, this.offers);
        }
        return this.offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        if (!this.level.isClientSide && offer.getResult().hasTag()) {
            String upgradeType = offer.getResult().getTag().getString("UpgradeType");
            LightningWizardTradeManager.applyUpgradeDirectly(this, upgradeType);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TurretLevel", this.getTurretLevel());
        tag.putInt("TurretXP", this.getXp());
        tag.putInt("NeededXP", this.getNeededXp());
        tag.putInt("StunDurationLevel", this.getStunDurationLevel());
        tag.putInt("CooldownReductionLevel", this.getCooldownReductionLevel());
        tag.putInt("UltDamageLevel", this.getUltDamageLevel());
        tag.putInt("UltDurationLevel", this.getUltDurationLevel());
        tag.putInt("UltRangeLevel", this.getUltRangeLevel());
        tag.putInt("UltCooldown", this.getUltCooldown());
        tag.putInt("UltActiveTime", this.getUltActiveTime());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TurretLevel")) this.setTurretLevel(tag.getInt("TurretLevel"));
        if (tag.contains("TurretXP")) this.entityData.set(XP, tag.getInt("TurretXP"));
        if (tag.contains("NeededXP")) this.setNeededXp(tag.getInt("NeededXP"));
        if (tag.contains("StunDurationLevel")) this.setStunDurationLevel(tag.getInt("StunDurationLevel"));
        if (tag.contains("CooldownReductionLevel")) this.setCooldownReductionLevel(tag.getInt("CooldownReductionLevel"));
        if (tag.contains("UltDamageLevel")) this.setUltDamageLevel(tag.getInt("UltDamageLevel"));
        if (tag.contains("UltDurationLevel")) this.setUltDurationLevel(tag.getInt("UltDurationLevel"));
        if (tag.contains("UltRangeLevel")) this.setUltRangeLevel(tag.getInt("UltRangeLevel"));
        if (tag.contains("UltCooldown")) this.setUltCooldown(tag.getInt("UltCooldown"));
        if (tag.contains("UltActiveTime")) this.setUltActiveTime(tag.getInt("UltActiveTime"));

        double loadedMax = 15.0D + (this.getTurretLevel() - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(loadedMax);
    }

    @Override public Component getDisplayName() { return new TextComponent("번개 마법사 주민 터렛"); }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(net.minecraft.world.entity.Entity entity) {}
}
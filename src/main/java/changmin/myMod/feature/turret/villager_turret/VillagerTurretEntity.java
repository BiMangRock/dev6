package changmin.myMod.feature.turret.villager_turret;

import changmin.myMod.ally.IAlly;
import changmin.myMod.registry.ModItems;
import changmin.myMod.zombieTribe.IZombieTribe; // 신규 추가된 임포트 구문
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
    private static final EntityDataAccessor<Integer> NO_GRAVITY = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INVULNERABILITY_LEVEL = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HEAL_LEVEL = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEEDED_XP = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PASS_BLOCKS = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CAN_SEE_THROUGH_WALLS = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ARROW_COUNT = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT); // 전용 데이터 연동 키

    private Player tradingPlayer;
    private MerchantOffers offers;
    private int attackCooldown = 0;
    private int customInvulnTicks = 0;

    // 순차 사격 처리 변수
    private int burstArrowsLeft = 0;
    private int burstDelayTicks = 0;
    private LivingEntity burstTarget = null;
    private boolean isKnockbackBurst = false;

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
        this.entityData.define(NO_GRAVITY, 0);
        this.entityData.define(INVULNERABILITY_LEVEL, 0);
        this.entityData.define(HEAL_LEVEL, 0);
        this.entityData.define(NEEDED_XP, 1);
        this.entityData.define(PASS_BLOCKS, 0);
        this.entityData.define(CAN_SEE_THROUGH_WALLS, 0);
        this.entityData.define(ARROW_COUNT, 1); // 기본 1발 시작
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));

        // [종족 필터 통합] 특정 좀비 클래스가 아닌 모든 생명체를 후보로 잡고, TurretTargetGoal 내부 필터로 좀비 진형을 골라냅니다.
        this.targetSelector.addGoal(1, new TurretTargetGoal<>(this, LivingEntity.class));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.customInvulnTicks > 0) return false;
        boolean damageSuccess = super.hurt(source, amount);
        if (damageSuccess && this.getInvulnerabilityLevel() > 0) {
            this.customInvulnTicks = this.getInvulnerabilityLevel() * 20;
        }
        return damageSuccess;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            if (this.attackCooldown > 0) this.attackCooldown--;
            if (this.customInvulnTicks > 0) this.customInvulnTicks--;
            if (this.getHealLevel() > 0 && this.tickCount % 100 == 0) this.heal(1.0F);

            // ==========================================
            // 🏹 1. 순차 발사(연사) 루프 처리
            // ==========================================
            if (this.burstArrowsLeft > 0 && this.burstTarget != null) {
                if (this.burstTarget.isAlive() && this.distanceToSqr(this.burstTarget) <= 400.0D) {
                    this.getLookControl().setLookAt(this.burstTarget, 30.0F, 30.0F);
                    if (this.burstDelayTicks > 0) {
                        this.burstDelayTicks--;
                    }
                    if (this.burstDelayTicks <= 0) {
                        // 화살 1발 격발
                        int mode = this.isKnockbackBurst ? 3 : 2;
                        TurretShooter.shootSingleArrow(this, this.burstTarget, mode, 0.0F);
                        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));

                        this.burstArrowsLeft--;
                        if (this.burstArrowsLeft > 0) {
                            this.burstDelayTicks = 2; // 0.1초 간격 (2틱)
                        } else {
                            // 발사가 완전히 모두 종료된 시점에 쿨타임 돌입
                            this.attackCooldown = this.getCalculatedCooldown();
                            this.burstTarget = null;
                        }
                    }
                } else {
                    // 사격 중 타겟 사망 혹은 거리 이탈 시 남은 잔여 사격 취소 및 즉시 쿨타임 처리
                    this.burstArrowsLeft = 0;
                    this.burstTarget = null;
                    this.attackCooldown = this.getCalculatedCooldown();
                }
            }

            // ==========================================
            // ⚔️ 2. 신규 사격 결정 판단 루프 (순차 연사 중이 아닐 때만 판단)
            // ==========================================
            if (this.burstArrowsLeft <= 0) {
                LivingEntity target = this.getTarget();
                if (target != null && target.isAlive() && this.distanceToSqr(target) <= 400.0D) {
                    this.getLookControl().setLookAt(target, 30.0F, 30.0F);
                    if (this.attackCooldown <= 0) {
                        int pattern = this.getArrowPattern();
                        if (pattern == 2 || pattern == 3) {
                            // 속사(2) 및 넉백(3) 모드일 경우 대기 스케줄만 시작하고 화살 격발은 순차 루프가 가로챕니다.
                            this.burstArrowsLeft = this.getTurretArrowCount(); // 변경된 메소드 사용
                            this.burstDelayTicks = 0; // 첫 발은 조준 즉시 격발
                            this.burstTarget = target;
                            this.isKnockbackBurst = (pattern == 3);
                        } else {
                            // 동시 타격 모드들 [일반(0), 부채꼴(1), 강공격(4)]
                            TurretShooter.shootTarget(this, target);
                            this.attackCooldown = this.getCalculatedCooldown();
                        }
                    }
                }
            }
        }
    }

    // 기본 사격 쿨타임 계산 공식 (기본 3초 - 강화 레벨당 0.25초 감소)
    public int getCalculatedCooldown() {
        int baseCooldown = 60; // 3.0초 (60틱)
        int reduction = this.getRechargeLevel() * 5; // 레벨당 5틱(0.25초) 감소
        return Math.max(10, baseCooldown - reduction); // 최소 0.5초 쿨타임 보장
    }

    @Override
    public void killed(ServerLevel level, LivingEntity killedEntity) {
        super.killed(level, killedEntity);
        // [수정] 바닐라 좀비뿐만 아니라 IZombieTribe 구현 종족을 사살했을 때도 경험치를 정상 부여하도록 확장
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
        double newMaxHealth = 10.0D + (newLevel - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);

        if (this.getHealLevel() > 0) {
            float healPercent = 0.20F * this.getHealLevel();
            float healAmount = (float) (newMaxHealth * healPercent);
            this.heal(healAmount);
        }

        if (!this.level.isClientSide) {
            this.spawnAtLocation(TurretTradeManager.getBoundToken(this, 1), 0.5F);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty()) {
            String upgradeType = null;
            if (heldItem.is(ModItems.SPEED_UPGRADE.get())) upgradeType = "speed";
            else if (heldItem.is(ModItems.RECHARGE_UPGRADE.get())) upgradeType = "recharge";
            else if (heldItem.is(ModItems.BURST_UPGRADE.get())) upgradeType = "burst";
            else if (heldItem.is(ModItems.FAN_UPGRADE.get())) upgradeType = "fan";
            else if (heldItem.is(ModItems.POISON_ARROW_UPGRADE.get())) upgradeType = "poison";
            else if (heldItem.hasTag() && heldItem.getTag().contains("UpgradeType")) {
                upgradeType = heldItem.getTag().getString("UpgradeType");
            }

            if (upgradeType != null) {
                if (!this.level.isClientSide) {
                    TurretTradeManager.applyUpgradeDirectly(this, upgradeType);
                    if (!player.getAbilities().instabuild) heldItem.shrink(1);
                }
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
        }

        if (!this.level.isClientSide) {
            this.setTradingPlayer(player);
            this.openTradingScreen(player, this.getDisplayName(), 1);
        }
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

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
            TurretTradeManager.populateOffers(this, this.offers);
        }
        return this.offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        if (!this.level.isClientSide) {
            ItemStack result = offer.getResult();
            if (result.hasTag() && result.getTag().contains("UpgradeType")) {
                String upgradeType = result.getTag().getString("UpgradeType");
                TurretTradeManager.applyUpgradeDirectly(this, upgradeType);
            }
        }
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
    public int getNoGravityEnabled() { return this.entityData.get(NO_GRAVITY); }
    public void setNoGravityEnabled(int enabled) { this.entityData.set(NO_GRAVITY, enabled); }
    public int getInvulnerabilityLevel() { return this.entityData.get(INVULNERABILITY_LEVEL); }
    public void setInvulnerabilityLevel(int level) { this.entityData.set(INVULNERABILITY_LEVEL, level); }
    public int getHealLevel() { return this.entityData.get(HEAL_LEVEL); }
    public void setHealLevel(int level) { this.entityData.set(HEAL_LEVEL, level); }
    public int getNeededXp() { return this.entityData.get(NEEDED_XP); }
    public void setNeededXp(int xp) { this.entityData.set(NEEDED_XP, xp); }
    public int getPassBlocksEnabled() { return this.entityData.get(PASS_BLOCKS); }
    public void setPassBlocksEnabled(int enabled) { this.entityData.set(PASS_BLOCKS, enabled); }
    public int getCanSeeThroughWalls() { return this.entityData.get(CAN_SEE_THROUGH_WALLS); }
    public void setCanSeeThroughWalls(int enabled) { this.entityData.set(CAN_SEE_THROUGH_WALLS, enabled); }

    // 이름 충돌을 피하기 위해 재배치된 고유 메소드들
    public int getTurretArrowCount() { return this.entityData.get(ARROW_COUNT); }
    public void setTurretArrowCount(int count) { this.entityData.set(ARROW_COUNT, count); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TurretLevel", this.getTurretLevel());
        tag.putInt("TurretXP", this.getXp());
        tag.putInt("SpeedLevel", this.getSpeedLevel());
        tag.putInt("RechargeLevel", this.getRechargeLevel());
        tag.putInt("ArrowPattern", this.getArrowPattern());
        tag.putInt("ArrowType", this.getArrowType());
        tag.putInt("NoGravityEnabled", this.getNoGravityEnabled());
        tag.putInt("InvulnerabilityLevel", this.getInvulnerabilityLevel());
        tag.putInt("HealLevel", this.getHealLevel());
        tag.putInt("NeededXP", this.getNeededXp());
        tag.putInt("PassBlocksEnabled", this.getPassBlocksEnabled());
        tag.putInt("CanSeeThroughWalls", this.getCanSeeThroughWalls());
        tag.putInt("ArrowCount", this.getTurretArrowCount()); // 변경된 메소드 적용
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
        if (tag.contains("NoGravityEnabled")) this.setNoGravityEnabled(tag.getInt("NoGravityEnabled"));
        if (tag.contains("InvulnerabilityLevel")) this.setInvulnerabilityLevel(tag.getInt("InvulnerabilityLevel"));
        if (tag.contains("HealLevel")) this.setHealLevel(tag.getInt("HealLevel"));
        this.setNeededXp(tag.contains("NeededXP") ? tag.getInt("NeededXP") : this.getTurretLevel());
        if (tag.contains("PassBlocksEnabled")) this.setPassBlocksEnabled(tag.getInt("PassBlocksEnabled"));
        if (tag.contains("CanSeeThroughWalls")) this.setCanSeeThroughWalls(tag.getInt("CanSeeThroughWalls"));
        if (tag.contains("ArrowCount")) this.setTurretArrowCount(tag.getInt("ArrowCount")); // 변경된 메소드 적용

        double loadedMax = 10.0D + (this.getTurretLevel() - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(loadedMax);
    }

    @Override public Component getDisplayName() { return new TextComponent("주민 터렛"); }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(net.minecraft.world.entity.Entity entity) {}
    @Override public void performRangedAttack(LivingEntity target, float velocity) {}
}
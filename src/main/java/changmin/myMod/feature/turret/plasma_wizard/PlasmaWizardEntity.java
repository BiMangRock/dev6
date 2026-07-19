package changmin.myMod.feature.turret.plasma_wizard;

import changmin.myMod.ally.IAlly;
import changmin.myMod.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
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

public class PlasmaWizardEntity extends PathfinderMob implements IAlly, Merchant {

    private static final EntityDataAccessor<Integer> TURRET_LEVEL = SynchedEntityData.defineId(PlasmaWizardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> XP = SynchedEntityData.defineId(PlasmaWizardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEEDED_XP = SynchedEntityData.defineId(PlasmaWizardEntity.class, EntityDataSerializers.INT);

    // 핵심 업그레이드 전용 패킷 선언
    private static final EntityDataAccessor<Integer> COOLDOWN_LEVEL = SynchedEntityData.defineId(PlasmaWizardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SIZE_LEVEL = SynchedEntityData.defineId(PlasmaWizardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DAMAGE_LEVEL = SynchedEntityData.defineId(PlasmaWizardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STUN_LEVEL = SynchedEntityData.defineId(PlasmaWizardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPLIT_SHOT_LEVEL = SynchedEntityData.defineId(PlasmaWizardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CURRENT_COOLDOWN = SynchedEntityData.defineId(PlasmaWizardEntity.class, EntityDataSerializers.INT);

    private Player tradingPlayer;
    private MerchantOffers offers;

    public PlasmaWizardEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TURRET_LEVEL, 1);
        this.entityData.define(XP, 0);
        this.entityData.define(NEEDED_XP, 20);

        this.entityData.define(COOLDOWN_LEVEL, 0);
        this.entityData.define(SIZE_LEVEL, 0);
        this.entityData.define(DAMAGE_LEVEL, 0);
        this.entityData.define(STUN_LEVEL, 0);
        this.entityData.define(SPLIT_SHOT_LEVEL, 0);
        this.entityData.define(CURRENT_COOLDOWN, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new PlasmaWizardTargetGoal<>(this, LivingEntity.class));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            if (this.getCurrentCooldown() > 0) {
                this.setCurrentCooldown(this.getCurrentCooldown() - 1);
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double distanceSqr = this.distanceToSqr(target);
                double maxRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);

                if (distanceSqr <= maxRange * maxRange) {
                    this.getLookControl().setLookAt(target, 30.0F, 30.0F);
                    if (this.getCurrentCooldown() <= 0) {
                        shootPlasmaOrb(target);
                        this.setCurrentCooldown(this.getCalculatedCooldown());
                    }
                }
            }
        }
    }

    private void shootPlasmaOrb(LivingEntity target) {
        int splitLvl = this.getSplitShotLevel();

        if (splitLvl == 0) {
            // 기본 상태: 1발 직선 사격
            shootRotatedOrb(target, 0.0D);
        } else if (splitLvl == 1) {
            // 다중 발사 1단계: 양옆으로 벌려 2발 사격
            shootRotatedOrb(target, -10.0D);
            shootRotatedOrb(target, 10.0D);
        } else if (splitLvl == 2) {
            // 다중 발사 2단계: 삼각 대형으로 3발 사격
            shootRotatedOrb(target, -15.0D);
            shootRotatedOrb(target, 0.0D);
            shootRotatedOrb(target, 15.0D);
        }

        this.playSound(SoundEvents.TRIDENT_THROW, 1.0F, 0.8F);
    }

    private void shootRotatedOrb(LivingEntity target, double angleDegrees) {
        PlasmaOrbEntity projectile = new PlasmaOrbEntity(this.level, this);
        projectile.setDamage(this.getCalculatedDamage());
        projectile.setStunDuration(this.getCalculatedStunDuration());
        projectile.setOrbScale(this.getCalculatedScale());

        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.5D) - projectile.getY();
        double d2 = target.getZ() - this.getZ();

        // 💡 삼각함수 좌표 평면 회전 변환으로 부채꼴 궤적 연산
        if (angleDegrees != 0.0D) {
            double angleRad = Math.toRadians(angleDegrees);
            double rotatedX = d0 * Math.cos(angleRad) - d2 * Math.sin(angleRad);
            double rotatedZ = d0 * Math.sin(angleRad) + d2 * Math.cos(angleRad);
            projectile.shoot(rotatedX, d1, rotatedZ, 0.375F, 0.0F);
        } else {
            projectile.shoot(d0, d1, d2, 0.375F, 0.0F);
        }

        this.level.addFreshEntity(projectile);
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
            this.setNeededXp(20 + (currentLvl - 1) * 10);
            this.onLevelUp(currentLvl);
        } else {
            this.entityData.set(XP, currentXp);
        }
    }

    private void onLevelUp(int newLevel) {
        double newMaxHealth = 20.0D + (newLevel - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        this.setHealth((float) newMaxHealth);

        this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 0.8F);

        if (!this.level.isClientSide) {
            // 하급 티켓 드롭 개수 정밀 계산
            int baseTickets = newLevel <= 4 ? 1 : (newLevel <= 7 ? 2 : 4 + (int) ((newLevel - 8) * 0.2F));
            if (this.random.nextFloat() < 0.10F) { // 10% 잭팟 보너스
                baseTickets = (int) Math.ceil(baseTickets * 1.5);
                this.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 1.3F);
            }

            // 하급 철조각 토큰 드롭 (티어 코드 0 전달)
            this.spawnAtLocation(PlasmaWizardTradeManager.getBoundToken(this, baseTickets, 0), 0.5F);

            if (this.level instanceof ServerLevel serverLevel) {
                ExperienceOrb.award(serverLevel, this.position(), newLevel * 50);
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY(1.0D), this.getZ(), 45, 0.5D, 0.5D, 0.5D, 0.15D);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty() && heldItem.hasTag() && heldItem.getTag().contains("UpgradeType")) {
            String allowedType = heldItem.getTag().getString("TurretType");
            if (allowedType.equals(this.getType().getRegistryName().toString())) {
                String upgradeType = heldItem.getTag().getString("UpgradeType");
                if (!this.level.isClientSide) {
                    PlasmaWizardTradeManager.applyUpgradeDirectly(this, upgradeType);
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

    // 업그레이드 연산 관련 보정 공식들
    public int getCalculatedCooldown() {
        int baseCd = 50; // 기본 2.5초 (50틱)
        int finalCd = Math.max(15, baseCd - (this.getCooldownLevel() * 2)); // 레벨당 0.1초 단축 (최소 0.75초)
        return finalCd;
    }

    public float getCalculatedScale() {
        return 1.0F + (this.getSizeLevel() * 0.25F); // 레벨당 크기 25% 복리 스케일업
    }

    public float getCalculatedDamage() {
        return 5.0F + (this.getDamageLevel() * 1.5F); // 레벨당 대미지 +1.5 강화
    }

    public int getCalculatedStunDuration() {
        return 100 + (this.getStunLevel() * 20); // 레벨당 기절 지속 시간 1.0초 증가
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
            PlasmaWizardTradeManager.populateOffers(this, this.offers);
        }
        return this.offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        if (!this.level.isClientSide && offer.getResult().hasTag() && offer.getResult().getTag().contains("UpgradeType")) {
            PlasmaWizardTradeManager.applyUpgradeDirectly(this, offer.getResult().getTag().getString("UpgradeType"));
        }
    }

    public int getTurretLevel() { return this.entityData.get(TURRET_LEVEL); }
    public void setTurretLevel(int lvl) { this.entityData.set(TURRET_LEVEL, lvl); }
    public int getXp() { return this.entityData.get(XP); }
    public int getNeededXp() { return this.entityData.get(NEEDED_XP); }
    public void setNeededXp(int xp) { this.entityData.set(NEEDED_XP, xp); }

    public int getCooldownLevel() { return this.entityData.get(COOLDOWN_LEVEL); }
    public void setCooldownLevel(int lvl) { this.entityData.set(COOLDOWN_LEVEL, lvl); }
    public int getSizeLevel() { return this.entityData.get(SIZE_LEVEL); }
    public void setSizeLevel(int lvl) { this.entityData.set(SIZE_LEVEL, lvl); }
    public int getDamageLevel() { return this.entityData.get(DAMAGE_LEVEL); }
    public void setDamageLevel(int lvl) { this.entityData.set(DAMAGE_LEVEL, lvl); }
    public int getStunLevel() { return this.entityData.get(STUN_LEVEL); }
    public void setStunLevel(int lvl) { this.entityData.set(STUN_LEVEL, lvl); }
    public int getSplitShotLevel() { return this.entityData.get(SPLIT_SHOT_LEVEL); }
    public void setSplitShotLevel(int lvl) { this.entityData.set(SPLIT_SHOT_LEVEL, lvl); }
    public int getCurrentCooldown() { return this.entityData.get(CURRENT_COOLDOWN); }
    public void setCurrentCooldown(int ticks) { this.entityData.set(CURRENT_COOLDOWN, ticks); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TurretLevel", this.getTurretLevel());
        tag.putInt("TurretXP", this.getXp());
        tag.putInt("NeededXP", this.getNeededXp());
        tag.putInt("CooldownLevel", this.getCooldownLevel());
        tag.putInt("SizeLevel", this.getSizeLevel());
        tag.putInt("DamageLevel", this.getDamageLevel());
        tag.putInt("StunLevel", this.getStunLevel());
        tag.putInt("SplitShotLevel", this.getSplitShotLevel());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TurretLevel")) this.setTurretLevel(tag.getInt("TurretLevel"));
        if (tag.contains("TurretXP")) this.entityData.set(XP, tag.getInt("TurretXP"));
        if (tag.contains("NeededXP")) this.setNeededXp(tag.getInt("NeededXP"));
        if (tag.contains("CooldownLevel")) this.setCooldownLevel(tag.getInt("CooldownLevel"));
        if (tag.contains("SizeLevel")) this.setSizeLevel(tag.getInt("SizeLevel"));
        if (tag.contains("DamageLevel")) this.setDamageLevel(tag.getInt("DamageLevel"));
        if (tag.contains("StunLevel")) this.setStunLevel(tag.getInt("StunLevel"));
        if (tag.contains("SplitShotLevel")) this.setSplitShotLevel(tag.getInt("SplitShotLevel"));

        double loadedMax = 20.0D + (this.getTurretLevel() - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(loadedMax);
    }

    @Override public Component getDisplayName() { return new TextComponent("플라즈마 마법사 주민 터렛"); }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(net.minecraft.world.entity.Entity entity) {}
}
package changmin.myMod.feature.turret.healer;

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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public class HealerTurretEntity extends PathfinderMob implements IAlly, Merchant {

    private static final EntityDataAccessor<Integer> TURRET_LEVEL = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> XP = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> HEAL_AMOUNT = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COOLDOWN_LEVEL = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RANGE_LEVEL = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEEDED_XP = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AOE_HEAL_ENABLED = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLEANSE_ENABLED = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);

    private Player tradingPlayer;
    private MerchantOffers offers;
    private int healCooldown = 0;
    private LivingEntity healingTarget = null;

    public HealerTurretEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TURRET_LEVEL, 1);
        this.entityData.define(XP, 0);
        this.entityData.define(HEAL_AMOUNT, 6.0F); // 기본 치유량: 하트 3개 (6 HP)
        this.entityData.define(COOLDOWN_LEVEL, 0);  // 쿨타임 레벨 (0부터 시작)
        this.entityData.define(RANGE_LEVEL, 0);     // 사거리 레벨 (0부터 시작)
        this.entityData.define(NEEDED_XP, 20);      // 1레벨업에 필요한 치유량: 하트 10개 (20 HP)
        this.entityData.define(AOE_HEAL_ENABLED, 0);
        this.entityData.define(CLEANSE_ENABLED, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D) // 기본 터렛 체력과 동일하게 설정
                .add(Attributes.MOVEMENT_SPEED, 0.0D) // 설치형이므로 이동 속도 0
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 자가 면역 등의 추가 방어 로직이 필요하다면 여기에 작성 가능합니다.
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            if (this.healCooldown > 0) {
                this.healCooldown--;
            }

            // 1. 타겟팅 로직 (최적화된 AABB 방식)
            findHealingTarget();

            // 2. 머리 회전 제어
            if (this.healingTarget != null) {
                this.getLookControl().setLookAt(this.healingTarget, 30.0F, 30.0F);
            }

            // 3. 치유 실행 판단
            if (this.healCooldown <= 0) {
                if (this.getAoeHealEnabled() == 1) {
                    performAoeHeal();
                } else if (this.healingTarget != null) {
                    performSingleHeal(this.healingTarget);
                }
            }
        }
    }

    private void findHealingTarget() {
        double range = 8.0D + (this.getRangeLevel() * 1.0D);
        AABB box = this.getBoundingBox().inflate(range);
        List<LivingEntity> entities = this.level.getEntitiesOfClass(LivingEntity.class, box);

        LivingEntity bestTarget = null;
        double closestDist = Double.MAX_VALUE;

        // 1순위: 플레이어 우선 감지 (부상당한 플레이어만)
        for (LivingEntity entity : entities) {
            if (entity instanceof Player && entity.isAlive() && entity.getHealth() < entity.getMaxHealth()) {
                double dist = this.distanceToSqr(entity);
                if (dist < closestDist) {
                    closestDist = dist;
                    bestTarget = entity;
                }
            }
        }

        // 2순위: 범위 내 가장 가까운 부상당한 아군(IAlly) 감지
        if (bestTarget == null) {
            closestDist = Double.MAX_VALUE;
            for (LivingEntity entity : entities) {
                if (entity != this && entity.isAlive() && this.isAllyWith(entity) && entity.getHealth() < entity.getMaxHealth()) {
                    double dist = this.distanceToSqr(entity);
                    if (dist < closestDist) {
                        closestDist = dist;
                        bestTarget = entity;
                    }
                }
            }
        }

        this.healingTarget = bestTarget;
    }

    private void performSingleHeal(LivingEntity target) {
        float restored = healEntity(target);
        if (restored > 0) {
            this.addXp((int) restored);
            spawnHealEffects(target);
            this.healCooldown = getCalculatedCooldown();
        }
    }

    private void performAoeHeal() {
        double range = 8.0D + (this.getRangeLevel() * 1.0D);
        AABB box = this.getBoundingBox().inflate(range);
        List<LivingEntity> entities = this.level.getEntitiesOfClass(LivingEntity.class, box);

        float totalRestored = 0.0F;
        boolean healedAtLeastOne = false;

        for (LivingEntity entity : entities) {
            if (entity.isAlive() && (entity instanceof Player || this.isAllyWith(entity))) {
                if (entity.getHealth() < entity.getMaxHealth()) {
                    float restored = healEntity(entity);
                    if (restored > 0) {
                        totalRestored += restored;
                        spawnHealEffects(entity);
                        healedAtLeastOne = true;
                    }
                }
            }
        }

        if (healedAtLeastOne) {
            this.addXp((int) totalRestored);
            this.healCooldown = getCalculatedCooldown();
        }
    }

    private float healEntity(LivingEntity entity) {
        float currentHealth = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float healAmount = this.getHealAmount();
        float actualHealed = Math.min(healAmount, maxHealth - currentHealth);

        if (actualHealed > 0) {
            entity.heal(actualHealed);

            // 디버프 정화 특성이 켜져있을 경우 해로운 효과 제거
            if (this.getCleanseEnabled() == 1) {
                entity.removeEffect(MobEffects.POISON);
                entity.removeEffect(MobEffects.WITHER);
                entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                entity.removeEffect(MobEffects.WEAKNESS);
            }
        }
        return actualHealed;
    }

    private void spawnHealEffects(LivingEntity entity) {
        if (this.level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART, entity.getX(), entity.getY(0.5D), entity.getZ(), 5, 0.3D, 0.3D, 0.3D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY(0.5D), entity.getZ(), 8, 0.4D, 0.4D, 0.4D, 0.02D);
        }
        this.playSound(SoundEvents.VILLAGER_WORK_CLERIC, 1.0F, 1.2F + this.random.nextFloat() * 0.3F);
    }

    public int getCalculatedCooldown() {
        int baseCooldown = 200; // 기본 10초 (200틱)
        int reduction = this.getCooldownLevel() * 10; // 레벨당 0.5초(10틱) 감소
        return Math.max(20, baseCooldown - reduction); // 최소 1초(20틱) 보장
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

            // 레벨업 난이도 곡선 공식 설계 (하트 요구치: 10 -> 15 -> 20 -> 25 ...)
            // HP 경험치 환산: 20 -> 30 -> 40 -> 50 ...
            int nextNeededHearts = 10 + (currentLvl - 1) * 5;
            this.setNeededXp(nextNeededHearts * 2);

            this.onLevelUp(currentLvl);
        } else {
            this.entityData.set(XP, currentXp);
        }
    }

    private void onLevelUp(int newLevel) {
        // 레벨업당 최대 체력 5씩 상승
        double newMaxHealth = 10.0D + (newLevel - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        this.setHealth((float) newMaxHealth); // 레벨업 시 완치 보너스

        this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 0.8F);

        if (!this.level.isClientSide) {
            // 본인 엔티티 형식의 ID가 박힌 공유 토큰 드랍 (동일 종류 치유 토템 상호 교환 가능)
            this.spawnAtLocation(HealerTradeManager.getBoundToken(this, 1), 0.5F);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty()) {
            String upgradeType = null;
            if (heldItem.hasTag() && heldItem.getTag().contains("UpgradeType")) {
                // 토큰에 바인딩된 터렛 종류가 일치해야 상호작용 가능
                String allowedType = heldItem.getTag().getString("TurretType");
                if (allowedType.equals(this.getType().getRegistryName().toString())) {
                    upgradeType = heldItem.getTag().getString("UpgradeType");
                }
            }

            if (upgradeType != null) {
                if (!this.level.isClientSide) {
                    HealerTradeManager.applyUpgradeDirectly(this, upgradeType);
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
            HealerTradeManager.populateOffers(this, this.offers);
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
                HealerTradeManager.applyUpgradeDirectly(this, upgradeType);
            }
        }
    }

    // --- 동적 동기화 게터 및 세터들 ---
    public int getTurretLevel() { return this.entityData.get(TURRET_LEVEL); }
    public void setTurretLevel(int level) { this.entityData.set(TURRET_LEVEL, level); }
    public int getXp() { return this.entityData.get(XP); }
    public float getHealAmount() { return this.entityData.get(HEAL_AMOUNT); }
    public void setHealAmount(float amount) { this.entityData.set(HEAL_AMOUNT, amount); }
    public int getCooldownLevel() { return this.entityData.get(COOLDOWN_LEVEL); }
    public void setCooldownLevel(int level) { this.entityData.set(COOLDOWN_LEVEL, level); }
    public int getRangeLevel() { return this.entityData.get(RANGE_LEVEL); }
    public void setRangeLevel(int level) { this.entityData.set(RANGE_LEVEL, level); }
    public int getNeededXp() { return this.entityData.get(NEEDED_XP); }
    public void setNeededXp(int xp) { this.entityData.set(NEEDED_XP, xp); }
    public int getAoeHealEnabled() { return this.entityData.get(AOE_HEAL_ENABLED); }
    public void setAoeHealEnabled(int enabled) { this.entityData.set(AOE_HEAL_ENABLED, enabled); }
    public int getCleanseEnabled() { return this.entityData.get(CLEANSE_ENABLED); }
    public void setCleanseEnabled(int enabled) { this.entityData.set(CLEANSE_ENABLED, enabled); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TurretLevel", this.getTurretLevel());
        tag.putInt("TurretXP", this.getXp());
        tag.putFloat("HealAmount", this.getHealAmount());
        tag.putInt("CooldownLevel", this.getCooldownLevel());
        tag.putInt("RangeLevel", this.getRangeLevel());
        tag.putInt("NeededXP", this.getNeededXp());
        tag.putInt("AoeHealEnabled", this.getAoeHealEnabled());
        tag.putInt("CleanseEnabled", this.getCleanseEnabled());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TurretLevel")) this.setTurretLevel(tag.getInt("TurretLevel"));
        if (tag.contains("TurretXP")) this.entityData.set(XP, tag.getInt("TurretXP"));
        if (tag.contains("HealAmount")) this.setHealAmount(tag.getFloat("HealAmount"));
        if (tag.contains("CooldownLevel")) this.setCooldownLevel(tag.getInt("CooldownLevel"));
        if (tag.contains("RangeLevel")) this.setRangeLevel(tag.getInt("RangeLevel"));
        if (tag.contains("NeededXP")) this.setNeededXp(tag.getInt("NeededXP"));
        if (tag.contains("AoeHealEnabled")) this.setAoeHealEnabled(tag.getInt("AoeHealEnabled"));
        if (tag.contains("CleanseEnabled")) this.setCleanseEnabled(tag.getInt("CleanseEnabled"));

        double loadedMax = 10.0D + (this.getTurretLevel() - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(loadedMax);
    }

    @Override public Component getDisplayName() { return new TextComponent("치유 토템 터렛"); }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(net.minecraft.world.entity.Entity entity) {}
}
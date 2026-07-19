package changmin.myMod.feature.turret.tanker;

import changmin.myMod.ally.IAlly;
import changmin.myMod.zombieTribe.IZombieTribe;
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

public class TankerTurretEntity extends PathfinderMob implements IAlly, Merchant {

    private static final EntityDataAccessor<Integer> TURRET_LEVEL = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> XP = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEEDED_XP = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.INT);

    // 업그레이드 요소 데이터 등록
    private static final EntityDataAccessor<Integer> TAUNT_DURATION_LEVEL = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> REDUCTION_LEVEL = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> THORNS_LEVEL = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RECHARGE_LEVEL = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_TAUNTING = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.BOOLEAN);

    private Player tradingPlayer;
    private MerchantOffers offers;

    private int tauntCooldown = 0;
    private int tauntDuration = 0;

    public TankerTurretEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TURRET_LEVEL, 1);
        this.entityData.define(XP, 0);
        this.entityData.define(NEEDED_XP, 1);

        this.entityData.define(TAUNT_DURATION_LEVEL, 0);
        this.entityData.define(REDUCTION_LEVEL, 0);
        this.entityData.define(THORNS_LEVEL, 0);
        this.entityData.define(RECHARGE_LEVEL, 0);
        this.entityData.define(IS_TAUNTING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        // 주변 좀비들을 시각적으로 쳐다보며 조준하기 위한 전용 타겟 AI 적용
        this.targetSelector.addGoal(1, new TankerTurretTargetGoal<>(this, LivingEntity.class));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D) // 기본 체력을 화살 터렛의 2배인 20(하트 10칸)으로 묵직하게 설정
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 12.0D) // 도발 범위에 맞춰 조준 시야를 12블록으로 지정
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D); // 넉백에 완전히 면역
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level.isClientSide) {
            // 🛡️ 1. 도발 중 피해량 감소 연산 처리
            if (this.getIsTaunting()) {
                float reduction = 0.10F + (this.getReductionLevel() * 0.05F);
                reduction = Math.min(0.70F, reduction); // 밸런스 상한선인 최대 70% 제한 적용
                amount *= (1.0F - reduction);
            }

            // 🛡️ 2. 가시 갑옷 반사 타격 연산 (근접 몬스터에게 적용)
            if (this.getThornsLevel() > 0 && source.getDirectEntity() instanceof LivingEntity attacker) {
                // [기획 요구사항] 현재 가지고 있는 실시간 체력(Current HP)에 비례하여 (2% + 레벨당 1%) 고정 피해 반사
                double reflectDamage = this.getHealth() * (0.02D + (this.getThornsLevel() * 0.01D));
                attacker.hurt(DamageSource.thorns(this), (float) reflectDamage);

                // 가시 반사 비주얼 파티클 및 쇳소리 사운드 가미
                if (this.level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, attacker.getX(), attacker.getY(0.5D), attacker.getZ(), 8, 0.1D, 0.1D, 0.1D, 0.15D);
                    serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, attacker.getX(), attacker.getY(0.5D), attacker.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
                this.playSound(SoundEvents.ANVIL_HIT, 0.6F, 1.4F);
            }

            // 🛡️ 3. [탱커 전용 성장 시스템] 피격 시 20% 확률로 경험치 1 획득 (좀비에게 피격되었을 때만 작동)
            if (source.getEntity() instanceof LivingEntity livingAttacker) {
                if (this.random.nextFloat() < 0.20F && IZombieTribe.isZombieTribe(livingAttacker)) {
                    this.addXp(1);
                }
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            if (this.tauntCooldown > 0) {
                this.tauntCooldown--;
            }

            // 📢 도발 상태인 경우 매 틱 범위 어그로 강제 고정 연산
            if (this.tauntDuration > 0) {
                this.tauntDuration--;

                if (this.level instanceof ServerLevel serverLevel) {
                    for (Mob enemy : serverLevel.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(10.0D))) {
                        if (IZombieTribe.isZombieTribe(enemy)) {
                            enemy.setTarget(this); // 좀비의 공격 타겟을 강제로 나로 고정

                            // 좀비 머리 위에 화난 주민(분노) 빨간색 번개구름 파티클 소환
                            if (this.tickCount % 6 == 0) {
                                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, enemy.getX(), enemy.getY() + enemy.getBbHeight() + 0.3D, enemy.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                            }
                        }
                    }
                }

                if (this.tauntDuration == 0) {
                    this.setIsTaunting(false);
                }
            }

            // 📢 쿨타임 만료 시 도발 자동 시전 판단
            if (this.tauntCooldown <= 0 && this.tauntDuration <= 0) {
                boolean hasZombies = false;
                for (Mob enemy : this.level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(10.0D))) {
                    if (IZombieTribe.isZombieTribe(enemy)) {
                        hasZombies = true;
                        break;
                    }
                }

                if (hasZombies) {
                    this.setIsTaunting(true);
                    // 도발 지속 시간: 기본 4초(80틱) + 레벨당 1초(20틱)
                    this.tauntDuration = 80 + (this.getTauntDurationLevel() * 20);
                    // 도발 쿨타임: 기본 10초(200틱) - 레벨당 0.5초(10틱) 단축 (최소 5초 제한)
                    this.tauntCooldown = Math.max(100, 200 - (this.getRechargeLevel() * 10));

                    this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.5F, 0.5F); // 철골렘의 묵직한 포효 사운드 출력

                    // 📢 [탱커 전용 성장 시스템] 도발 성공 시 추가 경험치 1 보너스 획득
                    this.addXp(1);

                    // 주황빛 마그마/불꽃 파동 충격파 링 연출
                    if (this.level instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 360; i += 15) {
                            double rad = Math.toRadians(i);
                            double px = this.getX() + Math.cos(rad) * 1.5D;
                            double pz = this.getZ() + Math.sin(rad) * 1.5D;
                            serverLevel.sendParticles(ParticleTypes.FLAME, px, this.getY() + 0.2D, pz, 1, 0.0D, 0.05D, 0.0D, 0.02D);
                        }
                    }
                }
            }
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
        // [기획 요구사항] 다른 주민 터렛과 동일하게 레벨업 시 최대 체력이 5씩 증가하도록 적용
        double newMaxHealth = 20.0D + (newLevel - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        this.playSound(SoundEvents.PLAYER_LEVELUP, 1.2F, 0.5F);
        this.heal((float)(newMaxHealth * 0.4F)); // 레벨업 성공 시 즉시 체력 40% 자가 복구 보너스

        if (!this.level.isClientSide) {
            this.spawnAtLocation(TankerTurretTradeManager.getBoundToken(this, 1), 0.5F);
        }
    }

    // 🆕 [추가된 부분] 플레이어가 엔티티를 우클릭했을 때 발생하는 상호작용 처리 메서드
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        // 업그레이드 전용 종이를 들고 있을 때 바로 소모하여 업그레이드하는 기능
        if (!heldItem.isEmpty() && heldItem.hasTag() && heldItem.getTag().contains("UpgradeType")) {
            String upgradeType = heldItem.getTag().getString("UpgradeType");
            if (!this.level.isClientSide) {
                TankerTurretTradeManager.applyUpgradeDirectly(this, upgradeType);
                if (!player.getAbilities().instabuild) heldItem.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }

        // 일반 손이나 일반 아이템 상태에서 우클릭 시 거래 화면을 표시
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

    public int getTauntDurationLevel() { return this.entityData.get(TAUNT_DURATION_LEVEL); }
    public void setTauntDurationLevel(int level) { this.entityData.set(TAUNT_DURATION_LEVEL, level); }
    public int getReductionLevel() { return this.entityData.get(REDUCTION_LEVEL); }
    public void setReductionLevel(int level) { this.entityData.set(REDUCTION_LEVEL, level); }
    public int getThornsLevel() { return this.entityData.get(THORNS_LEVEL); }
    public void setThornsLevel(int level) { this.entityData.set(THORNS_LEVEL, level); }
    public int getRechargeLevel() { return this.entityData.get(RECHARGE_LEVEL); }
    public void setRechargeLevel(int level) { this.entityData.set(RECHARGE_LEVEL, level); }

    public boolean getIsTaunting() { return this.entityData.get(IS_TAUNTING); }
    public void setIsTaunting(boolean taunting) { this.entityData.set(IS_TAUNTING, taunting); }

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
            TankerTurretTradeManager.populateOffers(this, this.offers);
        }
        return this.offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        if (!this.level.isClientSide && offer.getResult().hasTag()) {
            String upgradeType = offer.getResult().getTag().getString("UpgradeType");
            TankerTurretTradeManager.applyUpgradeDirectly(this, upgradeType);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TurretLevel", this.getTurretLevel());
        tag.putInt("TurretXP", this.getXp());
        tag.putInt("NeededXP", this.getNeededXp());
        tag.putInt("TauntDurationLevel", this.getTauntDurationLevel());
        tag.putInt("ReductionLevel", this.getReductionLevel());
        tag.putInt("ThornsLevel", this.getThornsLevel());
        tag.putInt("RechargeLevel", this.getRechargeLevel());
        tag.putBoolean("IsTaunting", this.getIsTaunting());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TurretLevel")) this.setTurretLevel(tag.getInt("TurretLevel"));
        if (tag.contains("TurretXP")) this.entityData.set(XP, tag.getInt("TurretXP"));
        if (tag.contains("NeededXP")) this.setNeededXp(tag.getInt("NeededXP"));
        if (tag.contains("TauntDurationLevel")) this.setTauntDurationLevel(tag.getInt("TauntDurationLevel"));
        if (tag.contains("ReductionLevel")) this.setReductionLevel(tag.getInt("ReductionLevel"));
        if (tag.contains("ThornsLevel")) this.setThornsLevel(tag.getInt("ThornsLevel"));
        if (tag.contains("RechargeLevel")) this.setRechargeLevel(tag.getInt("RechargeLevel"));
        if (tag.contains("IsTaunting")) this.setIsTaunting(tag.getBoolean("IsTaunting"));

        double loadedMax = 20.0D + (this.getTurretLevel() - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(loadedMax);
    }

    @Override public Component getDisplayName() { return new TextComponent("탱커 주민 터렛"); }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(net.minecraft.world.entity.Entity entity) {}
}
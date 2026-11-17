package changmin.changmin_villager_turret.feature.turret.tanker;

import changmin.changmin_villager_turret.feature.turret.common.TurretTradeUtil;

import changmin.changmin_villager_turret.feature.turret.common.AbstractTurretEntity;

import changmin.changmin_villager_turret.config.TurretConfig;

import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class TankerTurretEntity extends AbstractTurretEntity {

    private static final EntityDataAccessor<Integer> TURRET_LEVEL = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> XP = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEEDED_XP = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.INT);

    // 업그레이드 요소 데이터 등록
    private static final EntityDataAccessor<Integer> TAUNT_DURATION_LEVEL = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> REDUCTION_LEVEL = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> THORNS_LEVEL = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RECHARGE_LEVEL = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_TAUNTING = SynchedEntityData.defineId(TankerTurretEntity.class, EntityDataSerializers.BOOLEAN);


    private int tauntCooldown = 0;
    private int tauntDuration = 0;

    // 💡 [해결 핵심] 에메랄드 강화를 통한 보너스 최대 체력 보존용 변수
    private double extraMaxHealth = 0.0D;

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

    // 💡 보너스 최대체력을 관리하고 업데이트하는 메서드 (치유는 진행하지 않음)
    public double getExtraMaxHealth() {
        return this.extraMaxHealth;
    }

    public void setExtraMaxHealth(double extra) {
        this.extraMaxHealth = extra;
        double baseMax = 20.0D + (this.getTurretLevel() - 1) * 5.0D + this.extraMaxHealth;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseMax);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide) {
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
                attacker.hurt(this.damageSources().thorns(this), (float) reflectDamage);

                // 가시 반사 비주얼 파티클 및 쇳소리 사운드 가미
                if (this.level() instanceof ServerLevel serverLevel) {
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
        if (!this.level().isClientSide) {
            if (this.tauntCooldown > 0) {
                this.tauntCooldown--;
            }

            // 📢 도발 상태: 매 틱이 아닌 일정 주기(기본 10틱)마다 범위 어그로를 고정합니다. (렉 감소)
            if (this.tauntDuration > 0) {
                this.tauntDuration--;

                if (this.tickCount % TurretConfig.scanInterval() == 0 && this.level() instanceof ServerLevel serverLevel) {
                    for (Mob enemy : serverLevel.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(10.0D))) {
                        if (IZombieTribe.isZombieTribe(enemy)) {
                            enemy.setTarget(this); // 좀비의 공격 타겟을 강제로 나로 고정

                            // 좀비 머리 위에 화난 주민(분노) 빨간색 번개구름 파티클 소환
                            serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, enemy.getX(), enemy.getY() + enemy.getBbHeight() + 0.3D, enemy.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                        }
                    }
                }

                if (this.tauntDuration == 0) {
                    this.setIsTaunting(false);
                }
            }

            // 📢 쿨타임 만료 시 도발 자동 시전 판단 (주변에 좀비가 없을 때 매 틱 검색하지 않도록 주기 제한)
            if (this.tauntCooldown <= 0 && this.tauntDuration <= 0 && this.tickCount % TurretConfig.scanInterval() == 0) {
                boolean hasZombies = false;
                for (Mob enemy : this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(10.0D))) {
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
                    if (this.level() instanceof ServerLevel serverLevel) {
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
    public boolean wasKilled(ServerLevel level, LivingEntity killedEntity) {
        boolean result = super.wasKilled(level, killedEntity); // 1.19+: killed → wasKilled
        if (IZombieTribe.isZombieTribe(killedEntity)) {
            this.addXp(1);
        }
        return result;
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
            this.celebrateLevelUp(currentLvl); // 🎉 파티클 + 주변 플레이어 레벨업 문구
        } else {
            this.entityData.set(XP, currentXp);
        }
    }

    private void onLevelUp(int newLevel) {
        // [기획 요구사항] 다른 주민 터렛과 동일하게 레벨업 시 최대 체력이 5씩 증가하도록 적용 (강화 체력 수치 합산 보존)
        double newMaxHealth = 20.0D + (newLevel - 1) * 5.0D + this.extraMaxHealth;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        this.playSound(SoundEvents.PLAYER_LEVELUP, 1.2F, 0.5F);
        this.heal((float)(newMaxHealth * 0.2F)); // 레벨업 성공 시 즉시 체력 40% 자가 복구 보너스

        if (!this.level().isClientSide) {
            this.spawnAtLocation(TankerTurretTradeManager.getBoundToken(this, 1), 0.5F);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        // 💡 [추가] 손에 에메랄드를 들고 있을 때 즉시 최대체력 직접 강화 상호작용 (현재 체력 회복 제외)
        if (heldItem.is(Items.EMERALD)) {
            if (!this.level().isClientSide) {
                if (heldItem.getCount() >= 64) {
                    this.setExtraMaxHealth(this.getExtraMaxHealth() + 150.0D); // 64개 소모 ➔ 체력 +150 (하트 75개 보너스)
                    if (!player.getAbilities().instabuild) heldItem.shrink(64);
                    player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.misc.5"), true);
                } else {
                    this.setExtraMaxHealth(this.getExtraMaxHealth() + 4.0D); // 1개 소모 ➔ 체력 +4.0 (하트 2개)
                    if (!player.getAbilities().instabuild) heldItem.shrink(1);
                    player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.misc.6"), true);
                }
                this.playSound(SoundEvents.ANVIL_USE, 1.0F, 0.85F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // 업그레이드 전용 종이를 들고 있을 때 바로 소모하여 업그레이드하는 기능
        if (!heldItem.isEmpty() && heldItem.hasTag() && heldItem.getTag().contains("UpgradeType")) {
            String upgradeType = heldItem.getTag().getString("UpgradeType");
            if (!this.level().isClientSide) {
                TankerTurretTradeManager.applyUpgradeDirectly(this, upgradeType);
                if (!player.getAbilities().instabuild) heldItem.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // 일반 손이나 일반 아이템 상태에서 우클릭 시 거래 화면을 표시
        if (!this.level().isClientSide) {
            this.setTradingPlayer(player);
            this.openTradingScreen(player, this.getDisplayName(), 1);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
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


    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            TankerTurretTradeManager.populateOffers(this, this.offers);
        }
        return this.offers;
    }

    @Override
    protected void onTradeCompleted(MerchantOffer offer) {
        offer.increaseUses();
        if (!this.level().isClientSide && offer.getResult().hasTag()) {
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

        // 💡 [세이브 추가] 강화된 누적 보너스 체력 영구 보존
        tag.putDouble("ExtraMaxHealth", this.getExtraMaxHealth());
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

        // 💡 [로드 추가] 강화된 누적 보너스 체력 복구
        if (tag.contains("ExtraMaxHealth")) {
            this.extraMaxHealth = tag.getDouble("ExtraMaxHealth");
        }

        // 월드 복구 시 누적 데이터 수치가 깨지지 않도록 총합 연산 후 속성 값 재반영
        double loadedMax = 20.0D + (this.getTurretLevel() - 1) * 5.0D + this.extraMaxHealth;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(loadedMax);
    }

    @Override public Component getDisplayName() { return this.getType().getDescription(); } // 언어 파일을 따름
}
package changmin.myMod.feature.turret.healer;

import changmin.myMod.ally.IAlly;
import changmin.myMod.registry.ModItems;
import changmin.myMod.zombieTribe.IZombieTribe; // 좀비 진형 체크를 위한 임포트
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
import net.minecraft.world.entity.MobType; // 바닐라 언데드 타입을 감지하기 위한 임포트
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
        this.entityData.define(HEAL_AMOUNT, 6.0F); // 기본 단일 치유량: 하트 3개 (6 HP)
        this.entityData.define(COOLDOWN_LEVEL, 0);
        this.entityData.define(RANGE_LEVEL, 0);
        this.entityData.define(NEEDED_XP, 20);
        this.entityData.define(AOE_HEAL_ENABLED, 0);
        this.entityData.define(CLEANSE_ENABLED, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            if (this.healCooldown > 0) {
                this.healCooldown--;
            }

            // 1. 치유 타겟 물색 (자기 자신은 탐색 단계에서 원천 제외)
            findHealingTarget();

            if (this.healingTarget != null) {
                this.getLookControl().setLookAt(this.healingTarget, 30.0F, 30.0F);
            }

            // 2. 치유 발동 조건 계산
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

        // 1순위: 범위 내 부상당한 플레이어 우선 탐색
        for (LivingEntity entity : entities) {
            if (entity instanceof Player && entity.isAlive() && entity.getHealth() < entity.getMaxHealth()) {
                double dist = this.distanceToSqr(entity);
                if (dist < closestDist) {
                    closestDist = dist;
                    bestTarget = entity;
                }
            }
        }

        // 2순위: 부상당한 아군 엔티티 탐색 (자기 자신은 제외: entity != this)
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

    // [단일 치유 수행]
    private void performSingleHeal(LivingEntity target) {
        float restored = healEntity(target, false); // 단일 치유 (패널티 없음)
        if (restored > 0) {
            this.addXp((int) restored);

            // 1. 단일 마법 선형 줄기 연출 발동
            spawnSingleHealLineEffects(target);

            // 2. 주변 좀비 정화 타격 및 반격 어그로 끌기
            damageUndeadInRange();

            this.healCooldown = getCalculatedCooldown();
        }
    }

    // [광역 치유 수행]
    private void performAoeHeal() {
        double range = 8.0D + (this.getRangeLevel() * 1.0D);
        AABB box = this.getBoundingBox().inflate(range);
        List<LivingEntity> entities = this.level.getEntitiesOfClass(LivingEntity.class, box);

        float totalRestored = 0.0F;
        boolean healedAtLeastOne = false;

        for (LivingEntity entity : entities) {
            // 광역 치유 대상에서도 자기 자신은 철저히 제외하여 밸런스 유지
            if (entity != this && entity.isAlive() && (entity instanceof Player || this.isAllyWith(entity))) {
                if (entity.getHealth() < entity.getMaxHealth()) {
                    float restored = healEntity(entity, true); // 광역 치유 (치유량 50% 패널티 적용)
                    if (restored > 0) {
                        totalRestored += restored;

                        // 아군 치유 피격 연출 (하트 + 초록 반짝이)
                        if (this.level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.HEART, entity.getX(), entity.getY(0.5D), entity.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.01D);
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY(0.5D), entity.getZ(), 5, 0.3D, 0.3D, 0.3D, 0.01D);
                        }
                        healedAtLeastOne = true;
                    }
                }
            }
        }

        if (healedAtLeastOne) {
            this.addXp((int) totalRestored);

            // 1. 발밑에서 구형으로 퍼져나가는 파동 원형 고리 연출 발동
            spawnAoeRingEffects();

            // 2. 주변 좀비 정화 타격 및 반격 어그로 끌기
            damageUndeadInRange();

            this.healCooldown = getCalculatedCooldown();
        }
    }

    // [공통 치유 수치 계산]
    private float healEntity(LivingEntity entity, boolean isAoe) {
        float currentHealth = entity.getHealth();
        float maxHealth = entity.getMaxHealth();

        float healAmount = this.getHealAmount();
        // 밸런스 조정: 광역 치유 모드 시에는 개별 치유량을 50% 감소시킵니다.
        if (isAoe) {
            healAmount *= 0.5F;
        }

        float actualHealed = Math.min(healAmount, maxHealth - currentHealth);

        if (actualHealed > 0) {
            entity.heal(actualHealed);

            if (this.getCleanseEnabled() == 1) {
                entity.removeEffect(MobEffects.POISON);
                entity.removeEffect(MobEffects.WITHER);
                entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                entity.removeEffect(MobEffects.WEAKNESS);
            }
        }
        return actualHealed;
    }

    // [성스러운 빛 광역 좀비 정화 타격 - 좀비 반격 유도]
    private void damageUndeadInRange() {
        double range = 8.0D + (this.getRangeLevel() * 1.0D);
        AABB box = this.getBoundingBox().inflate(range);
        List<LivingEntity> targets = this.level.getEntitiesOfClass(LivingEntity.class, box);

        for (LivingEntity entity : targets) {
            // 아군이 아닌 언데드 판정 몹(좀비 진형 포함)을 감지합니다.
            if (entity.isAlive() && (entity.getMobType() == MobType.UNDEAD || IZombieTribe.isZombieTribe(entity))) {
                // 공격의 주체(Source)를 이 토템(this)으로 등록해 1.0F(하트 반 개)의 피해를 줍니다.
                // 좀비는 공격의 주체를 인식하고 복수(반격 AI)하기 위해 무조건 토템을 향해 진격하게 됩니다!
                entity.hurt(DamageSource.mobAttack(this), 1.0F);

                // 좀비 피격 시 오염 정화 연출 발동 (검은 연기와 작은 불꽃)
                spawnUndeadDamageEffects(entity);
            }
        }
    }

    // 1. [단일 치유 시 줄기 연출]
    private void spawnSingleHealLineEffects(LivingEntity target) {
        if (this.level instanceof ServerLevel serverLevel) {
            double startX = this.getX();
            double startY = this.getY(1.2D); // 토템 중심 높이
            double startZ = this.getZ();

            double endX = target.getX();
            double endY = target.getY(0.5D); // 대상 중심 높이
            double endZ = target.getZ();

            double dx = endX - startX;
            double dy = endY - startY;
            double dz = endZ - startZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            int particleCount = (int) (distance * 4); // 블록당 4개씩 파티클 촘촘하게 배치
            for (int i = 0; i <= particleCount; i++) {
                double ratio = (double) i / particleCount;
                double px = startX + dx * ratio;
                double py = startY + dy * ratio;
                double pz = startZ + dz * ratio;

                // 초록색 주민 효과 파티클로 완벽한 마법 선형 줄기 구현
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }

            // 최종 도달 시 대형 하트 연출
            serverLevel.sendParticles(ParticleTypes.HEART, endX, endY + 0.5D, endZ, 5, 0.3D, 0.3D, 0.3D, 0.02D);
        }
        this.playSound(SoundEvents.VILLAGER_WORK_CLERIC, 1.0F, 1.2F + this.random.nextFloat() * 0.3F);
    }

    // 2. [광역 치유 시 파동 연출]
    private void spawnAoeRingEffects() {
        if (this.level instanceof ServerLevel serverLevel) {
            double centerX = this.getX();
            double centerY = this.getY(); // 발밑 높이 기준
            double centerZ = this.getZ();

            double maxRange = 8.0D + (this.getRangeLevel() * 1.0D);

            // 3단 동심원 고리 파동 표현 (반지름: 2블록, 중간값블록, 최대 사거리 블록)
            double[] radii = { 2.0D, maxRange / 2.0D, maxRange };

            for (double r : radii) {
                int points = (int) (r * 8); // 원 둘레에 비례하게 파티클 포인트 개수 분배
                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI / points) * i;
                    double px = centerX + Math.cos(angle) * r;
                    double pz = centerZ + Math.sin(angle) * r;

                    // 토템 파티클로 사방으로 퍼져나가는 황금빛 고리 파동 연출
                    serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px, centerY + 0.1D, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }
        this.playSound(SoundEvents.VILLAGER_WORK_CLERIC, 1.0F, 1.0F + this.random.nextFloat() * 0.2F);
    }

    // 3. [좀비 피격 시 오염 정화 연출]
    private void spawnUndeadDamageEffects(LivingEntity entity) {
        if (this.level instanceof ServerLevel serverLevel) {
            // 정화되어 타들어가는 불꽃과 검은 기포 연기 방출
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY(0.5D), entity.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.01D);
            serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, entity.getX(), entity.getY(0.5D), entity.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.01D);
        }
    }

    // [성능 조절: 광역 치유 모드 시 쿨타임 50% 패널티 동적 가산 적용]
    public int getCalculatedCooldown() {
        int baseCooldown = 200; // 기본 10초 (200틱)
        int reduction = this.getCooldownLevel() * 10; // 레벨당 0.5초(10틱) 감소
        int finalCd = Math.max(20, baseCooldown - reduction); // 최소 1초(20틱) 보장

        // 광역 치유 업그레이드가 활성화된 경우 쿨타임 50% 추가 패널티 적용
        if (this.getAoeHealEnabled() == 1) {
            finalCd = (int) (finalCd * 1.5F);
        }
        return finalCd;
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

            int nextNeededHearts = 10 + (currentLvl - 1) * 5;
            this.setNeededXp(nextNeededHearts * 2);

            this.onLevelUp(currentLvl);
        } else {
            this.entityData.set(XP, currentXp);
        }
    }

    private void onLevelUp(int newLevel) {
        double newMaxHealth = 10.0D + (newLevel - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        this.setHealth((float) newMaxHealth);

        this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 0.8F);

        if (!this.level.isClientSide) {
            this.spawnAtLocation(HealerTradeManager.getBoundToken(this, 1), 0.5F);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty()) {
            String upgradeType = null;
            if (heldItem.hasTag() && heldItem.getTag().contains("UpgradeType")) {
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
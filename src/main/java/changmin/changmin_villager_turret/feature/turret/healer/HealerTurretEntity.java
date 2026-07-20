package changmin.changmin_villager_turret.feature.turret.healer;

import changmin.changmin_villager_turret.ally.IAlly;
import net.minecraft.core.particles.ParticleTypes; // 🆕 에메랄드 파티클용 추가
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
import net.minecraft.world.entity.ExperienceOrb; // 🆕 경험치 즉시 분출용 추가
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

public class HealerTurretEntity extends PathfinderMob implements IAlly, Merchant {

    private static final EntityDataAccessor<Integer> TURRET_LEVEL = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> XP = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> HEAL_AMOUNT = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COOLDOWN_LEVEL = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RANGE_LEVEL = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEEDED_XP = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AOE_HEAL_ENABLED = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLEANSE_ENABLED = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHIELD_LEVEL = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CURRENT_COOLDOWN = SynchedEntityData.defineId(HealerTurretEntity.class, EntityDataSerializers.INT);

    private Player tradingPlayer;
    private MerchantOffers offers;

    public HealerTurretEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TURRET_LEVEL, 1);
        this.entityData.define(XP, 0);
        this.entityData.define(HEAL_AMOUNT, 6.0F);
        this.entityData.define(COOLDOWN_LEVEL, 0);
        this.entityData.define(RANGE_LEVEL, 0);
        this.entityData.define(NEEDED_XP, 20);
        this.entityData.define(AOE_HEAL_ENABLED, 0);
        this.entityData.define(CLEANSE_ENABLED, 0);
        this.entityData.define(SHIELD_LEVEL, 0);
        this.entityData.define(CURRENT_COOLDOWN, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(1, new HealerHealGoal(this));
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
            if (this.getCurrentCooldown() > 0) {
                this.setCurrentCooldown(this.getCurrentCooldown() - 1);
            }
        }
    }

    public int getCalculatedCooldown() {
        int baseCooldown = 200; // 기본 10초
        int reduction = this.getCooldownLevel() * 10;
        int finalCd = Math.max(20, baseCooldown - reduction);

        if (this.getAoeHealEnabled() == 1) {
            finalCd = (int) (finalCd * 1.5F);
        }
        return finalCd;
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
            // 1. 하급 티켓 개수 연산 (*0.2 소수점 곱셈 연산 적용)
            int baseTickets;
            if (newLevel <= 4) {
                baseTickets = 1;
            } else if (newLevel <= 7) {
                baseTickets = 2;
            } else {
                baseTickets = 4 + (int) ((newLevel - 8) * 0.2F);
            }

            // 2. 10% 확률 대박(Jackpot) 연산 적용
            int finalTickets = baseTickets;
            if (this.random.nextFloat() < 0.10F) {
                if (newLevel <= 4) {
                    finalTickets = 2;
                } else if (newLevel <= 7) {
                    finalTickets = this.random.nextBoolean() ? 3 : 4;
                } else {
                    finalTickets = (int) Math.ceil(baseTickets * 1.5F); // 상급 구간 대박 시 1.5배 증가 (소수점 올림)
                }
                // 대박 보상 시 경쾌한 팡파레 소리 추가
                this.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 1.3F);
            }

            // 3. 단일화된 하급 티켓 드롭 (HealerTradeManager에는 수량 finalTickets와 하급 티어 코드 0 전달)
            this.spawnAtLocation(HealerTradeManager.getBoundToken(this, finalTickets, 0), 0.5F);

            // 4. 경험치병 아이템 대신 에메랄드빛 경험치 구슬 분수 연출 분출
            if (this.level instanceof ServerLevel serverLevel) {
                int xpAmount = newLevel * 50;

                // 마인크래프트 공식 API를 이용해 경험치 구슬 즉시 소환
                ExperienceOrb.award(serverLevel, this.position(), xpAmount);

                // 초록색 에메랄드 파티클(HAPPY_VILLAGER)을 사방으로 화려하게 터트림
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        this.getX(), this.getY(1.0D), this.getZ(), 45, 0.5D, 0.5D, 0.5D, 0.15D);
            }
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
    public int getShieldLevel() { return this.entityData.get(SHIELD_LEVEL); }
    public void setShieldLevel(int level) { this.entityData.set(SHIELD_LEVEL, level); }
    public int getCurrentCooldown() { return this.entityData.get(CURRENT_COOLDOWN); }
    public void setCurrentCooldown(int ticks) { this.entityData.set(CURRENT_COOLDOWN, ticks); }

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
        tag.putInt("ShieldLevel", this.getShieldLevel());
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
        if (tag.contains("ShieldLevel")) this.setShieldLevel(tag.getInt("ShieldLevel"));

        double loadedMax = 10.0D + (this.getTurretLevel() - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(loadedMax);
    }

    @Override public Component getDisplayName() { return new TextComponent("치유 토템 터렛"); }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(net.minecraft.world.entity.Entity entity) {}
}
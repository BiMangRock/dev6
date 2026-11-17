package changmin.changmin_villager_turret.feature.hero.healer_hero;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import changmin.changmin_villager_turret.feature.turret.common.TurretTradeUtil;

import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import changmin.changmin_villager_turret.feature.turret.healer.HealerTurretEntity;
import changmin.changmin_villager_turret.registry.ModEntityTypes;
import changmin.changmin_villager_turret.registry.ModItems; // 🆕 추가
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand; // 🆕 추가
import net.minecraft.world.InteractionResult; // 🆕 추가
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack; // 🆕 추가
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HealerHeroEntity extends PathfinderMob implements IAlly, GeoEntity {
    private static final EntityDataAccessor<Integer> DATA_LEVEL = SynchedEntityData.defineId(HealerHeroEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_XP_BUFF = SynchedEntityData.defineId(HealerHeroEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_XP_ATTACK = SynchedEntityData.defineId(HealerHeroEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ANIMATION = SynchedEntityData.defineId(HealerHeroEntity.class, EntityDataSerializers.INT);

    public static final UUID HERO_GROWTH_ID = UUID.fromString("4e5f6a7b-8c9d-0e1f-2a3b-4c5d6e7f8a9b");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int animationTick = 0;
    private final List<UUID> summonedTurretUUIDs = new ArrayList<>();

    // 🆕 NBT 경비 위치를 저장할 경비 좌표 전용 필드 선언
    private int homeX = 0;
    private int homeY = 0;
    private int homeZ = 0;
    private boolean isBound = false;

    public HealerHeroEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_LEVEL, 1);
        this.entityData.define(DATA_XP_BUFF, 0);
        this.entityData.define(DATA_XP_ATTACK, 0);
        this.entityData.define(DATA_ANIMATION, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // 🆕 [최우선 행동 가중치] 1순위 복귀 골 장착 (경비 상태 시 이탈을 제어)
        this.goalSelector.addGoal(1, new HealerHeroReturnHomeGoal(this));

        this.goalSelector.addGoal(2, new HealerHeroActionGoal(this));
        this.goalSelector.addGoal(5, new HealerHeroStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                this, net.minecraft.world.entity.LivingEntity.class, 10, true, false,
                e -> IZombieTribe.isZombieTribe(e) && !(e instanceof IAlly)
        ));
    }

    // 🆕 [우클릭 상호작용 해결] 때리지 않고 안전하게 경비 명령을 복사하여 임명하는 로직
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty()) {
            if (heldItem.is(ModItems.GUARD_LINKER.get())) {
                CompoundTag tag = heldItem.getTag();
                if (tag != null && tag.getBoolean("HasCoords")) {
                    if (!this.level().isClientSide) {
                        this.homeX = tag.getInt("TargetX");
                        this.homeY = tag.getInt("TargetY");
                        this.homeZ = tag.getInt("TargetZ");
                        this.isBound = true;

                        player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.misc.1", this.homeX, this.homeY, this.homeZ), true);
                        this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);

                        if (this.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY(1.0D), this.getZ(), 15, 0.2D, 0.2D, 0.2D, 0.05D);
                        }

                        if (!player.getAbilities().instabuild) {
                            heldItem.shrink(1);
                        }
                    }
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.animationTick > 0) {
                this.animationTick--;
                if (this.animationTick <= 0) {
                    this.setAnimationState(0);
                }
            }
        }
    }

    public void recordHealXP(int amount) {
        this.setXpBuff(this.getXpBuff() + amount);
        checkLevelUp();
    }

    public void recordAttackXP(int amount) {
        this.setXpAttack(this.getXpAttack() + amount);
        checkLevelUp();
    }

    private void checkLevelUp() {
        int needed = getNeededXp();
        if (this.getXpBuff() >= needed || this.getXpAttack() >= needed) {
            levelUp();
        }
    }

    private void levelUp() {
        this.setXpBuff(0);
        this.setXpAttack(0);
        int nextLevel = this.getHeroLevel() + 1;
        this.setHeroLevel(nextLevel);

        this.updateMaxHealthAttribute();

        if (!this.level().isClientSide) {
            triggerUltimateStorm();
        }
    }

    private void triggerUltimateStorm() {
        this.triggerUltimateAnimation();
        int count = 10 + (this.getHeroLevel() / 5) * 5;

        for (int i = 0; i < count; i++) {
            double angle = i * (2 * Math.PI / count);
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            double dy = 0.15D;

            LoveProjectileEntity proj = new LoveProjectileEntity(this.level(), this, this.getHeroLevel());
            proj.setDeltaMovement(dx * 0.8D, dy, dz * 0.8D);
            this.level().addFreshEntity(proj);
        }
    }

    private void updateMaxHealthAttribute() {
        var attribute = this.getAttribute(Attributes.MAX_HEALTH);
        if (attribute != null) {
            attribute.removeModifier(HERO_GROWTH_ID);
            double levelBonus = (getHeroLevel() - 1) * 5.0D;
            attribute.addPermanentModifier(new AttributeModifier(HERO_GROWTH_ID, "Hero Growth", levelBonus, AttributeModifier.Operation.ADDITION));
        }
    }

    public int getMaxTurretsCount() {
        return 3 + (this.getHeroLevel() / 5);
    }

    public int getSummonedTurretsCount() {
        if (this.level().isClientSide) return 0;
        ServerLevel serverLevel = (ServerLevel) this.level();
        this.summonedTurretUUIDs.removeIf(uuid -> {
            net.minecraft.world.entity.Entity entity = serverLevel.getEntity(uuid);
            return entity == null || !entity.isAlive();
        });
        return this.summonedTurretUUIDs.size();
    }

    public void trackSummonedTurret(UUID uuid) {
        this.summonedTurretUUIDs.add(uuid);
    }

    public void triggerAttackOrHealAnimation() { this.triggerAnimation(1, 40); }
    public void triggerBuildAnimation() { this.triggerAnimation(2, 20); }
    public void triggerUltimateAnimation() { this.triggerAnimation(3, 60); }
    public void triggerAnimation(int state, int ticks) { this.setAnimationState(state); this.animationTick = ticks; }

    public int getHeroLevel() { return this.entityData.get(DATA_LEVEL); }
    public void setHeroLevel(int level) { this.entityData.set(DATA_LEVEL, level); }
    public int getXpBuff() { return this.entityData.get(DATA_XP_BUFF); }
    public void setXpBuff(int xp) { this.entityData.set(DATA_XP_BUFF, xp); }
    public int getXpAttack() { return this.entityData.get(DATA_XP_ATTACK); }
    public void setXpAttack(int xp) { this.entityData.set(DATA_XP_ATTACK, xp); }
    public int getNeededXp() { return this.getHeroLevel() * 10; }

    public void setAnimationState(int state) { this.entityData.set(DATA_ANIMATION, state); }
    public int getAnimationState() { return this.entityData.get(DATA_ANIMATION); }

    // 🆕 NBT 경비 좌표 파일 영구 저장 연동 보강
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HeroLevel", this.getHeroLevel());
        tag.putInt("HealXp", this.getXpBuff());
        tag.putInt("AttackXp", this.getXpAttack());
        tag.putInt("HomeX", this.homeX);
        tag.putInt("HomeY", this.homeY);
        tag.putInt("HomeZ", this.homeZ);
        tag.putBoolean("IsBound", this.isBound);
        ListTag list = new ListTag();
        for (UUID uuid : this.summonedTurretUUIDs) {
            list.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put("SummonedTurrets", list);
    }

    // 🆕 NBT 경비 좌표 파일 불러오기 연동 보강
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HeroLevel", 99)) this.setHeroLevel(tag.getInt("HeroLevel"));
        if (tag.contains("HealXp", 99)) this.setXpBuff(tag.getInt("HealXp"));
        if (tag.contains("AttackXp", 99)) this.setXpAttack(tag.getInt("AttackXp"));
        if (tag.contains("HomeX")) this.homeX = tag.getInt("HomeX");
        if (tag.contains("HomeY")) this.homeY = tag.getInt("HomeY");
        if (tag.contains("HomeZ")) this.homeZ = tag.getInt("HomeZ");
        if (tag.contains("IsBound")) this.isBound = tag.getBoolean("IsBound");
        this.summonedTurretUUIDs.clear();
        if (tag.contains("SummonedTurrets", 9)) {
            ListTag list = tag.getList("SummonedTurrets", 8);
            for (int i = 0; i < list.size(); i++) {
                this.summonedTurretUUIDs.add(UUID.fromString(list.getString(i)));
            }
        }
        this.updateMaxHealthAttribute();
    }

    private PlayState predicate(AnimationState<HealerHeroEntity> event) {
        if (this.getAnimationState() == 1) {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("attackorheal"));
            return PlayState.CONTINUE;
        } else if (this.getAnimationState() == 2) {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("build"));
            return PlayState.CONTINUE;
        } else if (this.getAnimationState() == 3) {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("play"));
            return PlayState.CONTINUE;
        }
        if (event.isMoving()) {
            event.getController().setAnimation(RawAnimation.begin().thenLoop("walk"));
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    public static class HealerHeroStrollGoal extends WaterAvoidingRandomStrollGoal {
        private final HealerHeroEntity hero;

        public HealerHeroStrollGoal(HealerHeroEntity hero, double speed) {
            super(hero, speed);
            this.hero = hero;
        }

        @Override
        public boolean canUse() {
            return this.hero.getHeroLevel() >= 3 && super.canUse();
        }
    }

    // 🆕 [추가] 15블록 이상 거리 이탈 시 길찾기 복귀, 40블록 이상 시 정렬 텔레포트 수행 골
    public static class HealerHeroReturnHomeGoal extends Goal {
        private final HealerHeroEntity hero;

        public HealerHeroReturnHomeGoal(HealerHeroEntity hero) {
            this.hero = hero;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.hero.isBound) return false;
            double dx = Math.abs(this.hero.getX() - this.hero.homeX);
            double dz = Math.abs(this.hero.getZ() - this.hero.homeZ);
            return dx > 15.0D || dz > 15.0D;
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.hero.isBound) return false;
            double dx = Math.abs(this.hero.getX() - this.hero.homeX);
            double dz = Math.abs(this.hero.getZ() - this.hero.homeZ);
            return dx > 2.0D || dz > 2.0D;
        }

        @Override
        public void start() {
            this.hero.getNavigation().moveTo(this.hero.homeX, this.hero.homeY, this.hero.homeZ, 1.2D);
        }

        @Override
        public void tick() {
            double dx = Math.abs(this.hero.getX() - this.hero.homeX);
            double dz = Math.abs(this.hero.getZ() - this.hero.homeZ);

            if (dx > 40.0D || dz > 40.0D) {
                // 구덩이 끼임 방지용 강제 복귀 소환
                this.hero.teleportTo(this.hero.homeX, this.hero.homeY, this.hero.homeZ);
                this.hero.getNavigation().stop();
            } else {
                this.hero.getNavigation().moveTo(this.hero.homeX, this.hero.homeY, this.hero.homeZ, 1.2D);
            }
        }

        @Override
        public void stop() {
            this.hero.getNavigation().stop();
        }
    }
}
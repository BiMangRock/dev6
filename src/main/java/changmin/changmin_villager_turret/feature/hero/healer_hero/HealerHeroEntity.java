package changmin.changmin_villager_turret.feature.hero.healer_hero;

import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import changmin.changmin_villager_turret.feature.turret.healer.HealerTurretEntity;
import changmin.changmin_villager_turret.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HealerHeroEntity extends PathfinderMob implements IAlly, IAnimatable {
    private static final EntityDataAccessor<Integer> DATA_LEVEL = SynchedEntityData.defineId(HealerHeroEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_XP_BUFF = SynchedEntityData.defineId(HealerHeroEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_XP_ATTACK = SynchedEntityData.defineId(HealerHeroEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ANIMATION = SynchedEntityData.defineId(HealerHeroEntity.class, EntityDataSerializers.INT);

    public static final UUID HERO_GROWTH_ID = UUID.fromString("4e5f6a7b-8c9d-0e1f-2a3b-4c5d6e7f8a9b");

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int animationTick = 0;
    private final List<UUID> summonedTurretUUIDs = new ArrayList<>();

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
        this.goalSelector.addGoal(1, new HealerHeroActionGoal(this));

        // 🆕 [수정] 영웅 3레벨 이상일 때만 자유롭게 지상을 배회하도록 커스텀 골 등록
        this.goalSelector.addGoal(5, new HealerHeroStrollGoal(this, 1.0D));

        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                this, net.minecraft.world.entity.LivingEntity.class, 10, true, false,
                e -> IZombieTribe.isZombieTribe(e) && !(e instanceof IAlly)
        ));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
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

        if (!this.level.isClientSide) {
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

            LoveProjectileEntity proj = new LoveProjectileEntity(this.level, this, this.getHeroLevel());
            proj.setDeltaMovement(dx * 0.8D, dy, dz * 0.8D);
            this.level.addFreshEntity(proj);
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
        if (this.level.isClientSide) return 0;
        ServerLevel serverLevel = (ServerLevel) this.level;
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

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HeroLevel", this.getHeroLevel());
        tag.putInt("HealXp", this.getXpBuff());
        tag.putInt("AttackXp", this.getXpAttack());
        ListTag list = new ListTag();
        for (UUID uuid : this.summonedTurretUUIDs) {
            list.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put("SummonedTurrets", list);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HeroLevel", 99)) this.setHeroLevel(tag.getInt("HeroLevel"));
        if (tag.contains("HealXp", 99)) this.setXpBuff(tag.getInt("HealXp"));
        if (tag.contains("AttackXp", 99)) this.setXpAttack(tag.getInt("AttackXp"));
        this.summonedTurretUUIDs.clear();
        if (tag.contains("SummonedTurrets", 9)) {
            ListTag list = tag.getList("SummonedTurrets", 8);
            for (int i = 0; i < list.size(); i++) {
                this.summonedTurretUUIDs.add(UUID.fromString(list.getString(i)));
            }
        }
        this.updateMaxHealthAttribute();
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.getAnimationState() == 1) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("attackorheal"));
            return PlayState.CONTINUE;
        } else if (this.getAnimationState() == 2) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("build"));
            return PlayState.CONTINUE;
        } else if (this.getAnimationState() == 3) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("play"));
            return PlayState.CONTINUE;
        }
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().loop("walk"));
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() { return this.factory; }

    // 🆕 [추가] 3레벨 도달 시에만 배회 구동을 허용하는 전용 StrollGoal 구조 정의
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
}
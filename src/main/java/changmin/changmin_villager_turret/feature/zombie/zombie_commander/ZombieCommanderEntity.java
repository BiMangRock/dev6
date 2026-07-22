package changmin.changmin_villager_turret.feature.zombie.zombie_commander;

import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

import java.util.UUID;

public class ZombieCommanderEntity extends PathfinderMob implements IAnimatable, IZombieTribe {
    private static final EntityDataAccessor<Integer> DATA_LEVEL = SynchedEntityData.defineId(ZombieCommanderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_XP_BUFF = SynchedEntityData.defineId(ZombieCommanderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_XP_ATTACK = SynchedEntityData.defineId(ZombieCommanderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ANIMATION = SynchedEntityData.defineId(ZombieCommanderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COOLDOWN = SynchedEntityData.defineId(ZombieCommanderEntity.class, EntityDataSerializers.INT);

    public static final UUID AURA_HEALTH_BUFF_ID = UUID.fromString("7b5a2b3c-1d9e-4f1a-8c8b-9d5e6f7a8b9c");
    public static final UUID MAGGOT_ABSORB_ID = UUID.fromString("1a2b3c4d-5e6f-4a1b-8c8d-9e0f1a2b3c4d");

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int animationTick = 0;
    private double absorbedHealthBonus = 0;

    private float maggotAtkBonus = 0;
    private int maggotLifeBonus = 0;
    private double maggotHpBonus = 0;

    public ZombieCommanderEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_LEVEL, 1);
        this.entityData.define(DATA_XP_BUFF, 0);
        this.entityData.define(DATA_XP_ATTACK, 0);
        this.entityData.define(DATA_ANIMATION, 0);
        this.entityData.define(DATA_COOLDOWN, 0);
    }

    @Override
    protected float getJumpPower() { return 1.0F; }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            if (this.tickCount % 20 == 0 && this.getHealth() < this.getMaxHealth()) this.heal(1.0F);
            if (this.animationTick > 0) {
                this.animationTick--;
                if (this.animationTick <= 0) this.setAnimationState(0);
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowAlliesGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new CommanderActionGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    // 💡 에러 해결: recordKill 메서드 추가 (직접 킬 시 공격 XP 5점 부여)
    public void recordKill() {
        this.recordAttackXP(5.0D);
    }

    // Goal에서 호출하는 메서드
    public void recordBuffs(int count) {
        this.setBuffXp(this.getBuffXp() + count);
        checkLevelUp();
    }

    public void recordAttackXP(double damage) {
        this.setAttackXp(this.getAttackXp() + (int)damage);
        this.absorbedHealthBonus += damage;
        updateMaxHealthAttribute();
        checkLevelUp();
    }

    private void checkLevelUp() {
        int needed = getNeededXp();
        if (this.getBuffXp() >= needed || this.getAttackXp() >= needed) {
            this.setBossLevel(this.getBossLevel() + 1);
            this.setBuffXp(0);
            this.setAttackXp(0);
            applyRandomUpgrade();
            updateMaxHealthAttribute();
        }
    }

    private void applyRandomUpgrade() {
        int r = this.random.nextInt(100);
        int value = (r < 70) ? 1 : (r < 85) ? 2 : (r < 95) ? 3 : 4;
        int type = this.random.nextInt(3);
        switch (type) {
            case 0 -> maggotAtkBonus += value;
            case 1 -> maggotLifeBonus += value * 20;
            case 2 -> maggotHpBonus += value * 2.0D;
        }
    }

    private void updateMaxHealthAttribute() {
        var attribute = this.getAttribute(Attributes.MAX_HEALTH);
        if (attribute != null) {
            attribute.removeModifier(MAGGOT_ABSORB_ID);
            double levelBonus = (getBossLevel() - 1) * 5.0D;
            attribute.addPermanentModifier(new AttributeModifier(MAGGOT_ABSORB_ID, "Commander Growth", levelBonus + absorbedHealthBonus, AttributeModifier.Operation.ADDITION));
        }
    }

    public void triggerBuffAnimation() { this.triggerAnimation(1, 40); }
    public void triggerAttackAnimation() { this.triggerAnimation(2, 40); }
    public void triggerUltimateAnimation() { this.triggerAnimation(3, 60); }
    public void triggerAnimation(int state, int ticks) { this.setAnimationState(state); this.animationTick = ticks; }

    public float getMaggotAtk() { return 2.0F + maggotAtkBonus; }
    public int getMaggotLife() { return 200 + maggotLifeBonus; }
    public double getMaggotHp() { return 6.0D + maggotHpBonus; }

    public void setAnimationState(int state) { this.entityData.set(DATA_ANIMATION, state); }
    public int getAnimationState() { return this.entityData.get(DATA_ANIMATION); }
    public int getBossLevel() { return this.entityData.get(DATA_LEVEL); }
    public void setBossLevel(int level) { this.entityData.set(DATA_LEVEL, level); }
    public int getBuffXp() { return this.entityData.get(DATA_XP_BUFF); }
    public void setBuffXp(int xp) { this.entityData.set(DATA_XP_BUFF, xp); }
    public int getAttackXp() { return this.entityData.get(DATA_XP_ATTACK); }
    public void setAttackXp(int xp) { this.entityData.set(DATA_XP_ATTACK, xp); }
    public int getNeededXp() { return this.getBossLevel() * 10; }
    public float getBuffRange() { return 10.0F + (getBossLevel() * 0.5F); }
    public void setSyncCooldown(int ticks) { this.entityData.set(DATA_COOLDOWN, ticks); }
    public int getSyncCooldown() { return this.entityData.get(DATA_COOLDOWN); }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.getAnimationState() == 1 || this.getAnimationState() == 2) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("buff"));
            return PlayState.CONTINUE;
        } else if (this.getAnimationState() == 3) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("altimate"));
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
}
package changmin.changmin_villager_turret.feature.zombie.assassin2;

import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag; // 🆕 추가
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class Assassin2Entity extends Monster implements IAnimatable, IZombieTribe {
    private static final EntityDataAccessor<Boolean> DATA_IS_ATTACKING = SynchedEntityData.defineId(Assassin2Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ASSASSIN_LEVEL = SynchedEntityData.defineId(Assassin2Entity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CURRENT_XP = SynchedEntityData.defineId(Assassin2Entity.class, EntityDataSerializers.INT);

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    public int attackTimer = 0;

    public Assassin2Entity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.maxUpStep = 1.0F;
    }

    @Override
    public void jumpFromGround() { super.jumpFromGround(); }

    public void performAssassinJump() { this.jumpFromGround(); }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_ATTACKING, false);
        this.entityData.define(DATA_ASSASSIN_LEVEL, 1);
        this.entityData.define(DATA_CURRENT_XP, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // 💡 통합된 AI 하나만 등록 (이동과 공격을 동시에 수행)
        this.goalSelector.addGoal(1, new Assassin2IntegratedGoal(this));

        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, IAlly::isAllyEntity));
    }

    // 💡 암살자 저장 데이터를 NBT 파일에 기록하는 메서드
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AssassinLevel", this.getAssassinLevel());
        tag.putInt("CurrentXp", this.getCurrentXp());
    }

    // 💡 세이브파일로부터 암살자 레벨/경험치 복구 및 속성 재설정 메서드
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AssassinLevel", 99)) {
            this.setAssassinLevel(tag.getInt("AssassinLevel"));
        }
        if (tag.contains("CurrentXp", 99)) {
            this.setCurrentXp(tag.getInt("CurrentXp"));
        }

        // 💡 [속성 동기화] 저장된 레벨 값에 맞게 최대 체력과 공격력을 파일 로딩 시점에 올바르게 주입합니다.
        int loadedLevel = this.getAssassinLevel();

        double newMaxHealth = 30.0D + (loadedLevel - 1) * 5.0D;
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        }

        double d = 4.0D + (loadedLevel - 1) * 1.0D;
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(d);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackTimer > 0) {
            this.attackTimer--;
            if (!this.level.isClientSide) this.setAttacking(true);
        } else {
            if (!this.level.isClientSide) this.setAttacking(false);
        }
    }

    // 레벨업 로직 (기존 유지)
    public int getNeededXp() { return this.getAssassinLevel() * 3; }
    public void recordKill() {
        int nextXp = this.getCurrentXp() + 1;
        this.setCurrentXp(nextXp);
        if (nextXp >= getNeededXp()) levelUp();
    }
    private void levelUp() {
        this.setCurrentXp(0);
        int nextLevel = this.getAssassinLevel() + 1;
        this.setAssassinLevel(nextLevel);
        double newMaxHealth = 30.0D + (nextLevel - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        this.setHealth((float) newMaxHealth);
        double d = 4.0D + (nextLevel - 1) * 1.0D;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(d);
    }

    public boolean isAttacking() { return this.entityData.get(DATA_IS_ATTACKING); }
    public void setAttacking(boolean attacking) { this.entityData.set(DATA_IS_ATTACKING, attacking); }
    public int getAssassinLevel() { return this.entityData.get(DATA_ASSASSIN_LEVEL); }
    public void setAssassinLevel(int level) { this.entityData.set(DATA_ASSASSIN_LEVEL, level); }
    public int getCurrentXp() { return this.entityData.get(DATA_CURRENT_XP); }
    public void setCurrentXp(int xp) { this.entityData.set(DATA_CURRENT_XP, xp); }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("attack"));
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
        data.addAnimationController(new AnimationController<>(this, "controller", 4, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() { return this.factory; }

    // Assassin2Entity 클래스 안에 추가
    public void performAirJump() {
        // 현재 속도를 가져와서 Y값(높이)만 위로 다시 설정 (0.5D 정도가 적당함)
        Vec3 currentMove = this.getDeltaMovement();
        this.setDeltaMovement(currentMove.x, 0.5D, currentMove.z);

        // 이단 점프 시 소리와 파티클 효과 (닌자 느낌)
        if (!this.level.isClientSide) {
            ((ServerLevel)this.level).sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 5, 0.2, 0, 0.2, 0.05);
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_FLAP, this.getSoundSource(), 1.0F, 1.5F);
        }
    }
}
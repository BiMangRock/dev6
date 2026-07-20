package changmin.myMod.feature.zombie.assassin2;

import changmin.myMod.ally.IAlly;
import changmin.myMod.zombieTribe.IZombieTribe;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class Assassin2Entity extends Monster implements IAnimatable, IZombieTribe {
    // 애니메이션 및 레벨업 시스템 동기화를 위한 데이터 와처 변수 등록
    private static final EntityDataAccessor<Boolean> DATA_IS_ATTACKING = SynchedEntityData.defineId(Assassin2Entity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ASSASSIN_LEVEL = SynchedEntityData.defineId(Assassin2Entity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CURRENT_XP = SynchedEntityData.defineId(Assassin2Entity.class, EntityDataSerializers.INT);

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public Assassin2Entity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.maxUpStep = 1.0F; // 1블록 높이의 언덕을 점프 없이 민첩하게 넘는 Step Assist 설정
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)       // 기본 체력 30
                .add(Attributes.ATTACK_DAMAGE, 4.0D)     // 기본 공격력 4
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_ATTACKING, false);
        this.entityData.define(DATA_ASSASSIN_LEVEL, 1); // 기본 암살자 레벨 1 등록
        this.entityData.define(DATA_CURRENT_XP, 0);     // 기본 처치 경험치 0 등록
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new Assassin2AttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        // [타겟 1] 나를 선제 타격한 대상을 우선적으로 반격 (반격 우선순위 1단계)
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));

        // [타겟 2] 플레이어, 주민, 주민 터렛을 포함한 모든 아군 진형을 하나의 목표로 타겟 지정 (우선순위 2단계)
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this,
                LivingEntity.class,
                10,
                true,
                false,
                IAlly::isAllyEntity
        ));
    }

    // 다음 레벨업에 필요한 킬 수 (레벨 * 3)
    public int getNeededXp() {
        return this.getAssassinLevel() * 3;
    }

    // 경험치 누적 및 레벨업 체크
    public void recordKill() {
        int nextXp = this.getCurrentXp() + 1;
        this.setCurrentXp(nextXp);
        if (nextXp >= getNeededXp()) {
            levelUp();
        }
    }

    // 레벨업 시 속성 정보 변경 및 체력 회복
    private void levelUp() {
        this.setCurrentXp(0);
        int nextLevel = this.getAssassinLevel() + 1;
        this.setAssassinLevel(nextLevel);

        // 1. 최대 체력 상승 (레벨당 +5)
        double newMaxHealth = 30.0D + (nextLevel - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        this.setHealth((float) newMaxHealth); // 체력 완전 회복

        // 2. 공격력 상승 (레벨당 +1)
        double newAttackDamage = 4.0D + (nextLevel - 1) * 1.0D;
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(newAttackDamage);
    }

    // Getter & Setter들 (와처 데이터 기반 작동)
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
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
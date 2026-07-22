package changmin.changmin_villager_turret.feature.turret.goddess_of_flame;

import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
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

public class GoddessOfFlameEntity extends Monster implements IAnimatable, IAlly, IGoddessAttack {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    // 서버-클라이언트 상태 동기화
    private static final EntityDataAccessor<Boolean> IS_ATTACKING =
            SynchedEntityData.defineId(GoddessOfFlameEntity.class, EntityDataSerializers.BOOLEAN);

    public int attackTimer = 0;

    public GoddessOfFlameEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ATTACKING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new GoddessRangedAttackGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // 좀비 부족 진형만 탐색 및 선제 타겟 공격
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                (entity) -> IZombieTribe.isZombieTribe(entity)));
    }

    @Override
    public void startAttackSequence(LivingEntity target) {
        if (!this.level.isClientSide) {
            this.attackTimer = 40; // 2초 공격 모션 타이머
        }
    }

    @Override
    public Monster asMonster() {
        return this;
    }

    private void spawnFireball(LivingEntity target) {
        if (!this.level.isClientSide && target != null) {
            GoddessFireballEntity fireball = new GoddessFireballEntity(this.level, this);

            double dX = target.getX() - this.getX();
            double dY = target.getY(0.5D) - fireball.getY();
            double dZ = target.getZ() - this.getZ();

            // 불덩이 투사체 직선 발사 (속도 1.0F, 난사 흩어짐도 1.0F)
            fireball.shoot(dX, dY, dZ, 1.0F, 1.0F);
            this.level.addFreshEntity(fireball);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            if (this.attackTimer > 0) {
                this.attackTimer--;
                this.entityData.set(IS_ATTACKING, true);

                // 공격 애니메이션 2초 동안 5틱 간격으로 불덩이를 끊임없이 연사합니다.
                if (this.attackTimer % 5 == 0) {
                    LivingEntity target = this.getTarget();
                    if (target != null && target.isAlive()) {
                        this.spawnFireball(target);
                    }
                }
            } else {
                this.entityData.set(IS_ATTACKING, false);
            }
        }
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.entityData.get(IS_ATTACKING)) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("attack"));
        } else {
            // 대기(Idle) 중일 때도 "walk" 애니메이션을 활용하여 몸체 주변 불꽃 이펙트의 팽창/수축을 상시 표현
            event.getController().setAnimation(new AnimationBuilder().loop("walk"));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
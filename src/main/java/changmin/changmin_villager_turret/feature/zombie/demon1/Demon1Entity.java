package changmin.changmin_villager_turret.feature.zombie.demon1;

import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import changmin.changmin_villager_turret.ally.IAlly;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
// 💡 Synced -> Synched 로 철자 수정 완료
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
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

import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class Demon1Entity extends Monster implements IAnimatable, IZombieTribe, IDemon1Attack {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    // 💡 SyncedEntityData -> SynchedEntityData 로 철자 수정 완료
    private static final EntityDataAccessor<Boolean> IS_ATTACKING =
            SynchedEntityData.defineId(Demon1Entity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private LivingEntity leader = null;
    public int attackTimer = 0;

    public Demon1Entity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ATTACKING, false);
    }

    public void setLeader(@Nullable LivingEntity leader) {
        this.leader = leader;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 35.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ReturnToLeaderGoal(this));
        this.goalSelector.addGoal(2, new Demon1RangedAttackGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, (entity) -> entity instanceof IAlly));
    }

    @Override
    public void startAttackSequence(LivingEntity target) {
        if (!this.level.isClientSide) {
            this.attackTimer = 40;
        }
    }

    @Override
    public Monster asMonster() {
        return this;
    }

    private void spawnBat(LivingEntity target) {
        if (!this.level.isClientSide && target != null) {
            Demon1BatEntity bat = new Demon1BatEntity(this.level, this, target);

            double dX = target.getX() - this.getX();
            double dY = target.getY(0.5D) - bat.getY();
            double dZ = target.getZ() - this.getZ();

            // 예시: 속도를 0.7F 로 감소
            bat.shoot(dX, dY, dZ, 0.7F, 1.0F);
            this.level.addFreshEntity(bat);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level.isClientSide) {
            if (this.attackTimer > 0) {
                this.attackTimer--;
                this.entityData.set(IS_ATTACKING, true);

                if (this.attackTimer <= 35 && this.attackTimer > 0) {
                    if (this.attackTimer % 4 == 0) {
                        LivingEntity target = this.getTarget();
                        if (target != null && target.isAlive()) {
                            this.spawnBat(target);
                        }
                    }
                }
            } else {
                this.entityData.set(IS_ATTACKING, false);
            }
        }
    }

    static class ReturnToLeaderGoal extends Goal {
        private final Demon1Entity demon;
        public ReturnToLeaderGoal(Demon1Entity demon) {
            this.demon = demon;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }
        @Override
        public boolean canUse() {
            return demon.leader != null && demon.leader.isAlive() && demon.distanceToSqr(demon.leader) > 400.0D;
        }
        @Override
        public void tick() {
            if (demon.leader != null) {
                demon.getNavigation().moveTo(demon.leader.getX(), demon.leader.getY() + 6, demon.leader.getZ(), 1.5D);
            }
        }
    }

    @Override
    public void travel(Vec3 p_21280_) {
        if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
            float f = this.isInWater() ? 0.8F : 0.91F;
            this.moveRelative(0.02F, p_21280_);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(f));
        }
        this.calculateEntityAnimation(this, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.entityData.get(IS_ATTACKING)) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("attack1"));
        } else {
            event.getController().setAnimation(new AnimationBuilder().loop("fly"));
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
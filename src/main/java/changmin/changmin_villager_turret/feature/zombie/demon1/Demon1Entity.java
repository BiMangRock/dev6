package changmin.changmin_villager_turret.feature.zombie.demon1;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

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

import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class Demon1Entity extends Monster implements GeoEntity, IZombieTribe, IDemon1Attack {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

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
        this.leaderUUID = leader == null ? null : leader.getUUID();
    }

    // 💾 리더 UUID 저장: 월드 재접속 후에도 리더(종말의 사도 등)와의 연결을 복원합니다.
    @Nullable private java.util.UUID leaderUUID = null;
    private int leaderLookupCooldown = 0;

    /** 리더 참조가 끊겼으면(재접속 등) 1초마다 UUID로 다시 찾아 연결합니다. */
    @Nullable
    LivingEntity resolveLeader() {
        // (AI 목표는 2틱마다 호출될 수 있어 tickCount % N 대신 호출 횟수 카운터 사용)
        if (this.leader == null && this.leaderUUID != null && --this.leaderLookupCooldown <= 0
                && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            this.leaderLookupCooldown = 10;
            net.minecraft.world.entity.Entity found = serverLevel.getEntity(this.leaderUUID);
            if (found instanceof LivingEntity living) this.leader = living;
        }
        return this.leader;
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.leaderUUID != null) tag.putUUID("LeaderUUID", this.leaderUUID);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("LeaderUUID")) this.leaderUUID = tag.getUUID("LeaderUUID");
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
        if (!this.level().isClientSide) {
            this.attackTimer = 40;
        }
    }

    @Override
    public Monster asMonster() {
        return this;
    }

    private void spawnBat(LivingEntity target) {
        if (!this.level().isClientSide && target != null) {
            Demon1BatEntity bat = new Demon1BatEntity(this.level(), this, target);

            double dX = target.getX() - this.getX();
            double dY = target.getY(0.5D) - bat.getY();
            double dZ = target.getZ() - this.getZ();

            // 예시: 속도를 0.7F 로 감소
            bat.shoot(dX, dY, dZ, 0.7F, 1.0F);
            this.level().addFreshEntity(bat);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide) {
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
            return demon.resolveLeader() != null && demon.leader.isAlive() && demon.distanceToSqr(demon.leader) > 400.0D;
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
        this.calculateEntityAnimation(false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    private PlayState predicate(AnimationState<Demon1Entity> event) {
        if (this.entityData.get(IS_ATTACKING)) {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("attack1"));
        } else {
            event.getController().setAnimation(RawAnimation.begin().thenLoop("fly"));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}
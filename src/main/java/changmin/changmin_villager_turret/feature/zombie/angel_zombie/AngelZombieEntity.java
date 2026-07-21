package changmin.changmin_villager_turret.feature.zombie.angel_zombie;

import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import changmin.changmin_villager_turret.ally.IAlly;
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

import javax.annotation.Nullable;
import java.util.EnumSet;

public class AngelZombieEntity extends Monster implements IAnimatable, IZombieTribe {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    @Nullable
    private LivingEntity leader = null; // 💡 특정 보스가 아닌 일반 LivingEntity로 변경
    public int attackTimer = 0;

    public AngelZombieEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    // 💡 주인을 설정하는 메서드 (사도가 소환할 때만 호출됨)
    public void setLeader(@Nullable LivingEntity leader) {
        this.leader = leader;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void registerGoals() {
        // 💡 주인 곁을 지키는 AI (주인이 없으면 자동으로 무시됨)
        this.goalSelector.addGoal(1, new ReturnToLeaderGoal(this));

        this.goalSelector.addGoal(2, new AngelRangedAttackGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, (entity) -> entity instanceof IAlly));
    }

    // --- 호위/귀환 AI (독립 클래스로 분리) ---
    static class ReturnToLeaderGoal extends Goal {
        private final AngelZombieEntity angel;

        public ReturnToLeaderGoal(AngelZombieEntity angel) {
            this.angel = angel;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            // 💡 핵심: 주인이 설정되어 있고, 살아있으며, 거리가 20블록(400.0D) 이상일 때만 작동
            return angel.leader != null && angel.leader.isAlive() && angel.distanceToSqr(angel.leader) > 400.0D;
        }

        @Override
        public void tick() {
            if (angel.leader != null) {
                // 주인 머리 위로 빠르게 귀환
                angel.getNavigation().moveTo(angel.leader.getX(), angel.leader.getY() + 6, angel.leader.getZ(), 1.5D);
            }
        }
    }

    // --- 나머지 로직 (동일) ---
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackTimer > 0) this.attackTimer--;
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
    protected PathNavigation createNavigation(Level level) { return new FlyingPathNavigation(this, level); }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.attackTimer > 0) {
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
    public AnimationFactory getFactory() { return factory; }
}
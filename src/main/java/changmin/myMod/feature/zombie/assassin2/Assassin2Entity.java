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
    // 애니메이션 재생 유무를 클라이언트와 동기화하기 위한 데이터 와처 변수
    private static final EntityDataAccessor<Boolean> DATA_IS_ATTACKING = SynchedEntityData.defineId(Assassin2Entity.class, EntityDataSerializers.BOOLEAN);

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public Assassin2Entity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);

        // 💡 [추가] 자동 언덕 오르기 높이를 1.0블록으로 상향합니다.
        // 이제 1블록 높이의 경사로나 장애물을 점프 딜레이 없이 미끄러지듯 민첩하게 타고 넘어갑니다.
        this.maxUpStep = 1.0F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)       // 기본 체력 30
                .add(Attributes.ATTACK_DAMAGE, 4.0D)     // 근접 공격력 4
                .add(Attributes.MOVEMENT_SPEED, 0.28D)   // 암살자 콘셉트에 맞춰 보스 좀비보다 살짝 빠르게 설정
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_ATTACKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new Assassin2AttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        // 🎯 [타겟 1] 💡 [추가] 나를 선제 타격한 대상을 우선적으로 반격 (반격 우선순위 1단계)
        // 어차피 좀비 진형의 타격과 타겟 지정은 이벤트 핸들러에서 원천 차단되므로 동족 간에는 발동하지 않습니다.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));

        // 🎯 [타겟 2] 플레이어, 주민, 주민 터렛을 포함한 모든 아군 진형을 타겟 지정 (우선순위 2단계)
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this,
                LivingEntity.class,
                10,
                true,
                false,
                IAlly::isAllyEntity
        ));
    }

    public boolean isAttacking() {
        return this.entityData.get(DATA_IS_ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(DATA_IS_ATTACKING, attacking);
    }

    // GeckoLib 애니메이션 컨트롤러 로직
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        // 1. 공격 상태일 때 'attack' 애니메이션을 1회 우선 재생
        if (this.isAttacking()) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("attack"));
            return PlayState.CONTINUE;
        }

        // 2. 이동 중일 때 'walk' 애니메이션을 반복 재생
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().loop("walk"));
            return PlayState.CONTINUE;
        }

        // 3. 정지 상태일 때는 애니메이션을 멈춰 기본 포즈로 대기 (제공된 JSON에 idle이 없기 때문)
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
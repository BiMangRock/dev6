package changmin.changmin_villager_turret.feature.zombie.healer_zombie;

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

public class HealerZombieEntity extends PathfinderMob implements IAnimatable, IZombieTribe {
    private static final EntityDataAccessor<Integer> DATA_LEVEL = SynchedEntityData.defineId(HealerZombieEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_XP = SynchedEntityData.defineId(HealerZombieEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ANIMATION = SynchedEntityData.defineId(HealerZombieEntity.class, EntityDataSerializers.INT);

    // 레벨업 시 최대 체력 증가 수치를 누적 적용할 UUID
    public static final UUID HEALER_GROWTH_ID = UUID.fromString("2d3e4f5a-6b7c-8d9e-0f1a-2b3c4d5e6f7a");

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int animationTick = 0;

    public HealerZombieEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D) // 기본 체력 30
                .add(Attributes.ATTACK_DAMAGE, 0.0D) // 공격력 없음
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_LEVEL, 1);
        this.entityData.define(DATA_XP, 0);
        this.entityData.define(DATA_ANIMATION, 0);
    }

    @Override
    protected float getJumpPower() {
        return 1.0F; // 점프력 1로 고정
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            // 애니메이션 타이머 감소 처리
            if (this.animationTick > 0) {
                this.animationTick--;
                if (this.animationTick <= 0) {
                    this.setAnimationState(0); // 애니메이션 종료 시 아이들 상태로 복구
                }
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this)); // 🌊 물에 뜨는 목표 (익사 방지 및 수영)
        this.goalSelector.addGoal(1, new HealerZombiePanicGoal(this, 1.8D)); // 피격 시 도망치는 행동
        this.goalSelector.addGoal(2, new HealerZombieActionGoal(this)); // 아군 회복 행동
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D)); // 배회 행동
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F)); // 플레이어 바라보기
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this)); // 무작위 두리번거리기
    }

    public void addXp(int amount) {
        this.setXp(this.getXp() + amount);
        while (this.getXp() >= this.getNeededXp()) {
            this.setXp(this.getXp() - this.getNeededXp());
            this.setHealerLevel(this.getHealerLevel() + 1);
            this.updateMaxHealthAttribute(); // 레벨업 시 최대 체력 증가 적용 (즉시 회복하지 않음)
        }
    }

    private void updateMaxHealthAttribute() {
        var attribute = this.getAttribute(Attributes.MAX_HEALTH);
        if (attribute != null) {
            attribute.removeModifier(HEALER_GROWTH_ID);
            double levelBonus = (getHealerLevel() - 1) * 5.0D; // 레벨업당 최대 체력 +5
            attribute.addPermanentModifier(new AttributeModifier(HEALER_GROWTH_ID, "Healer Growth", levelBonus, AttributeModifier.Operation.ADDITION));
        }
    }

    public void triggerHealAnimation() {
        this.setAnimationState(1);
        this.animationTick = 40; // 2초 (40틱) 동안 애니메이션 활성화
    }

    // Getters & Setters
    public void setAnimationState(int state) { this.entityData.set(DATA_ANIMATION, state); }
    public int getAnimationState() { return this.entityData.get(DATA_ANIMATION); }

    // 바닐라 Entity.getLevel()과의 이름 충돌을 피하기 위해 getHealerLevel로 변경
    public int getHealerLevel() { return this.entityData.get(DATA_LEVEL); }
    public void setHealerLevel(int level) { this.entityData.set(DATA_LEVEL, level); }

    public int getXp() { return this.entityData.get(DATA_XP); }
    public void setXp(int xp) { this.entityData.set(DATA_XP, xp); }

    public int getNeededXp() { return this.getHealerLevel() * 5; } // 레벨당 필요 경험치 5씩 증가

    public float getHealAmount() { return 5.0F * this.getHealerLevel(); } // 5, 10, 15... 레벨에 비례한 회복량
    public float getHealRange() { return 10.0F + (this.getHealerLevel() * 0.5F); }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.getAnimationState() == 1) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("heal"));
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
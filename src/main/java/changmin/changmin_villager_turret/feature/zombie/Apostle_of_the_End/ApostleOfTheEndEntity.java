package changmin.changmin_villager_turret.feature.zombie.Apostle_of_the_End;

import changmin.changmin_villager_turret.feature.zombie.angel_zombie.AngelZombieEntity;
import changmin.changmin_villager_turret.registry.ModEntityTypes;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import changmin.changmin_villager_turret.ally.IAlly;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
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

import java.util.ArrayList;
import java.util.List;

public class ApostleOfTheEndEntity extends Monster implements IAnimatable, IZombieTribe {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private static final EntityDataAccessor<Integer> DATA_SUMMON_TIME = SynchedEntityData.defineId(ApostleOfTheEndEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ANGEL_COUNT = SynchedEntityData.defineId(ApostleOfTheEndEntity.class, EntityDataSerializers.INT);
    public static final int MAX_SUMMON_COOLDOWN = 600;
    private final List<AngelZombieEntity> summonedAngels = new ArrayList<>();
    public int attackTimer = 0;

    public ApostleOfTheEndEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SUMMON_TIME, MAX_SUMMON_COOLDOWN);
        this.entityData.define(DATA_ANGEL_COUNT, 0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ApostleCirclingAttackGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, (entity) -> entity instanceof IAlly));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            summonedAngels.removeIf(angel -> !angel.isAlive());
            this.entityData.set(DATA_ANGEL_COUNT, summonedAngels.size());

            int currentTimer = this.entityData.get(DATA_SUMMON_TIME);
            if (currentTimer > 0) {
                this.entityData.set(DATA_SUMMON_TIME, currentTimer - 1);
            } else {
                if (this.getSummonedAngelsCount() < 3) summonAngel();
                this.entityData.set(DATA_SUMMON_TIME, MAX_SUMMON_COOLDOWN);
            }
        }
        if (this.attackTimer > 0) this.attackTimer--;
    }

    // ... Apostle 클래스 내부 소환 메서드 중 일부
    private void summonAngel() {
        if (this.level instanceof ServerLevel serverLevel) {
            AngelZombieEntity angel = (AngelZombieEntity) ModEntityTypes.ANGEL_ZOMBIE.get().spawn(
                    serverLevel, null, null, this.blockPosition().above(),
                    MobSpawnType.MOB_SUMMONED, true, false
            );

            if (angel != null) {
                // 💡 여기서만 주인을 설정함!
                // 스폰 알로 소환된 천사는 이 과정을 거치지 않으므로 자유로운 야생 상태가 됨.
                angel.setLeader(this);

                summonedAngels.add(angel);
                this.entityData.set(DATA_ANGEL_COUNT, summonedAngels.size());
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level.isClientSide) {
            // 💡 사도가 공격받으면 모든 천사에게 공격자를 알림 (호위 모드)
            if (source.getEntity() instanceof LivingEntity attacker) {
                for (AngelZombieEntity angel : summonedAngels) {
                    if (angel.isAlive()) {
                        angel.setTarget(attacker);
                    }
                }
            }

            // 무적 상태 체크
            if (this.getSummonedAngelsCount() > 0) {
                if (source.getEntity() instanceof Player player) {
                    player.displayClientMessage(new TextComponent("§e[경고] §f천사가 보호하고 있어 §c무적§f 상태입니다!"), true);
                    this.level.playSound(null, this.getX(), this.getY(), this.getZ(), net.minecraft.sounds.SoundEvents.SHIELD_BLOCK, this.getSoundSource(), 1.0F, 1.0F);
                }
                return false;
            }
        }
        return super.hurt(source, amount);
    }

    public int getSummonTimer() { return this.entityData.get(DATA_SUMMON_TIME); }
    public int getSummonedAngelsCount() { return this.entityData.get(DATA_ANGEL_COUNT); }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.attackTimer > 0) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("attack"));
        } else if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().loop("walk"));
        } else {
            event.getController().setAnimation(new AnimationBuilder().loop("idle"));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 2, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() { return factory; }
}
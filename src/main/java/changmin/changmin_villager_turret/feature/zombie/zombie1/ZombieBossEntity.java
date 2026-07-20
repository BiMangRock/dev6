package changmin.changmin_villager_turret.feature.zombie.zombie1;

import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import changmin.changmin_villager_turret.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.syncher.EntityDataAccessor;            // 동기화 변수 관리 클래스
import net.minecraft.network.syncher.EntityDataSerializers;        // 동기화 변수 직렬화 클래스
import net.minecraft.network.syncher.SynchedEntityData;            // 데이터 와처 클래스
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class ZombieBossEntity extends Monster implements IAnimatable, IZombieTribe {
    // 렌더러와 서버 연산을 실시간으로 강제 동기화하는 데이터 감시자 변수(EntityDataAccessor) 선언 [1]
    private static final EntityDataAccessor<Integer> DATA_BOSS_LEVEL = SynchedEntityData.defineId(ZombieBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CURRENT_XP = SynchedEntityData.defineId(ZombieBossEntity.class, EntityDataSerializers.INT);

    private int activeAttack = 0;
    private int attackTick = 0;

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public ZombieBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
    }

    // 자바 기본 메모리에만 상주하던 동기화용 메모리를 마인크래프트 데이터 와처 시스템에 등재합니다 [1].
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BOSS_LEVEL, 1); // 기본 보스 레벨 1 등록
        this.entityData.define(DATA_CURRENT_XP, 0); // 기본 처치 경험치 0 등록
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<LivingEntity>(
                this,
                LivingEntity.class,
                10,
                true,
                false,
                IAlly.class::isInstance
        ));

        this.goalSelector.addGoal(1, new BossPatternGoal(this));

        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level.isClientSide) {
            ZombieBossAttackHelper.handleAttackPatterns(this);
            ZombieBossAttackHelper.handlePassiveGimmick(this);
        }
    }

    public int getNeededXp() {
        return this.getBossLevel() * 5; // 동기화 게터 활용
    }

    // 경험치 누적 및 연산을 동기화 변수(EntityData) 기반으로 가동합니다 [1].
    public void recordKill() {
        int nextXp = this.getCurrentXp() + 1;
        this.setCurrentXp(nextXp);
        if (nextXp >= getNeededXp()) {
            levelUp();
        }
    }

    // 레벨업 시 내부 데이터 동기화와 속성 수치를 정확하게 갱신합니다 [1].
    private void levelUp() {
        this.setCurrentXp(0);
        int nextLevel = this.getBossLevel() + 1;
        this.setBossLevel(nextLevel);

        double newMaxHealth = 50.0D + (nextLevel - 1) * 10.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        this.setHealth((float) newMaxHealth);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean hitByPlayer) {
        super.dropCustomDeathLoot(source, looting, hitByPlayer);

        float rewardMultiplier = 1.0F + (this.getBossLevel() - 1) * 0.50F;

        int baseDiamond = this.random.nextInt(5) + 1;
        int finalDiamond = Math.round(baseDiamond * rewardMultiplier);
        this.spawnAtLocation(new ItemStack(Blocks.DIAMOND_BLOCK, finalDiamond));

        int baseTokens = this.random.nextInt(5) + 1;
        int finalTokens = Math.round(baseTokens * rewardMultiplier);

        if (this.getBossLevel() % 5 == 0) {
            finalTokens += 1;
        }

        // 🆕 [수정] TURRET_POINT_TOKEN 대신 TURRET_POINT_TOKEN_MID (중급 토큰)을 드롭하도록 변경
        this.spawnAtLocation(new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), finalTokens));
    }

    public void triggerAttack2() {
        if (this.activeAttack == 0) {
            this.activeAttack = 2;
            this.attackTick = 0;
        }
    }

    public void triggerAttack3() {
        if (this.activeAttack == 0) {
            this.activeAttack = 3;
            this.attackTick = 0;
        }
    }

    public void triggerAttack5() {
        if (this.activeAttack == 0) {
            this.activeAttack = 5;
            this.attackTick = 0;
        }
    }

    // 렌더러가 접근하는 Getter와 Setter를 와처(EntityData) 기반으로 고쳐 실시간 반영을 이끌어냅니다.
    public int getBossLevel() { return this.entityData.get(DATA_BOSS_LEVEL); }
    public void setBossLevel(int level) { this.entityData.set(DATA_BOSS_LEVEL, level); }
    public int getCurrentXp() { return this.entityData.get(DATA_CURRENT_XP); }
    public void setCurrentXp(int xp) { this.entityData.set(DATA_CURRENT_XP, xp); }

    public int getActiveAttack() { return this.activeAttack; }
    public void setActiveAttack(int activeAttack) { this.activeAttack = activeAttack; }
    public int getAttackTick() { return this.attackTick; }
    public void setAttackTick(int attackTick) { this.attackTick = attackTick; }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.activeAttack == 2) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("attack2"));
            return PlayState.CONTINUE;
        } else if (this.activeAttack == 3) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("attack3"));
            return PlayState.CONTINUE;
        } else if (this.activeAttack == 5) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("attack5"));
            return PlayState.CONTINUE;
        }

        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().loop("walk"));
            return PlayState.CONTINUE;
        }

        event.getController().setAnimation(new AnimationBuilder().loop("idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
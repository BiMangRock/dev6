package changmin.myMod.feature.zombie.zombie1;

import changmin.myMod.ally.IAlly;
import changmin.myMod.zombieTribe.IZombieTribe;
import changmin.myMod.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class ZombieBossEntity extends Monster implements IAnimatable, IZombieTribe {
    private int activeAttack = 0;
    private int attackTick = 0;

    private int bossLevel = 1;
    private int currentXp = 0;

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public ZombieBossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        // [타겟 탐색 시스템] 플레이어와 아군(IAlly) 모두 탐색 대상에 등록합니다 [1].
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));

        // 제네릭 및 메서드 참조를 사용하여 타입 안전성을 높였습니다.
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<LivingEntity>(
                this,
                LivingEntity.class,
                10,
                true,
                false,
                IAlly.class::isInstance
        ));

        // [우선순위 1] 타겟이 존재할 때의 원거리 시전 모드
        this.goalSelector.addGoal(1, new BossPatternGoal(this));

        // 💡 [수정 완료] 아군 구경용 LookAtGoal 라인을 지우고 표준 생성자 규칙을 준수하도록 정돈했습니다.
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D)); // 주변 돌아다니기
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F)); // 플레이어 구경하기
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this)); // 주위 두리번거리기
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level.isClientSide) {
            ZombieBossAttackHelper.handleAttackPatterns(this);
        }
    }

    public int getNeededXp() {
        return this.bossLevel * 5;
    }

    public void recordKill() {
        this.currentXp++;
        if (this.currentXp >= getNeededXp()) {
            levelUp();
        }
    }

    private void levelUp() {
        this.currentXp = 0;
        this.bossLevel++;

        double newMaxHealth = 50.0D + (this.bossLevel - 1) * 10.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        this.setHealth((float) newMaxHealth);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean hitByPlayer) {
        super.dropCustomDeathLoot(source, looting, hitByPlayer);

        float rewardMultiplier = 1.0F + (this.bossLevel - 1) * 0.50F;

        int baseDiamond = this.random.nextInt(5) + 1;
        int finalDiamond = Math.round(baseDiamond * rewardMultiplier);
        this.spawnAtLocation(new ItemStack(Blocks.DIAMOND_BLOCK, finalDiamond));

        int baseTokens = this.random.nextInt(5) + 1;
        int finalTokens = Math.round(baseTokens * rewardMultiplier);

        if (this.bossLevel % 5 == 0) {
            finalTokens += 1;
        }

        this.spawnAtLocation(new ItemStack(ModItems.TURRET_POINT_TOKEN.get(), finalTokens));
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

    public void triggerAttack4() {
        if (this.activeAttack == 0) {
            this.activeAttack = 4;
            this.attackTick = 0;
        }
    }

    public int getActiveAttack() { return this.activeAttack; }
    public void setActiveAttack(int activeAttack) { this.activeAttack = activeAttack; }
    public int getAttackTick() { return this.attackTick; }
    public void setAttackTick(int attackTick) { this.attackTick = attackTick; }
    public int getBossLevel() { return this.bossLevel; }
    public int getCurrentXp() { return this.currentXp; }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.activeAttack == 2) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("attack2"));
            return PlayState.CONTINUE;
        } else if (this.activeAttack == 3) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("attack3"));
            return PlayState.CONTINUE;
        } else if (this.activeAttack == 4) {
            event.getController().setAnimation(new AnimationBuilder().playOnce("attack4"));
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
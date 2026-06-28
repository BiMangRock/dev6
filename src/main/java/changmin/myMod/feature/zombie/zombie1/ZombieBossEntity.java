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
    private int activeAttack = 0;       // 0: 평소, 2: 정밀화살, 3: 구형나선, 5: 제자리360도
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

    // 🆕 공격 5 트리거 선언
    public void triggerAttack5() {
        if (this.activeAttack == 0) {
            this.activeAttack = 5;
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
        } else if (this.activeAttack == 5) {
            // 🆕 공격 5 전용 애니메이션 작동
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
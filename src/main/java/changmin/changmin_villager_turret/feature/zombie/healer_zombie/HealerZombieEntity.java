package changmin.changmin_villager_turret.feature.zombie.healer_zombie;

import changmin.changmin_villager_turret.zombieTribe.IZombieTribe;
import net.minecraft.nbt.CompoundTag; // 🆕 추가
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

    public static final UUID HEALER_GROWTH_ID = UUID.fromString("2d3e4f5a-6b7c-8d9e-0f1a-2b3c4d5e6f7a");

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int animationTick = 0;

    public HealerZombieEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
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
        return 1.0F;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            if (this.animationTick > 0) {
                this.animationTick--;
                if (this.animationTick <= 0) {
                    this.setAnimationState(0);
                }
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this)); // 🌊 물에 뜨는 목표 최우선 배치
        this.goalSelector.addGoal(1, new HealerZombiePanicGoal(this, 1.8D));
        this.goalSelector.addGoal(2, new HealerZombieActionGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    // 💡 힐러 저장 데이터를 NBT 파일에 안전하게 기록하는 메서드
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HealerLevel", this.getHealerLevel());
        tag.putInt("HealerXp", this.getXp());
    }

    // 💡 힐러 불러오기 시 데이터를 복구하고 최대 체력 속성을 계산하는 메서드
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HealerLevel", 99)) {
            this.setHealerLevel(tag.getInt("HealerLevel"));
        }
        if (tag.contains("HealerXp", 99)) {
            this.setXp(tag.getInt("HealerXp"));
        }
        this.updateMaxHealthAttribute();
    }

    public void addXp(int amount) {
        this.setXp(this.getXp() + amount);
        while (this.getXp() >= this.getNeededXp()) {
            this.setXp(this.getXp() - this.getNeededXp());
            this.setHealerLevel(this.getHealerLevel() + 1);
            this.updateMaxHealthAttribute();
        }
    }

    public void updateMaxHealthAttribute() {
        var attribute = this.getAttribute(Attributes.MAX_HEALTH);
        if (attribute != null) {
            attribute.removeModifier(HEALER_GROWTH_ID);
            double levelBonus = (getHealerLevel() - 1) * 5.0D;
            attribute.addPermanentModifier(new AttributeModifier(HEALER_GROWTH_ID, "Healer Growth", levelBonus, AttributeModifier.Operation.ADDITION));
        }
    }

    public void triggerHealAnimation() {
        this.setAnimationState(1);
        this.animationTick = 40;
    }

    public void setAnimationState(int state) { this.entityData.set(DATA_ANIMATION, state); }
    public int getAnimationState() { return this.entityData.get(DATA_ANIMATION); }

    public int getHealerLevel() { return this.entityData.get(DATA_LEVEL); }
    public void setHealerLevel(int level) { this.entityData.set(DATA_LEVEL, level); }

    public int getXp() { return this.entityData.get(DATA_XP); }
    public void setXp(int xp) { this.entityData.set(DATA_XP, xp); }

    public int getNeededXp() { return this.getHealerLevel() * 5; }

    public float getHealAmount() { return 5.0F * this.getHealerLevel(); }
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
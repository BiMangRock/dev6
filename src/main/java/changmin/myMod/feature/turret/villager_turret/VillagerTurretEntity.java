package changmin.myMod.feature.turret.villager_turret;

import changmin.myMod.ally.IAlly; // 아군 인터페이스 임포트 추가
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

// implements IAlly 추가하여 아군 시스템에 통합
public class VillagerTurretEntity extends PathfinderMob implements RangedAttackMob, IAlly {

    public static final float ARROW_SPEED = 0.8F;
    public static final double ARROW_DAMAGE = 2.0D;

    private static final EntityDataAccessor<Integer> TURRET_LEVEL = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> XP = SynchedEntityData.defineId(VillagerTurretEntity.class, EntityDataSerializers.INT);

    public VillagerTurretEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TURRET_LEVEL, 1);
        this.entityData.define(XP, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.25D, 40, 20.0F));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Zombie.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public void killed(ServerLevel level, LivingEntity killedEntity) {
        super.killed(level, killedEntity);
        if (killedEntity instanceof Zombie) {
            this.addXp(1);
        }
    }

    public void addXp(int amount) {
        int currentXp = this.entityData.get(XP) + amount;
        int currentLvl = this.getTurretLevel();
        int neededXp = currentLvl;

        if (currentXp >= neededXp) {
            currentXp -= neededXp;
            currentLvl++;

            this.setTurretLevel(currentLvl);
            this.entityData.set(XP, currentXp);

            double newMaxHealth = 10.0D + (currentLvl - 1) * 5.0D;
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);

            this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
        } else {
            this.entityData.set(XP, currentXp);
        }
    }

    public int getTurretLevel() {
        return this.entityData.get(TURRET_LEVEL);
    }

    public void setTurretLevel(int level) {
        this.entityData.set(TURRET_LEVEL, level);
    }

    public int getXp() {
        return this.entityData.get(XP);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TurretLevel", this.getTurretLevel());
        tag.putInt("TurretXP", this.getXp());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TurretLevel")) {
            this.setTurretLevel(tag.getInt("TurretLevel"));
        }
        if (tag.contains("TurretXP")) {
            this.entityData.set(XP, tag.getInt("TurretXP"));
        }
        double loadedMax = 10.0D + (this.getTurretLevel() - 1) * 5.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(loadedMax);
    }

    @Override
    public Component getDisplayName() {
        return new TextComponent("주민 터렛");
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        ItemStack arrowStack = new ItemStack(Items.ARROW);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, arrowStack, velocity);

        arrow.setBaseDamage(ARROW_DAMAGE);

        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333D) - arrow.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        arrow.shoot(d0, d1 + d3 * 0.2D, d2, ARROW_SPEED, (float)(14 - this.level.getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(arrow);
    }
}
package changmin.myMod.feature.turret.plasma_wizard;

import changmin.myMod.ally.IAlly;
import changmin.myMod.registry.ModEntityTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PlasmaWizardEntity extends PathfinderMob implements IAlly {
    private int attackCooldown = 0;

    public PlasmaWizardEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new PlasmaWizardTargetGoal<>(this, LivingEntity.class));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double distanceSqr = this.distanceToSqr(target);
                double maxRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);

                if (distanceSqr <= maxRange * maxRange) {
                    this.getLookControl().setLookAt(target, 30.0F, 30.0F);
                    if (this.attackCooldown <= 0) {
                        shootPlasmaOrb(target);
                        this.attackCooldown = 50; // 기본 공격 주기 2.5초
                    }
                }
            }
        }
    }

    private void shootPlasmaOrb(LivingEntity target) {
        PlasmaOrbEntity projectile = new PlasmaOrbEntity(this.level, this);
        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333D) - projectile.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        projectile.shoot(d0, d1 + d3 * 0.18D, d2, 1.5F, 0.0F);
        this.level.addFreshEntity(projectile);
        this.playSound(SoundEvents.TRIDENT_THROW, 1.0F, 0.8F);
    }

    @Override public Component getDisplayName() { return new TextComponent("플라즈마 마법사 주민 터렛"); }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(net.minecraft.world.entity.Entity entity) {}
}
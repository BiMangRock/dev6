package changmin.myMod.feature.turret.villager_turret;

import changmin.myMod.MyMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TurretShooter {

    public static void shootTarget(VillagerTurretEntity turret, LivingEntity target) {
        int pattern = turret.getArrowPattern();
        int arrowCount = turret.getTurretLevel();
        Level level = turret.level;

        if (pattern == 2) {
            for (int i = 0; i < arrowCount; i++) {
                shootSingleArrow(turret, target, VillagerTurretEntity.ARROW_SPEED + (i * 0.15F), 0.0F);
            }
        } else if (pattern == 1) {
            for (int i = 0; i < arrowCount; i++) {
                float baseSpread = (float)(14 - level.getDifficulty().getId() * 4);
                float angleOffset = (arrowCount > 1) ? (i - (arrowCount - 1) / 2.0F) * 4.0F : 0.0F;
                shootSingleArrow(turret, target, VillagerTurretEntity.ARROW_SPEED, baseSpread + angleOffset);
            }
        } else {
            shootSingleArrow(turret, target, VillagerTurretEntity.ARROW_SPEED, (float)(14 - level.getDifficulty().getId() * 4));
        }

        turret.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (turret.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    private static void shootSingleArrow(VillagerTurretEntity turret, LivingEntity target, float speed, float spread) {
        ItemStack arrowStack;
        int type = turret.getArrowType();

        if (type == 1) {
            arrowStack = new ItemStack(Items.TIPPED_ARROW);
            PotionUtils.setPotion(arrowStack, Potions.POISON);
        } else if (type == 2) {
            arrowStack = new ItemStack(Items.TIPPED_ARROW);
            PotionUtils.setPotion(arrowStack, Potions.WEAKNESS);
        } else if (type == 3) {
            arrowStack = new ItemStack(Items.TIPPED_ARROW);
            PotionUtils.setPotion(arrowStack, Potions.SLOWNESS);
        } else {
            arrowStack = new ItemStack(Items.ARROW);
        }

        AbstractArrow arrow = ProjectileUtil.getMobArrow(turret, arrowStack, 1.0F);
        arrow.setBaseDamage(VillagerTurretEntity.ARROW_DAMAGE);

        arrow.addTag("turret_arrow");

        if (turret.getPassBlocksEnabled() == 1) {
            arrow.addTag("pass_blocks");
        }

        double d0 = target.getX() - turret.getX();
        double d1 = target.getY(0.3333333333333333D) - arrow.getY();
        double d2 = target.getZ() - turret.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        float finalSpeed = speed + (turret.getSpeedLevel() * 0.15F);

        if (turret.getNoGravityEnabled() == 1) {
            arrow.setNoGravity(true);
            arrow.shoot(d0, d1, d2, finalSpeed, spread);
        } else {
            arrow.setNoGravity(false);
            arrow.shoot(d0, d1 + d3 * 0.2D, d2, finalSpeed, spread);
        }

        turret.level.addFreshEntity(arrow);
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof AbstractArrow arrow) {
            Level level = arrow.level;

            if (arrow.getTags().contains("turret_arrow")) {
                HitResult result = event.getRayTraceResult();
                boolean hasPassTag = arrow.getTags().contains("pass_blocks");

                if (hasPassTag) {
                    if (result.getType() == HitResult.Type.BLOCK) {
                        event.setCanceled(true); // 물리학 충돌 상쇄

                        Vec3 hitLoc = result.getLocation();
                        Vec3 motion = arrow.getDeltaMovement().normalize();

                        double prevX = arrow.getX();
                        double prevY = arrow.getY();
                        double prevZ = arrow.getZ();

                        double warpDist = 0.5;
                        boolean foundOpenSpace = false;
                        double finalX = hitLoc.x;
                        double finalY = hitLoc.y;
                        double finalZ = hitLoc.z;

                        while (warpDist < 16.0) {
                            double testX = hitLoc.x + motion.x * warpDist;
                            double testY = hitLoc.y + motion.y * warpDist;
                            double testZ = hitLoc.z + motion.z * warpDist;
                            net.minecraft.core.BlockPos testPos = new net.minecraft.core.BlockPos(testX, testY, testZ);

                            if (level.getBlockState(testPos).getCollisionShape(level, testPos).isEmpty()) {
                                finalX = testX;
                                finalY = testY;
                                finalZ = testZ;
                                foundOpenSpace = true;
                                break;
                            }
                            warpDist += 0.5;
                        }

                        if (foundOpenSpace) {
                            Vec3 finalLoc = new Vec3(finalX, finalY, finalZ);

                            // 1. 궤적 간 몹 타격 가로채기 알고리즘
                            AABB pathBox = new AABB(hitLoc, finalLoc).inflate(0.5);
                            LivingEntity hitTarget = null;
                            double closestDist = Double.MAX_VALUE;

                            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, pathBox)) {
                                if (entity != arrow.getOwner() && entity.isAlive()) {
                                    double dist = entity.distanceToSqr(hitLoc);
                                    if (dist < closestDist) {
                                        closestDist = dist;
                                        hitTarget = entity;
                                    }
                                }
                            }

                            if (hitTarget != null) {
                                Entity owner = arrow.getOwner();
                                DamageSource damageSource = owner == null ? DamageSource.arrow(arrow, arrow) : DamageSource.arrow(arrow, owner);

                                int knockback = arrow.getKnockback();
                                if (knockback > 0) {
                                    Vec3 vec3 = arrow.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double)knockback * 0.6D);
                                    if (vec3.lengthSqr() > 0.0D) {
                                        hitTarget.push(vec3.x, 0.1D, vec3.z);
                                    }
                                }

                                if (hitTarget.hurt(damageSource, (float)arrow.getBaseDamage())) {
                                    // [오류 수정] protected 필드인 random 대신 공용 게터인 arrow.getRandom()을 호출해 충돌을 방지합니다.
                                    arrow.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (arrow.level.random.nextFloat() * 0.2F + 0.9F));
                                    arrow.discard();
                                }
                                return;
                            }

                            // 2. 관통 워프 공백 궤적 파티클 트레일 선명화
                            if (level instanceof ServerLevel serverLevel) {
                                double dist = hitLoc.distanceTo(finalLoc);
                                int particleCount = (int) (dist * 4);
                                for (int i = 0; i <= particleCount; i++) {
                                    double t = (double) i / particleCount;
                                    double px = hitLoc.x + (finalLoc.x - hitLoc.x) * t;
                                    double py = hitLoc.y + (finalLoc.y - hitLoc.y) * t;
                                    double pz = hitLoc.z + (finalLoc.z - hitLoc.z) * t;
                                    serverLevel.sendParticles(ParticleTypes.CRIT, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
                                }
                            }

                            arrow.teleportTo(finalX, finalY, finalZ);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.world instanceof ServerLevel serverLevel) {
            if (serverLevel.getGameTime() % 20 == 0) {
                for (Entity entity : serverLevel.getAllEntities()) {
                    if (entity instanceof AbstractArrow arrow) {
                        if (arrow.getTags().contains("turret_arrow") && arrow.tickCount > 200) {
                            arrow.discard();
                        }
                    }
                }
            }
        }
    }
}
package changmin.myMod.feature.turret.trident_turret;

import changmin.myMod.MyMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TridentTurretShooter {

    public static void shootTarget(TridentTurretEntity turret, LivingEntity target) {
        int count = turret.getTridentCount();
        int mode = turret.getShootMode();

        if (mode == 1) {
            float totalCone = 60.0F;
            float angleStep = (count > 1) ? totalCone / (count - 1) : 0.0F;

            for (int i = 0; i < count; i++) {
                float angleOffset = (count > 1) ? (i * angleStep - totalCone / 2.0F) : 0.0F;
                shootSingleTrident(turret, target, angleOffset);
            }
        } else {
            for (int i = 0; i < count; i++) {
                float angleOffset = (turret.getRandom().nextFloat() - 0.5F) * 3.0F;
                shootSingleTrident(turret, target, angleOffset);
            }
        }

        turret.playSound(SoundEvents.TRIDENT_THROW, 1.2F, 0.8F / (turret.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    private static void shootSingleTrident(TridentTurretEntity turret, LivingEntity target, float spreadOffset) {
        Level level = turret.level;
        ItemStack tridentStack = new ItemStack(Items.TRIDENT);
        ThrownTrident trident = new ThrownTrident(level, turret, tridentStack);

        double finalDamage = 9.0D + (turret.getDamageLevel() * 2.5D);
        trident.setBaseDamage(finalDamage);
        trident.pickup = AbstractArrow.Pickup.DISALLOWED;

        // 🆕 [추가] 관통 설정 주입 (바닐라 AbstractArrow 관통 물리 코드 활용)
        if (turret.getPierceLevel() > 0) {
            trident.setPierceLevel((byte) turret.getPierceLevel());
        }

        trident.addTag("turret_trident");
        if (turret.getLightningLevel() == 1) {
            trident.addTag("lightning_trident");
        }

        double d0 = target.getX() - turret.getX();
        double d1 = target.getY(0.3333333333333333D) - trident.getY();
        double d2 = target.getZ() - turret.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);

        float throwSpeed = 1.6F + (turret.getRangeLevel() * 0.1F);

        trident.shoot(d0, d1 + d3 * 0.15D, d2, throwSpeed, spreadOffset);
        level.addFreshEntity(trident);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getDirectEntity() instanceof ThrownTrident trident) {
            if (trident.getTags().contains("turret_trident") && trident.getTags().contains("lightning_trident")) {
                LivingEntity victim = event.getEntityLiving();
                Level level = victim.level;

                if (level instanceof ServerLevel serverLevel) {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                    if (bolt != null) {
                        bolt.moveTo(victim.position());
                        bolt.addTag("mymod_friendly_lightning");
                        serverLevel.addFreshEntity(bolt);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
        LightningBolt bolt = event.getLightning();
        if (bolt != null && bolt.getTags().contains("mymod_friendly_lightning")) {
            net.minecraft.world.entity.Entity victim = event.getEntity();

            if (victim instanceof net.minecraft.world.entity.player.Player || victim instanceof changmin.myMod.ally.IAlly) {
                event.setCanceled(true);
            }
        }
    }
}
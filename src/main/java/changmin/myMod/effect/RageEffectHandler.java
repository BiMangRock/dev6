package changmin.myMod.effect;

import changmin.myMod.MyMod;
import changmin.myMod.ally.IAlly;
import changmin.myMod.registry.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster; // 👈 Enemy 대신 Monster 임포트
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RageEffectHandler {

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Mob mob && !mob.level.isClientSide) {
            if (mob.hasEffect(ModEffects.RAGE.get())) {
                LivingEntity newTarget = event.getNewTarget();

                if (newTarget == null || newTarget instanceof Player || newTarget instanceof IAlly) {
                    LivingEntity closestHostile = findClosestHostile(mob);
                    if (closestHostile != null) {
                        event.setNewTarget(closestHostile);
                    }
                }
            }
        }
    }

    private static LivingEntity findClosestHostile(Mob mob) {
        AABB area = mob.getBoundingBox().inflate(10.0D);
        List<Monster> hostiles = mob.level.getEntitiesOfClass(Monster.class, area, target ->
                !(target instanceof IAlly)        // 1. 플레이어 조건은 빼고, 아군 주민 터렛만 제외
                        && target != mob
                        && target.isAlive()
        );

        LivingEntity closestHostile = null;
        double closestDist = Double.MAX_VALUE;

        for (Monster target : hostiles) {
            double dist = mob.distanceToSqr(target);
            if (dist < closestDist) {
                closestDist = dist;
                closestHostile = target;
            }
        }
        return closestHostile;
    }
}
package changmin.myMod.effect;

import changmin.myMod.MyMod;
import changmin.myMod.ally.IAlly;
import changmin.myMod.registry.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal; // 👈 기본 Goal 클래스 임포트
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.EnumSet;
import java.util.List;

@Mod.EventBusSubscriber(modid = MyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RageEffectHandler {

    // [매 틱 최적화 검사] 효과가 있을 때만 주입을 시도하므로 렉이 발생하지 않습니다.
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof Mob mob && !mob.level.isClientSide) {
            if (mob.hasEffect(ModEffects.RAGE.get())) {
                injectRageGoals(mob);
            }
        }
    }

    // 버프 자연 만료 시 제거
    @SubscribeEvent
    public static void onPotionExpired(PotionEvent.PotionExpiryEvent event) {
        if (event.getPotionEffect() != null && event.getPotionEffect().getEffect() == ModEffects.RAGE.get()) {
            if (event.getEntityLiving() instanceof Mob mob && !mob.level.isClientSide) {
                removeRageGoals(mob);
            }
        }
    }

    // 우유 등 강제 삭제 시 제거
    @SubscribeEvent
    public static void onPotionRemoved(PotionEvent.PotionRemoveEvent event) {
        if (event.getPotion() == ModEffects.RAGE.get()) {
            if (event.getEntityLiving() instanceof Mob mob && !mob.level.isClientSide) {
                removeRageGoals(mob);
            }
        }
    }

    // AI 주입 로직
    private static void injectRageGoals(Mob mob) {
        // 1. 공격 AI 주입 (longMemory = true)
        boolean hasAttack = mob.goalSelector.getAvailableGoals().stream()
                .anyMatch(g -> g.getGoal() instanceof RageAttackGoal);
        if (!hasAttack && mob instanceof PathfinderMob pathfinderMob) {
            mob.goalSelector.addGoal(0, new RageAttackGoal(pathfinderMob));
        }

        // 2. 타겟팅 AI 주입 (커스텀 타겟 Goal 사용)
        boolean hasTarget = mob.targetSelector.getAvailableGoals().stream()
                .anyMatch(g -> g.getGoal() instanceof RageTargetGoal);
        if (!hasTarget) {
            mob.targetSelector.addGoal(0, new RageTargetGoal(mob));
        }
    }

    // AI 원상 복구 로직
    private static void removeRageGoals(Mob mob) {
        mob.goalSelector.getAvailableGoals().stream()
                .map(g -> g.getGoal())
                .filter(goal -> goal instanceof RageAttackGoal)
                .findFirst()
                .ifPresent(mob.goalSelector::removeGoal);

        mob.targetSelector.getAvailableGoals().stream()
                .map(g -> g.getGoal())
                .filter(goal -> goal instanceof RageTargetGoal)
                .findFirst()
                .ifPresent(mob.targetSelector::removeGoal);
    }

    // ==========================================
    //       분노 상태 전용 커스텀 AI Goal 정의
    // ==========================================

    // [공격 AI] 밀치기와 충돌 유무에 상관없이 집요하게 경로를 찾아 타격하는 AI
    public static class RageAttackGoal extends MeleeAttackGoal {
        public RageAttackGoal(PathfinderMob pathfinderMob) {
            super(pathfinderMob, 1.2D, true);
        }
    }

    // [타겟팅 AI] 바닐라 제약(동족 구별, 대기 시간)을 완전히 무시하고 즉시 탐색하는 커스텀 타겟 Goal
    public static class RageTargetGoal extends Goal {
        private final Mob mob;

        public RageTargetGoal(Mob mob) {
            this.mob = mob;
            // TARGET 플래그를 할당하여 바닐라 좀비의 일반 타겟팅을 안전하게 억제합니다.
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            // 이미 유효한 아군이 아닌 대상을 타겟팅 중이라면 굳이 새로 연산하지 않고 유지합니다. (최적화)
            LivingEntity currentTarget = this.mob.getTarget();
            if (currentTarget != null && currentTarget.isAlive() && !(currentTarget instanceof IAlly) && currentTarget != this.mob) {
                return false;
            }

            // 10블록 반경 내 가장 가까운 몬스터를 강제 탐색 (질문자님의 핵심 로직 활용)
            AABB area = this.mob.getBoundingBox().inflate(10.0D);
            List<Monster> hostiles = this.mob.level.getEntitiesOfClass(Monster.class, area, target ->
                    target != this.mob
                            && target.isAlive()
                            && !(target instanceof IAlly)
            );

            LivingEntity closestHostile = null;
            double closestDist = Double.MAX_VALUE;

            for (Monster target : hostiles) {
                double dist = this.mob.distanceToSqr(target);
                if (dist < closestDist) {
                    closestDist = dist;
                    closestHostile = target;
                }
            }

            // 가장 가까운 대상을 찾았다면 강제로 타겟으로 등록하고 AI 작동을 시작합니다.
            if (closestHostile != null) {
                this.mob.setTarget(closestHostile);
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            // 타겟이 유효한 살아있는 아군이 아닌 몬스터라면 계속 타겟을 유지합니다.
            LivingEntity currentTarget = this.mob.getTarget();
            return currentTarget != null
                    && currentTarget.isAlive()
                    && !(currentTarget instanceof IAlly)
                    && currentTarget != this.mob;
        }
    }
}
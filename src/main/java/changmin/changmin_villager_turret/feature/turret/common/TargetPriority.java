package changmin.changmin_villager_turret.feature.turret.common;

import changmin.changmin_villager_turret.config.TurretConfig;
import changmin.changmin_villager_turret.feature.zombie.Apostle_of_the_End.ApostleOfTheEndEntity;
import changmin.changmin_villager_turret.feature.zombie.angel_zombie.AngelZombieEntity;
import changmin.changmin_villager_turret.feature.zombie.assassin2.Assassin2Entity;
import changmin.changmin_villager_turret.feature.zombie.creaking.CreakingEntity;
import changmin.changmin_villager_turret.feature.zombie.demon1.Demon1Entity;
import changmin.changmin_villager_turret.feature.zombie.healer_zombie.HealerZombieEntity;
import changmin.changmin_villager_turret.feature.zombie.raged_angel_zombie.RagedAngelZombieEntity;
import changmin.changmin_villager_turret.feature.zombie.zombie1.ZombieBossEntity;
import changmin.changmin_villager_turret.feature.zombie.zombie_commander.ZombieCommanderEntity;
import changmin.changmin_villager_turret.feature.zombie.zombie_commander.ZombieCommanderMaggotEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 🧠 터렛 표적 우선순위 점수 계산
 *
 * 점수 = 위협도 × threatWeight
 *      + (1 - 남은체력비율) × lowHealthWeight
 *      + (한 방에 죽일 수 있으면) finishBonus
 *      - 거리(블록) × distanceWeight
 *      - (이미 다른 터렛이 노리는 수) × focusPenalty
 *
 * 가중치는 모두 config/changmin_villager_turret-common.toml 의 [targeting] 에서 조정할 수 있습니다.
 */
public final class TargetPriority {

    private TargetPriority() {}

    /** 몹 종류별 기본 위협도. 아군을 회복·강화하는 지원형을 가장 먼저 잡도록 높게 둡니다. */
    public static double threatOf(LivingEntity e) {
        if (e instanceof HealerZombieEntity) return 20.0D;       // 좀비 회복 → 최우선
        if (e instanceof ZombieCommanderEntity) return 18.0D;    // 버프 오라 + 구더기 소환
        if (e instanceof ApostleOfTheEndEntity) return 16.0D;    // 보스급
        if (e instanceof ZombieBossEntity) return 15.0D;         // 보스
        if (e instanceof RagedAngelZombieEntity) return 10.0D;
        if (e instanceof Assassin2Entity) return 9.0D;           // 순간이동 암살자
        if (e instanceof Demon1Entity) return 8.0D;
        if (e instanceof AngelZombieEntity) return 7.0D;
        if (e instanceof CreakingEntity) return 6.0D;
        if (e instanceof ZombieCommanderMaggotEntity) return -4.0D; // 곧 사라지는 소환물은 후순위
        return 0.0D;                                             // 일반 좀비
    }

    /**
     * @param shooter       사격하는 터렛
     * @param candidate     후보 표적
     * @param expectedHit   이 터렛의 한 발 예상 피해 (막타 판정용)
     * @param focusCount    이미 이 표적을 노리고 있는 다른 아군 터렛 수
     */
    public static double score(Mob shooter, LivingEntity candidate, double expectedHit, int focusCount) {
        double distance = Math.sqrt(shooter.distanceToSqr(candidate));
        double maxHp = Math.max(1.0D, candidate.getMaxHealth());
        double hpRatio = candidate.getHealth() / maxHp;

        double s = 0.0D;
        s += threatOf(candidate) * TurretConfig.threatWeight();
        s += (1.0D - hpRatio) * TurretConfig.lowHealthWeight();
        if (expectedHit > 0.0D && candidate.getHealth() <= expectedHit) {
            s += TurretConfig.finishBonus();
        }
        s -= distance * TurretConfig.distanceWeight();
        s -= focusCount * TurretConfig.focusPenalty();
        return s;
    }

    /**
     * 후보 중 점수가 가장 높은 표적을 고릅니다.
     * 같은 종류의 아군 터렛(turretClass)이 이미 노리는 표적은 감점해 화력이 분산되게 합니다.
     */
    @Nullable
    public static <T extends LivingEntity> T pickBest(Mob shooter, List<T> candidates, double expectedHit,
                                                     Class<? extends Mob> turretClass, double searchRange) {
        if (candidates.isEmpty()) return null;

        // 주변 아군 터렛 목록은 한 번만 조회 (후보마다 검색하지 않음)
        AABB area = shooter.getBoundingBox().inflate(searchRange);
        List<? extends Mob> allies = shooter.level.getEntitiesOfClass(turretClass, area, m -> m != shooter && m.isAlive());

        T best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (T candidate : candidates) {
            int focus = 0;
            for (Mob ally : allies) {
                if (ally.getTarget() == candidate) focus++;
            }
            double sc = score(shooter, candidate, expectedHit, focus);
            if (sc > bestScore) {
                bestScore = sc;
                best = candidate;
            }
        }
        return best;
    }
}

package changmin.changmin_villager_turret.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * ⚙️ 밸런스·성능 설정 (Forge COMMON config)
 *
 * 게임을 한 번 실행하면 아래 파일이 자동 생성됩니다.
 *   - 싱글플레이: .minecraft/config/changmin_villager_turret-common.toml
 *   - 개발 환경(runClient): run/config/changmin_villager_turret-common.toml
 * 파일 값을 바꾼 뒤 게임(또는 월드)을 다시 열면 재빌드 없이 적용됩니다.
 *
 * 값을 읽을 때는 아래 정적 메서드(arrowSpeed() 등)를 사용하세요.
 * 설정이 아직 로드되지 않은 시점에 호출되어도 기본값을 돌려주므로 크래시가 나지 않습니다.
 */
public final class TurretConfig {

    public static final ForgeConfigSpec SPEC;

    // ── 성능 ──
    private static final ForgeConfigSpec.IntValue AREA_SCAN_INTERVAL;

    // ── 주민 터렛(활) ──
    private static final ForgeConfigSpec.DoubleValue ARROW_SPEED;
    private static final ForgeConfigSpec.DoubleValue ARROW_SPEED_PER_LEVEL;
    private static final ForgeConfigSpec.DoubleValue ARROW_DAMAGE;
    private static final ForgeConfigSpec.DoubleValue ARROW_DAMAGE_PER_LEVEL;
    private static final ForgeConfigSpec.IntValue BASE_COOLDOWN;
    private static final ForgeConfigSpec.IntValue COOLDOWN_REDUCTION_PER_LEVEL;
    private static final ForgeConfigSpec.IntValue MIN_COOLDOWN;
    private static final ForgeConfigSpec.DoubleValue ATTACK_RANGE;
    private static final ForgeConfigSpec.IntValue ARROW_LIFETIME;
    private static final ForgeConfigSpec.IntValue MAX_RECHARGE_LEVEL;
    private static final ForgeConfigSpec.IntValue MAX_SPEED_LEVEL;

    // ── 조준·타겟팅 ──
    private static final ForgeConfigSpec.BooleanValue LEAD_TARGETING;
    private static final ForgeConfigSpec.IntValue RETARGET_INTERVAL;
    private static final ForgeConfigSpec.DoubleValue W_DISTANCE;
    private static final ForgeConfigSpec.DoubleValue W_THREAT;
    private static final ForgeConfigSpec.DoubleValue W_LOW_HEALTH;
    private static final ForgeConfigSpec.DoubleValue W_FINISH;
    private static final ForgeConfigSpec.DoubleValue W_FOCUS_PENALTY;

    // ── 상점 주민: 스폰알 가격(입력 아이템 개수) ──
    private static final Map<String, ForgeConfigSpec.IntValue> EGG_COSTS = new LinkedHashMap<>();

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("성능 설정").push("performance");
        AREA_SCAN_INTERVAL = b.comment("주변 몹 범위 검색 주기(틱). 탱커 도발, 번개 마법사 궁극기 대기, 치유 토템 대상 탐색, 분노 타겟팅에 사용. 20틱 = 1초")
                .defineInRange("areaScanIntervalTicks", 10, 1, 100);
        b.pop();

        b.comment("주민 터렛(활) 밸런스").push("villager_turret");
        ARROW_SPEED = b.comment("기본 화살 속도(블록/틱)").defineInRange("arrowSpeed", 0.8D, 0.1D, 5.0D);
        ARROW_SPEED_PER_LEVEL = b.comment("탄속 업그레이드 1레벨당 증가 속도").defineInRange("arrowSpeedPerLevel", 0.15D, 0.0D, 1.0D);
        ARROW_DAMAGE = b.comment("기본 화살 피해").defineInRange("arrowDamage", 2.0D, 0.0D, 100.0D);
        ARROW_DAMAGE_PER_LEVEL = b.comment("공격력 업그레이드 1레벨당 증가 피해").defineInRange("arrowDamagePerLevel", 1.0D, 0.0D, 100.0D);
        BASE_COOLDOWN = b.comment("기본 사격 쿨타임(틱)").defineInRange("baseCooldownTicks", 60, 1, 1200);
        COOLDOWN_REDUCTION_PER_LEVEL = b.comment("재장전 업그레이드 1레벨당 쿨타임 감소(틱)").defineInRange("cooldownReductionPerLevel", 5, 0, 100);
        MIN_COOLDOWN = b.comment("최소 쿨타임(틱)").defineInRange("minCooldownTicks", 10, 1, 1200);
        ATTACK_RANGE = b.comment("사격 사거리(블록)").defineInRange("attackRange", 20.0D, 4.0D, 64.0D);
        ARROW_LIFETIME = b.comment("터렛 화살이 자동으로 사라지기까지의 시간(틱)").defineInRange("arrowLifetimeTicks", 200, 20, 6000);
        MAX_RECHARGE_LEVEL = b.comment("재장전 업그레이드 최대 레벨").defineInRange("maxRechargeLevel", 10, 0, 1000);
        MAX_SPEED_LEVEL = b.comment("탄속 업그레이드 최대 레벨 (너무 높으면 화살이 몹을 뚫고 지나가는 물리 버그 발생)").defineInRange("maxSpeedLevel", 15, 0, 1000);
        b.pop();

        b.comment("조준·타겟 우선순위 (주민 터렛, 삼지창 터렛)").push("targeting");
        LEAD_TARGETING = b.comment("true면 적의 이동을 예측해 미래 위치로 쏘고(리드샷), 중력 포물선을 정확히 계산합니다.")
                .define("leadTargeting", true);
        RETARGET_INTERVAL = b.comment("교전 중 더 좋은 표적이 있는지 다시 평가하는 주기(틱)").defineInRange("retargetIntervalTicks", 20, 5, 200);
        W_DISTANCE = b.comment("거리 가중치: 1블록 멀어질 때마다 감점").defineInRange("distanceWeight", 1.0D, 0.0D, 100.0D);
        W_THREAT = b.comment("위협도 가중치 (힐러·지휘관·보스 등 우선 처치)").defineInRange("threatWeight", 1.0D, 0.0D, 100.0D);
        W_LOW_HEALTH = b.comment("체력 비율이 낮을수록 가산 (0~이 값)").defineInRange("lowHealthWeight", 10.0D, 0.0D, 100.0D);
        W_FINISH = b.comment("한 방에 죽일 수 있는 적 가산점(막타 우선)").defineInRange("finishBonus", 12.0D, 0.0D, 100.0D);
        W_FOCUS_PENALTY = b.comment("다른 터렛이 이미 노리는 적 1마리당 감점(분산 사격)").defineInRange("focusPenalty", 6.0D, 0.0D, 100.0D);
        b.pop();

        b.comment("상점 주민 스폰알 가격 (기존 입력 아이템의 개수만 바뀝니다)").push("store");
        defineEgg(b, "VILLAGER_TURRET_SPAWN_EGG", 3);
        defineEgg(b, "RESOURCE_VILLAGER_SPAWN_EGG", 3);
        defineEgg(b, "RESOURCE_VILLAGER_EASY_SPAWN_EGG", 3);
        defineEgg(b, "HEALER_TURRET_SPAWN_EGG", 1);
        defineEgg(b, "TRIDENT_TURRET_SPAWN_EGG", 4);
        defineEgg(b, "TANKER_TURRET_SPAWN_EGG", 1);
        defineEgg(b, "LIGHTNING_WIZARD_SPAWN_EGG", 1);
        defineEgg(b, "PLASMA_WIZARD_SPAWN_EGG", 4);
        defineEgg(b, "BEE_TURRET_SPAWN_EGG", 1);
        b.pop();

        SPEC = b.build();
    }

    private static void defineEgg(ForgeConfigSpec.Builder b, String eggId, int def) {
        EGG_COSTS.put(eggId, b.defineInRange(eggId.toLowerCase() + "_cost", def, 1, 64));
    }

    private TurretConfig() {}

    // 설정 로드 전에 호출되어도 기본값으로 안전하게 동작하도록 감싸는 헬퍼
    private static <T> T safe(Supplier<T> getter, T fallback) {
        try {
            return getter.get();
        } catch (IllegalStateException | NullPointerException e) {
            return fallback;
        }
    }

    public static int scanInterval() { return Math.max(1, safe(AREA_SCAN_INTERVAL::get, 10)); }

    public static double arrowSpeed() { return safe(ARROW_SPEED::get, 0.8D); }
    public static double arrowSpeedPerLevel() { return safe(ARROW_SPEED_PER_LEVEL::get, 0.15D); }
    public static double arrowDamage() { return safe(ARROW_DAMAGE::get, 2.0D); }
    public static double arrowDamagePerLevel() { return safe(ARROW_DAMAGE_PER_LEVEL::get, 1.0D); }
    public static int baseCooldown() { return safe(BASE_COOLDOWN::get, 60); }
    public static int cooldownReductionPerLevel() { return safe(COOLDOWN_REDUCTION_PER_LEVEL::get, 5); }
    public static int minCooldown() { return safe(MIN_COOLDOWN::get, 10); }
    public static double attackRange() { return safe(ATTACK_RANGE::get, 20.0D); }
    public static int arrowLifetime() { return safe(ARROW_LIFETIME::get, 200); }
    public static int maxRechargeLevel() { return safe(MAX_RECHARGE_LEVEL::get, 10); }
    public static int maxSpeedLevel() { return safe(MAX_SPEED_LEVEL::get, 15); }

    public static boolean leadTargeting() { return safe(LEAD_TARGETING::get, true); }
    public static int retargetInterval() { return Math.max(1, safe(RETARGET_INTERVAL::get, 20)); }
    public static double distanceWeight() { return safe(W_DISTANCE::get, 1.0D); }
    public static double threatWeight() { return safe(W_THREAT::get, 1.0D); }
    public static double lowHealthWeight() { return safe(W_LOW_HEALTH::get, 10.0D); }
    public static double finishBonus() { return safe(W_FINISH::get, 12.0D); }
    public static double focusPenalty() { return safe(W_FOCUS_PENALTY::get, 6.0D); }

    /** 상점 주민 스폰알 가격. eggId는 ModItems 필드 이름 (예: "VILLAGER_TURRET_SPAWN_EGG") */
    public static int eggCost(String eggId, int fallback) {
        ForgeConfigSpec.IntValue v = EGG_COSTS.get(eggId);
        return v == null ? fallback : safe(v::get, fallback);
    }
}

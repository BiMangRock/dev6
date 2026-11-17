package changmin.changmin_villager_turret.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 🎯 탄도 조준 계산기 (리드샷 + 포물선 해석)
 *
 * 마인크래프트 화살/삼지창(AbstractArrow)의 실제 물리 규칙을 그대로 수식화합니다.
 *   매 틱:  위치 += 속도  →  속도 *= DRAG(0.99)  →  (중력 켜짐) 속도.y -= GRAVITY(0.05)
 *
 * n틱 후 수평 이동거리:  X(n) = vx * S(n),     S(n) = (1 - d^n) / (1 - d)
 * n틱 후 높이:          Y(n) = vy * S(n) - g/(1-d) * (n - S(n))
 *
 * 1) 목표 수평거리로부터 n(도착 시간)을 역산하고,
 * 2) 그 n에서 높이가 목표 높이와 같아지는 발사각을 이분 탐색으로 찾습니다.
 * 3) 적의 이동 속도 × n 만큼 앞을 노리도록(리드샷) 2~3회 반복해 수렴시킵니다.
 *
 * 반환값은 Projectile.shoot(x, y, z, speed, inaccuracy)에 그대로 넣을 방향 벡터입니다.
 */
public final class BallisticAim {

    public static final double ARROW_GRAVITY = 0.05D;
    public static final double ARROW_DRAG = 0.99D;

    private static final double MIN_PITCH = Math.toRadians(-80.0D);
    private static final double MAX_PITCH = Math.toRadians(45.0D); // 저탄도(직사)만 사용. 45도 넘는 고탄도는 느려서 제외
    private static final double FALLBACK_PITCH = Math.toRadians(35.0D); // 사거리 밖일 때 최대 사거리에 가까운 각도
    private static final int LEAD_ITERATIONS = 3;
    private static final int BISECTION_STEPS = 32;

    private BallisticAim() {}

    /**
     * 표적의 최근 1틱 이동량(블록/틱). 서버의 deltaMovement는 마찰 적용 후 값이라 실제 이동보다 작게 나오므로
     * 이전 틱 위치(xo/yo/zo)와의 차이를 사용합니다. 땅 위 몹의 점프 흔들림은 무시하고, 비행 몹만 Y 이동을 반영합니다.
     */
    public static Vec3 observedVelocity(Entity target) {
        double vx = target.getX() - target.xo;
        double vz = target.getZ() - target.zo;
        double vy = target.isNoGravity() ? target.getY() - target.yo : 0.0D;
        // 순간이동·넉백 등 비정상 값은 예측에서 제외
        if (vx * vx + vy * vy + vz * vz > 4.0D) return Vec3.ZERO;
        return new Vec3(vx, vy, vz);
    }

    /**
     * @param from          투사체 생성 위치 (예: arrow.position())
     * @param target        표적
     * @param heightFrac    표적의 어느 높이를 노릴지 (0=발, 0.5=몸통, 1=머리)
     * @param speed         발사 속도 (shoot()에 넘기는 velocity 값)
     * @param gravity       0이면 무중력 직선 탄도
     * @param lead          true면 이동 예측(리드샷) 적용
     * @return shoot()에 넣을 방향 벡터 (길이는 상관없음)
     */
    public static Vec3 solve(Vec3 from, LivingEntity target, double heightFrac, double speed,
                             double gravity, double drag, boolean lead) {
        Vec3 aimPoint = new Vec3(target.getX(), target.getY(heightFrac), target.getZ());
        Vec3 velocity = lead ? observedVelocity(target) : Vec3.ZERO;

        double flightTicks = 0.0D;
        Vec3 direction = aimPoint.subtract(from);

        int iterations = lead ? LEAD_ITERATIONS : 1;
        for (int i = 0; i < iterations; i++) {
            Vec3 predicted = aimPoint.add(velocity.scale(flightTicks));
            double dx = predicted.x - from.x;
            double dy = predicted.y - from.y;
            double dz = predicted.z - from.z;
            double horizontal = Math.sqrt(dx * dx + dz * dz);

            if (gravity <= 0.0D) {
                // 무중력: 직선으로 예측 지점을 그대로 겨눔 (공기저항으로 인한 감속만 시간 계산에 반영)
                direction = new Vec3(dx, dy, dz);
                double t = ticksToTravel(direction.length(), speed, drag);
                flightTicks = Double.isInfinite(t) ? 0.0D : t;
                continue;
            }

            double pitch = solvePitch(horizontal, dy, speed, gravity, drag);
            double cos = Math.cos(pitch);
            double sin = Math.sin(pitch);

            double hx = horizontal < 1.0E-6D ? 0.0D : dx / horizontal;
            double hz = horizontal < 1.0E-6D ? 0.0D : dz / horizontal;
            direction = new Vec3(hx * cos, sin, hz * cos);

            double t = ticksToTravel(horizontal, speed * cos, drag);
            flightTicks = Double.isInfinite(t) ? 0.0D : t;
        }
        return direction;
    }

    /** 수평거리 horizontal, 높이차 dy를 맞히는 저탄도 발사각(라디안) */
    static double solvePitch(double horizontal, double dy, double speed, double gravity, double drag) {
        if (horizontal < 1.0E-3D) {
            return dy >= 0.0D ? MAX_PITCH : MIN_PITCH;
        }
        // 최대 속도로 수평 발사해도 닿지 않는 거리면 최대 사거리 근처 각도로 쏩니다.
        if (Double.isInfinite(ticksToTravel(horizontal, speed, drag))) {
            return FALLBACK_PITCH;
        }

        double lo = MIN_PITCH;
        double hi = MAX_PITCH;
        // 사거리 안이지만 목표가 너무 높아 45도로도 못 닿으면 45도
        if (heightError(hi, horizontal, dy, speed, gravity, drag) < 0.0D) {
            return hi;
        }
        for (int i = 0; i < BISECTION_STEPS; i++) {
            double mid = (lo + hi) * 0.5D;
            if (heightError(mid, horizontal, dy, speed, gravity, drag) < 0.0D) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        return (lo + hi) * 0.5D;
    }

    /** 발사각 pitch로 쐈을 때 수평거리 지점에서의 (도달 높이 - 목표 높이). 도달 불가면 +무한대(=각도를 낮춰야 함) */
    private static double heightError(double pitch, double horizontal, double dy, double speed, double gravity, double drag) {
        double vx = speed * Math.cos(pitch);
        double vy = speed * Math.sin(pitch);
        double n = ticksToTravel(horizontal, vx, drag);
        if (Double.isInfinite(n)) return Double.POSITIVE_INFINITY;

        double s = horizontal / vx; // = S(n)
        double y = vy * s - gravity / (1.0D - drag) * (n - s);
        return y - dy;
    }

    /** 공기저항 drag가 있을 때 초기속도 v로 distance만큼 가는 데 걸리는 틱 수. 도달 불가면 +무한대 */
    static double ticksToTravel(double distance, double v, double drag) {
        if (distance <= 0.0D) return 0.0D;
        if (v <= 1.0E-6D) return Double.POSITIVE_INFINITY;
        if (drag >= 1.0D) return distance / v;
        double ratio = 1.0D - distance * (1.0D - drag) / v;
        if (ratio <= 1.0E-6D) return Double.POSITIVE_INFINITY;
        return Math.log(ratio) / Math.log(drag);
    }
}

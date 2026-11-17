package changmin.changmin_villager_turret.client;

import changmin.changmin_villager_turret.changmin_villager_turret;
import changmin.changmin_villager_turret.config.TurretConfig;
import changmin.changmin_villager_turret.feature.turret.bee_summoner_turret.BeeSummonerTurretEntity;
import changmin.changmin_villager_turret.feature.turret.common.AbstractTurretEntity;
import changmin.changmin_villager_turret.feature.turret.healer.HealerTurretEntity;
import changmin.changmin_villager_turret.feature.turret.trident_turret.TridentTurretEntity;
import changmin.changmin_villager_turret.feature.turret.villager_turret.VillagerTurretEntity;
import changmin.changmin_villager_turret.registry.ModEntityTypes;
import changmin.changmin_villager_turret.util.BallisticAim;
import org.joml.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

/**
 * 🎯 사거리 미리보기 (클라이언트 전용, 본인 화면에만 보임)
 *
 *  - 터렛 스폰알을 들고 있으면: 조준한 바닥(설치될 자리)에 기본 사거리 원
 *  - 웅크린 채 터렛을 바라보면(최대 32블록): 그 터렛의 현재 사거리 원 (업그레이드 반영)
 *
 * 주민 터렛은 "설정 사거리 / 표적 탐지 거리 / 화살이 실제로 닿는 거리" 중 가장 짧은 값을 그립니다.
 * 그래서 탄속이 낮으면 원이 작게 보입니다.
 */
@Mod.EventBusSubscriber(modid = changmin_villager_turret.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class RangePreview {

    private static final int DRAW_INTERVAL_TICKS = 4;
    private static final double LOOK_DISTANCE = 32.0D;
    /** 터렛 표적 탐지 거리(FOLLOW_RANGE 기본값). 주민 터렛은 이 거리 안의 적만 표적으로 잡습니다. */
    private static final double VILLAGER_TURRET_DETECT_RANGE = 16.0D;

    private RangePreview() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null || mc.isPaused()) return;
        if (player.tickCount % DRAW_INTERVAL_TICKS != 0) return;

        // 1. 터렛 스폰알을 들고 있으면 설치 예정 위치에 기본 사거리
        EntityType<?> eggType = heldTurretEggType(player);
        if (eggType != null) {
            if (mc.hitResult instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());
                double range = defaultRange(eggType);
                if (range > 0.0D) {
                    drawRing(level, new Vec3(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D), range, colorOf(eggType));
                }
            }
            return;
        }

        // 2. 웅크린 채 터렛을 바라보면 현재 사거리
        if (player.isShiftKeyDown()) {
            AbstractTurretEntity turret = lookedAtTurret(player);
            if (turret != null) {
                double range = currentRange(turret);
                if (range > 0.0D) {
                    drawRing(level, turret.position(), range, colorOf(turret.getType()));
                }
            }
        }
    }

    // ── 사거리 계산 ──

    /** 설치 직후(업그레이드 0) 기본 사거리 */
    private static double defaultRange(EntityType<?> type) {
        if (type == ModEntityTypes.VILLAGER_TURRET.get()) return villagerTurretRange(TurretConfig.arrowSpeed(), false);
        if (type == ModEntityTypes.TRIDENT_TURRET.get()) return 16.0D;
        if (type == ModEntityTypes.TANKER_TURRET.get()) return 10.0D;      // 도발 범위
        if (type == ModEntityTypes.HEALER_TURRET.get()) return 8.0D;       // 치유 범위
        if (type == ModEntityTypes.LIGHTNING_WIZARD.get()) return 14.0D;
        if (type == ModEntityTypes.PLASMA_WIZARD.get()) return 14.0D;
        if (type == ModEntityTypes.BEE_SUMMONER_TURRET.get()) return 16.0D;
        return 0.0D;
    }

    /** 업그레이드가 반영된 현재 사거리 */
    private static double currentRange(AbstractTurretEntity turret) {
        if (turret instanceof VillagerTurretEntity villager) {
            double speed = TurretConfig.arrowSpeed() + villager.getSpeedLevel() * TurretConfig.arrowSpeedPerLevel();
            return villagerTurretRange(speed, villager.getNoGravityEnabled() == 1);
        }
        if (turret instanceof TridentTurretEntity trident) return 16.0D + trident.getRangeLevel() * 4.0D;
        if (turret instanceof BeeSummonerTurretEntity bee) return 16.0D + bee.getRangeLevel() * 4.0D;
        if (turret instanceof HealerTurretEntity healer) return 8.0D + healer.getRangeLevel();
        return defaultRange(turret.getType());
    }

    private static double villagerTurretRange(double arrowSpeed, boolean noGravity) {
        double range = Math.min(TurretConfig.attackRange(), VILLAGER_TURRET_DETECT_RANGE);
        if (!noGravity) {
            range = Math.min(range, BallisticAim.maxFlatRange(arrowSpeed, BallisticAim.ARROW_GRAVITY, BallisticAim.ARROW_DRAG));
        }
        return range;
    }

    // ── 색상 (터렛마다 구분) ──
    private static Vector3f colorOf(EntityType<?> type) {
        if (type == ModEntityTypes.TRIDENT_TURRET.get()) return new Vector3f(0.2F, 0.8F, 1.0F);
        if (type == ModEntityTypes.TANKER_TURRET.get()) return new Vector3f(1.0F, 0.55F, 0.1F);
        if (type == ModEntityTypes.HEALER_TURRET.get()) return new Vector3f(1.0F, 0.45F, 0.75F);
        if (type == ModEntityTypes.LIGHTNING_WIZARD.get()) return new Vector3f(0.6F, 0.3F, 1.0F);
        if (type == ModEntityTypes.PLASMA_WIZARD.get()) return new Vector3f(0.2F, 0.4F, 1.0F);
        if (type == ModEntityTypes.BEE_SUMMONER_TURRET.get()) return new Vector3f(1.0F, 0.85F, 0.2F);
        return new Vector3f(0.3F, 1.0F, 0.3F); // 주민 터렛: 초록
    }

    // ── 보조 ──

    @Nullable
    private static EntityType<?> heldTurretEggType(LocalPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof SpawnEggItem egg) {
                EntityType<?> type = egg.getType(stack.getTag());
                if (defaultRange(type) > 0.0D) return type;
            }
        }
        return null;
    }

    @Nullable
    private static AbstractTurretEntity lookedAtTurret(LocalPlayer player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(LOOK_DISTANCE));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(LOOK_DISTANCE)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level(), player, eye, end, searchBox,
                e -> e instanceof AbstractTurretEntity && e.isAlive());
        return hit != null && hit.getEntity() instanceof AbstractTurretEntity turret ? turret : null;
    }

    /** 바닥 높이에 파티클로 원을 그립니다. 반지름에 비례해 점 개수를 정하되 너무 많아지지 않게 제한 */
    private static void drawRing(ClientLevel level, Vec3 center, double radius, Vector3f color) {
        int points = Mth.clamp((int) (2.0D * Math.PI * radius * 1.2D), 24, 180);
        DustParticleOptions dust = new DustParticleOptions(color, 1.0F);
        double y = center.y + 0.15D;
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D) * i / points;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.addParticle(dust, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}

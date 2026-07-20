package changmin.changmin_villager_turret.feature.turret.trident_turret;

import changmin.changmin_villager_turret.changmin_villager_turret;
import changmin.changmin_villager_turret.ally.IAlly;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = changmin_villager_turret.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TridentTurretShooter {

    // 🔒 AbstractArrow의 private piercingIgnoreEntityIds(관통 충돌 제외 셋)를 제어하기 위한 리플렉션 객체
    private static java.lang.reflect.Field piercingIgnoreField = null;

    static {
        try {
            // 개발 환경(Mojang 매핑) 이름으로 바인딩 시도
            piercingIgnoreField = AbstractArrow.class.getDeclaredField("piercingIgnoreEntityIds");
            piercingIgnoreField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // 실 서버 및 난독화 환경을 위한 다중 백업 감지 알고리즘 (타입 기반 검출 포함)
            for (java.lang.reflect.Field field : AbstractArrow.class.getDeclaredFields()) {
                if (field.getType().getName().contains("IntOpenHashSet") ||
                        field.getName().equals("f_36710_") ||
                        field.getName().equals("piercingIgnoreEntityIds")) {
                    piercingIgnoreField = field;
                    piercingIgnoreField.setAccessible(true);
                    break;
                }
            }
        }
    }

    // 삼지창의 충돌 제외 무시 목록 가져오기
    public static IntOpenHashSet getIgnoredEntities(AbstractArrow arrow) {
        if (piercingIgnoreField != null) {
            try {
                return (IntOpenHashSet) piercingIgnoreField.get(arrow);
            } catch (IllegalAccessException e) {
                // 무시
            }
        }
        return null;
    }

    // 충돌 제외 목록에 대상 적 추가하기 (연사 중복 충돌 무한루프 방지)
    public static void addIgnoredEntity(AbstractArrow arrow, net.minecraft.world.entity.Entity entity) {
        if (piercingIgnoreField != null) {
            try {
                IntOpenHashSet set = (IntOpenHashSet) piercingIgnoreField.get(arrow);
                if (set == null) {
                    set = new IntOpenHashSet(5);
                    piercingIgnoreField.set(arrow, set);
                }
                set.add(entity.getId());
            } catch (IllegalAccessException e) {
                // 무시
            }
        }
    }

    // 🏹 [핵심] 삼지창 충돌 분석 및 이벤트 필터링 연산
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof ThrownTrident trident && !trident.level.isClientSide) {
            if (trident.getTags().contains("turret_trident")) {
                HitResult hitResult = event.getRayTraceResult();

                // 블록 충돌이 아닌 엔티티 충돌 상태일 때만 개입
                if (hitResult instanceof EntityHitResult entityHitResult) {
                    net.minecraft.world.entity.Entity target = entityHitResult.getEntity();

                    // 1. 아군 사격 및 자신 사격 무시 판정
                    net.minecraft.world.entity.Entity owner = trident.getOwner();
                    if (target == owner || (owner instanceof IAlly && target instanceof IAlly)) {
                        event.setCanceled(true);
                        addIgnoredEntity(trident, target); // 무한루프 락 방지용 대상 목록 수동 등록
                        return;
                    }

                    // 엔더맨 회피 통과 연산
                    if (target.getType() == EntityType.ENDERMAN) {
                        event.setCanceled(true);
                        addIgnoredEntity(trident, target);
                        return;
                    }

                    // 2. 이미 해당 틱이나 이전 연산에서 맞았던 적은 연산 생략 (충돌 판정 무한루프 락 크래시 방지 필수)
                    IntOpenHashSet ignoredSet = getIgnoredEntities(trident);
                    if (ignoredSet != null && ignoredSet.contains(target.getId())) {
                        event.setCanceled(true);
                        return;
                    }

                    int maxPierce = trident.getPierceLevel(); // 관통 가능 횟수 업그레이드 수치
                    int currentHitCount = ignoredSet == null ? 0 : ignoredSet.size();

                    // 3. 관통 허용 한계에 도달하지 않은 경우: 충돌 이벤트를 취소해 전진 물리 속도를 온전히 유지하며 관통 진행
                    if (currentHitCount < maxPierce) {
                        // 수동 물리 데미지 정밀 연산 및 피해 전달
                        float baseDamage = (float) trident.getBaseDamage();
                        DamageSource damageSource = DamageSource.trident(trident, owner == null ? trident : owner);

                        if (target.hurt(damageSource, baseDamage)) {
                            if (target instanceof LivingEntity livingTarget) {
                                if (owner instanceof LivingEntity livingOwner) {
                                    net.minecraft.world.item.enchantment.EnchantmentHelper.doPostHurtEffects(livingTarget, livingOwner);
                                    net.minecraft.world.item.enchantment.EnchantmentHelper.doPostDamageEffects(livingOwner, livingTarget);
                                }
                            }

                            // 삼지창 전설 효과: 집뢰(벼락) 무장 개조 시 번개 소환
                            if (trident.getTags().contains("lightning_trident") && target instanceof LivingEntity) {
                                Level level = target.level;
                                if (level instanceof ServerLevel serverLevel) {
                                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                                    if (bolt != null) {
                                        bolt.moveTo(target.position());
                                        bolt.addTag("changmin_villager_turret_friendly_lightning");
                                        serverLevel.addFreshEntity(bolt);
                                    }
                                }
                            }

                            trident.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
                        }

                        // 이 관통 타깃을 무시 목록에 안전하게 추가한 후, 충돌 이벤트를 무효화하여 관통 비행 계속 진행
                        addIgnoredEntity(trident, target);
                        event.setCanceled(true);
                    }
                    // 4. 마지막 타격(관통 한도 도달): 취소하지 않고 삼지창 고유의 바닐라 충돌 코드로 이양하여 꽂히거나 낙하하도록 처리
                    else {
                        // event.setCanceled를 수행하지 않으므로 바닐라 로직이 알아서 데미지를 넣고 삼지창 전진 벡터를 지워버려 떨어뜨림.
                    }
                }
            }
        }
    }

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
                        bolt.addTag("changmin_villager_turret_friendly_lightning");
                        serverLevel.addFreshEntity(bolt);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
        LightningBolt bolt = event.getLightning();
        if (bolt != null && bolt.getTags().contains("changmin_villager_turret_friendly_lightning")) {
            net.minecraft.world.entity.Entity victim = event.getEntity();

            if (victim instanceof net.minecraft.world.entity.player.Player || victim instanceof changmin.changmin_villager_turret.ally.IAlly) {
                event.setCanceled(true);
            }
        }
    }
}
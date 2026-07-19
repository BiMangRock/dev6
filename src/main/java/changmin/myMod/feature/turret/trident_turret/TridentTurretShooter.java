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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = MyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TridentTurretShooter {

    // 🔒 메모리 누수(Memory Leak) 방지를 위한 가비지 컬렉션 연동 WeakHashMap 기반 동기화 Set
    private static final Set<ThrownTrident> activeTridents = Collections.synchronizedSet(
            Collections.newSetFromMap(new WeakHashMap<>())
    );

    // 🔒 ThrownTrident의 private boolean dealtDamage 필드에 접근하기 위한 리플렉션 객체
    private static java.lang.reflect.Field dealtDamageField = null;

    static {
        try {
            // 개발 환경(Mojang 매핑) 이름으로 시도
            dealtDamageField = ThrownTrident.class.getDeclaredField("dealtDamage");
            dealtDamageField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // 실 서버 실행(Obfuscated/SRG) 환경의 경우, ThrownTrident 안의 유일한 boolean 원시 타입 필드를 검색하여 바인딩
            for (java.lang.reflect.Field field : ThrownTrident.class.getDeclaredFields()) {
                if (field.getType() == boolean.class) {
                    dealtDamageField = field;
                    dealtDamageField.setAccessible(true);
                    break;
                }
            }
        }
    }

    private static void setDealtDamage(ThrownTrident trident, boolean val) {
        if (dealtDamageField != null) {
            try {
                dealtDamageField.setBoolean(trident, val);
            } catch (IllegalAccessException e) {
                // 예외 무시
            }
        }
    }

    // 🔄 매 틱마다 활성화된 삼지창의 dealtDamage 값을 false로 리셋하여, AbstractArrow 관통 물리 엔진이 작동하도록 강제 유도
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isServer()) {
            // 이미 제거되었거나 사라진 삼지창 엔티티는 정리
            activeTridents.removeIf(trident -> !trident.isAlive() || trident.isRemoved());

            for (ThrownTrident trident : activeTridents) {
                if (trident.level == event.world) {
                    if (trident.getPierceLevel() > 0) {
                        setDealtDamage(trident, false); // 관통 중이라면 강제 리셋
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

        // 월드에 등록함과 동시에 월드 틱 추적 리스트에 삽입
        level.addFreshEntity(trident);
        activeTridents.add(trident);
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
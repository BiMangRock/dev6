package changmin.changmin_villager_turret.gametest;

import changmin.changmin_villager_turret.changmin_villager_turret;
import changmin.changmin_villager_turret.feature.turret.common.TurretInteractionHandler;
import changmin.changmin_villager_turret.feature.turret.villager_turret.TurretTradeManager;
import changmin.changmin_villager_turret.feature.turret.villager_turret.VillagerTurretEntity;
import changmin.changmin_villager_turret.registry.ModEntityTypes;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.RegistryObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 자동 테스트 (Forge GameTest)
 *
 * 실행: gradlew runGameTestServer  (모두 통과하면 서버가 정상 종료, 실패가 있으면 오류 코드로 종료)
 * 게임 안에서: runClient → 월드 접속 → /test runall
 *
 * 모든 테스트는 13×6×13 돌 바닥 구조물(structures/empty.nbt) 위에서 진행됩니다.
 * 좌표는 GameTestHelper 기준 상대 좌표입니다. 구조물은 테스트 기준점보다 한 칸 위에 놓이므로
 * 돌 바닥은 y=1, 엔티티가 서는 칸은 y=2 입니다. (y=1에 소환하면 돌 속에 끼어서 아래로 떨어짐)
 */
@GameTestHolder(changmin_villager_turret.MODID)
@PrefixGameTestTemplate(false)
public class TurretGameTests {

    private static final String TEMPLATE = "empty";
    private static final String TEMPLATE_ID = changmin_villager_turret.MODID + ":" + TEMPLATE;
    private static final String BATCH = "defaultBatch";

    /** 엔티티가 서는 높이 (돌 바닥 바로 위) */
    private static final int FLOOR_TOP = 2;

    private static final BlockPos CENTER = new BlockPos(6, FLOOR_TOP, 6);

    // ─────────────────────────────── 대상 목록 ───────────────────────────────

    /** 설치형 엔티티 전부 (움직이면 안 되는 것들) */
    private static Map<String, Supplier<? extends EntityType<?>>> stationaryTypes() {
        Map<String, Supplier<? extends EntityType<?>>> map = new LinkedHashMap<>();
        map.put("villager_turret", ModEntityTypes.VILLAGER_TURRET);
        map.put("trident_turret", ModEntityTypes.TRIDENT_TURRET);
        map.put("tanker_turret", ModEntityTypes.TANKER_TURRET);
        map.put("healer_turret", ModEntityTypes.HEALER_TURRET);
        map.put("lightning_wizard", ModEntityTypes.LIGHTNING_WIZARD);
        map.put("plasma_wizard", ModEntityTypes.PLASMA_WIZARD);
        map.put("bee_summoner_turret", ModEntityTypes.BEE_SUMMONER_TURRET);
        map.put("resource_villager", ModEntityTypes.RESOURCE_VILLAGER);
        map.put("resource_villager_easy", ModEntityTypes.RESOURCE_VILLAGER_EASY);
        return map;
    }

    /** 좀비를 직접 공격해야 하는 터렛 */
    private static Map<String, Supplier<? extends EntityType<?>>> attackerTypes() {
        Map<String, Supplier<? extends EntityType<?>>> map = new LinkedHashMap<>();
        map.put("villager_turret", ModEntityTypes.VILLAGER_TURRET);
        map.put("trident_turret", ModEntityTypes.TRIDENT_TURRET);
        map.put("lightning_wizard", ModEntityTypes.LIGHTNING_WIZARD);
        map.put("plasma_wizard", ModEntityTypes.PLASMA_WIZARD);
        map.put("bee_summoner_turret", ModEntityTypes.BEE_SUMMONER_TURRET);
        return map;
    }

    // ─────────────────────────────── 공용 도우미 ───────────────────────────────

    /** 플레이어가 스폰알로 설치한 것과 같은 경로(finalizeSpawn 포함)로 소환 */
    private static Entity place(GameTestHelper helper, EntityType<?> type, BlockPos relative) {
        Entity entity = type.spawn(helper.getLevel(), helper.absolutePos(relative), MobSpawnType.SPAWN_EGG);
        if (entity == null) {
            helper.fail("소환 실패: " + EntityType.getKey(type));
        }
        return entity;
    }

    /** 움직이지 않고, 햇빛에 타지 않는 표적 좀비 (투구 착용) */
    private static Zombie dummyZombie(GameTestHelper helper, BlockPos relative) {
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, relative);
        zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
        zombie.setHealth(zombie.getMaxHealth());
        return zombie;
    }

    private static TestFunction test(String name, int timeoutTicks, Consumer<GameTestHelper> body) {
        // (배치 이름, 테스트 이름, 구조물, 회전, 제한 시간, 준비 시간, 필수 여부, 내용)
        return new TestFunction(BATCH, name, TEMPLATE_ID, Rotation.NONE, timeoutTicks, 0L, true, body);
    }

    // ─────────────────────────────── 1. 고정 ───────────────────────────────

    /**
     * 터렛이 제자리에 있는지: 옆에 좀비를 두고, 한 번 맞게 한 뒤 200틱 동안 위치를 확인합니다.
     * (밀림·넉백·순간이동 모두 잡힘)
     */
    @GameTestGenerator
    public static Collection<TestFunction> stayInPlace() {
        List<TestFunction> tests = new ArrayList<>();
        stationaryTypes().forEach((name, type) -> tests.add(test("stays_in_place_" + name, 260, helper -> {
            Entity turret = place(helper, type.get(), CENTER);
            Vec3 start = turret.position();
            Zombie zombie = dummyZombie(helper, CENTER.offset(2, 0, 0));

            // 좀비가 터렛을 한 대 때린 것처럼 (넉백이 발생하는 경로)
            helper.runAfterDelay(20, () -> {
                if (turret.isAlive() && zombie.isAlive()) {
                    turret.hurt(helper.getLevel().damageSources().mobAttack(zombie), 1.0F);
                }
            });

            // 매 틱마다 확인 → 한 번이라도 0.5칸 넘게 움직이면 즉시 실패
            helper.onEachTick(() -> {
                if (!turret.isAlive()) return;
                double moved = turret.position().distanceTo(start);
                if (moved > 0.5D) {
                    helper.fail(String.format("%s 이(가) %.2f칸 이동함 (시작 %s → 현재 %s)",
                            name, moved, fmt(start), fmt(turret.position())));
                }
            });

            helper.runAfterDelay(220, helper::succeed);
        })));
        return tests;
    }

    // ─────────────────────────────── 2. 공격 ───────────────────────────────

    /** 터렛 앞 5칸에 움직이지 않는 좀비를 두고, 체력이 줄어들면 통과 */
    @GameTestGenerator
    public static Collection<TestFunction> attacksZombie() {
        List<TestFunction> tests = new ArrayList<>();
        attackerTypes().forEach((name, type) -> tests.add(test("attacks_zombie_" + name, 400, helper -> {
            place(helper, type.get(), new BlockPos(3, FLOOR_TOP, 6));
            Zombie zombie = dummyZombie(helper, new BlockPos(8, FLOOR_TOP, 6));
            float startHealth = zombie.getHealth();
            helper.succeedWhen(() -> helper.assertTrue(!zombie.isAlive() || zombie.getHealth() < startHealth,
                    name + " 이(가) 좀비를 공격하지 않음"));
        })));
        return tests;
    }

    // ─────────────────────────────── 3. 치유 ───────────────────────────────

    /** 치유 터렛이 체력이 깎인 아군(주민 터렛)을 회복시키는지 */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void healer_heals_ally(GameTestHelper helper) {
        place(helper, ModEntityTypes.HEALER_TURRET.get(), new BlockPos(4, FLOOR_TOP, 6));
        LivingEntity ally = (LivingEntity) place(helper, ModEntityTypes.VILLAGER_TURRET.get(), new BlockPos(8, FLOOR_TOP, 6));

        // finalizeSpawn 이후 한 틱 뒤에 체력을 깎음
        float[] damaged = new float[1];
        helper.runAfterDelay(1, () -> {
            damaged[0] = ally.getMaxHealth() * 0.5F;
            ally.setHealth(damaged[0]);
        });

        helper.succeedWhen(() -> helper.assertTrue(damaged[0] > 0 && ally.getHealth() > damaged[0],
                "치유 터렛이 아군을 회복시키지 않음 (현재 " + ally.getHealth() + ")"));
    }

    // ─────────────────────────────── 4. 인증서 재사용 ───────────────────────────────

    /** 업그레이드 인증서를 들고 터렛을 우클릭해도 업그레이드가 다시 적용되지 않아야 함 */
    @GameTest(template = TEMPLATE)
    public static void receipt_cannot_be_reused(GameTestHelper helper) {
        VillagerTurretEntity turret = (VillagerTurretEntity) place(helper, ModEntityTypes.VILLAGER_TURRET.get(), CENTER);
        int speedBefore = turret.getSpeedLevel();

        ItemStack receipt = TurretTradeManager.createUpgradeReceipt(turret, "speed",
                Component.literal("test"), Component.literal("a"), Component.literal("b"), Component.literal("c"));

        Player player = helper.makeMockPlayer();
        player.moveTo(helper.absoluteVec(Vec3.atBottomCenterOf(CENTER.offset(1, 0, 0))));
        player.setItemInHand(InteractionHand.MAIN_HAND, receipt);

        // 여러 번 눌러봄
        for (int i = 0; i < 3; i++) {
            player.interactOn(turret, InteractionHand.MAIN_HAND);
        }

        helper.assertTrue(turret.getSpeedLevel() == speedBefore,
                "인증서로 속도 업그레이드가 다시 적용됨: " + speedBefore + " → " + turret.getSpeedLevel());
        helper.succeed();
    }

    // ─────────────────────────────── 5. 회수 ───────────────────────────────

    /** 웅크린 채 빈손 우클릭 → 스폰알 → 다시 설치했을 때 레벨·경험치·체력·강화가 그대로인지 */
    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void pickup_keeps_level_and_health(GameTestHelper helper) {
        VillagerTurretEntity turret = (VillagerTurretEntity) place(helper, ModEntityTypes.VILLAGER_TURRET.get(), CENTER);

        helper.runAfterDelay(1, () -> {
            turret.setTurretLevel(4);
            turret.setNeededXp(1000);
            turret.addXp(7);
            turret.setDamageLevel(3);
            turret.setSpeedLevel(2);
            turret.setHealth(turret.getMaxHealth() - 5.0F);

            int level = turret.getTurretLevel();
            int xp = turret.getXp();
            int damageLevel = turret.getDamageLevel();
            int speedLevel = turret.getSpeedLevel();
            float health = turret.getHealth();

            // 실제 플레이어 동작 그대로: 웅크리기 + 빈손 + 우클릭
            Player player = helper.makeMockPlayer();
            player.moveTo(helper.absoluteVec(Vec3.atBottomCenterOf(CENTER.offset(1, 0, 0))));
            player.setShiftKeyDown(true);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.interactOn(turret, InteractionHand.MAIN_HAND);

            helper.assertTrue(turret.isRemoved(), "회수 후에도 터렛이 남아 있음");

            ItemStack egg = ItemStack.EMPTY;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack s = player.getInventory().getItem(i);
                if (s.getItem() instanceof SpawnEggItem) { egg = s; break; }
            }
            helper.assertTrue(!egg.isEmpty(), "회수했는데 스폰알이 인벤토리에 없음");

            Entity respawned = ModEntityTypes.VILLAGER_TURRET.get().spawn(helper.getLevel(), egg, null,
                    helper.absolutePos(CENTER.offset(-2, 0, 0)), MobSpawnType.SPAWN_EGG, true, false);
            helper.assertTrue(respawned instanceof VillagerTurretEntity, "스폰알로 다시 설치되지 않음");
            VillagerTurretEntity again = (VillagerTurretEntity) respawned;

            List<String> errors = new ArrayList<>();
            if (again.getTurretLevel() != level) errors.add("레벨 " + level + " → " + again.getTurretLevel());
            if (again.getXp() != xp) errors.add("경험치 " + xp + " → " + again.getXp());
            if (again.getDamageLevel() != damageLevel) errors.add("공격력 강화 " + damageLevel + " → " + again.getDamageLevel());
            if (again.getSpeedLevel() != speedLevel) errors.add("속도 강화 " + speedLevel + " → " + again.getSpeedLevel());
            if (Math.abs(again.getHealth() - health) > 0.01F) errors.add("체력 " + health + " → " + again.getHealth());
            helper.assertTrue(errors.isEmpty(), "회수 후 값이 바뀜: " + String.join(", ", errors));
            helper.succeed();
        });
    }

    // ─────────────────────────────── 6. 소환 ───────────────────────────────

    /** 모드의 모든 생명체가 오류 없이 소환되고 2초 동안 동작하는지 (엔티티마다 테스트 1개) */
    @GameTestGenerator
    public static Collection<TestFunction> spawnAll() {
        List<TestFunction> tests = new ArrayList<>();
        for (RegistryObject<EntityType<?>> entry : ModEntityTypes.ENTITY_TYPES.getEntries()) {
            String name = entry.getId().getPath();
            tests.add(test("spawns_" + name, 100, helper -> {
                EntityType<?> type = entry.get();
                Entity probe = type.create(helper.getLevel());
                if (!(probe instanceof Mob)) {
                    // 투사체 등은 단독 소환 대상이 아님
                    if (probe != null) probe.discard();
                    helper.succeed();
                    return;
                }
                probe.discard();

                Entity entity = place(helper, type, CENTER);
                helper.runAfterDelay(40, () -> {
                    helper.assertTrue(entity.tickCount > 0 || entity.isRemoved(),
                            name + " 이(가) 틱을 돌지 않음");
                    helper.succeed();
                });
            }));
        }
        return tests;
    }

    // ─────────────────────────────── 7. 언어 파일 ───────────────────────────────

    private static final Pattern PLACEHOLDER = Pattern.compile("%(?:\\d+\\$)?[sd]");

    /** ko_kr / en_us 키가 서로 같고, 각 문장의 %s 개수도 같은지 */
    @GameTest(template = TEMPLATE)
    public static void lang_files_match(GameTestHelper helper) {
        JsonObject ko = readLang("ko_kr");
        JsonObject en = readLang("en_us");
        helper.assertTrue(ko != null, "ko_kr.json 을 읽을 수 없음");
        helper.assertTrue(en != null, "en_us.json 을 읽을 수 없음");

        Set<String> onlyKo = new TreeSet<>(ko.keySet());
        onlyKo.removeAll(en.keySet());
        Set<String> onlyEn = new TreeSet<>(en.keySet());
        onlyEn.removeAll(ko.keySet());

        List<String> errors = new ArrayList<>();
        if (!onlyKo.isEmpty()) errors.add("en_us 에 없는 키 " + onlyKo.size() + "개: " + preview(onlyKo));
        if (!onlyEn.isEmpty()) errors.add("ko_kr 에 없는 키 " + onlyEn.size() + "개: " + preview(onlyEn));

        Set<String> mismatch = new TreeSet<>();
        for (String key : ko.keySet()) {
            if (!en.has(key)) continue;
            if (placeholders(ko.get(key).getAsString()) != placeholders(en.get(key).getAsString())) {
                mismatch.add(key);
            }
        }
        if (!mismatch.isEmpty()) errors.add("%s 개수가 다른 키 " + mismatch.size() + "개: " + preview(mismatch));

        helper.assertTrue(errors.isEmpty(), String.join(" / ", errors));
        helper.succeed();
    }

    private static JsonObject readLang(String code) {
        String path = "/assets/" + changmin_villager_turret.MODID + "/lang/" + code + ".json";
        try (InputStream in = TurretGameTests.class.getResourceAsStream(path)) {
            if (in == null) return null;
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    private static int placeholders(String text) {
        String cleaned = text.replace("%%", "");
        Matcher m = PLACEHOLDER.matcher(cleaned);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    private static String preview(Set<String> keys) {
        List<String> list = new ArrayList<>(keys);
        return list.size() <= 5 ? list.toString() : list.subList(0, 5) + " …";
    }

    private static String fmt(Vec3 v) {
        return String.format("(%.2f, %.2f, %.2f)", v.x, v.y, v.z);
    }
}

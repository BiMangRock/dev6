package changmin.changmin_villager_turret.feature.turret.resource_easy;

import changmin.changmin_villager_turret.ally.IAlly;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

public class ResourceVillagerEasyEntity extends PathfinderMob implements VillagerDataHolder, IAlly {
    private static final EntityDataAccessor<Integer> DATA_LEVEL = SynchedEntityData.defineId(ResourceVillagerEasyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_RESOURCE_TIMER = SynchedEntityData.defineId(ResourceVillagerEasyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_COMBAT = SynchedEntityData.defineId(ResourceVillagerEasyEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_PHASE_TIMER = SynchedEntityData.defineId(ResourceVillagerEasyEntity.class, EntityDataSerializers.INT);

    // 15초 통합 주기 설정 (15초 * 20틱 = 300틱)
    public static final int RESOURCE_COOLDOWN_MAX = 300;

    // 주민이 살아있던 누적 시간 기록 (전체 진행 제어 및 세이브 보존용)
    private int totalTicksAlive = 0;

    // 소환된 정예 및 보스 몹을 일괄 카운트하기 위한 리스트
    private final List<Mob> spawnedZombies = new ArrayList<>();

    public ResourceVillagerEasyEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_LEVEL, 1);
        this.entityData.define(DATA_RESOURCE_TIMER, RESOURCE_COOLDOWN_MAX);
        this.entityData.define(DATA_IS_COMBAT, true);   // 최초 기동 시 전투로 시작
        this.entityData.define(DATA_PHASE_TIMER, 2400); // LEVEL 1 최초 전투 시간 (2분 = 2400틱)
    }

    @Override
    public VillagerData getVillagerData() {
        return new VillagerData(VillagerType.JUNGLE, VillagerProfession.NONE, 1);
    }

    @Override
    public void setVillagerData(VillagerData data) {
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level.isClientSide) {
            this.totalTicksAlive++;

            // 시간 경과에 따른 전투/정비 페이즈 정보 실시간 갱신 및 동기화
            updatePhaseInfo();

            // 전투 기간에만 15초 타이머가 흘러가며 자원과 좀비를 동시 스폰시킵니다.
            if (this.isCombatPhase()) {
                int rTimer = this.getResourceTimer();
                if (rTimer > 0) {
                    this.setResourceTimer(rTimer - 1);
                } else {
                    spawnResourceAndGainXp();
                    spawnZombieWave();
                    this.setResourceTimer(RESOURCE_COOLDOWN_MAX);
                }
            } else {
                // 휴식 기간에는 타이머를 초기 대기 상태로 정지시켜 둡니다. (휴식기 자원/좀비 일절 없음)
                this.setResourceTimer(RESOURCE_COOLDOWN_MAX);
            }
        }
    }

    // 전투 선순위 루프를 완벽히 통제하는 시간 계산기
    private void updatePhaseInfo() {
        int ticks = this.totalTicksAlive;
        int level = 1;
        boolean isCombat = true;
        int timer = 0;

        if (ticks < 2400) { // 0 ~ 2분 (전투 Level 1)
            level = 1;
            isCombat = true;
            timer = 2400 - ticks;
        } else if (ticks < 7200) { // 2 ~ 6분 (휴식 1 - 4분)
            level = 1;
            isCombat = false;
            timer = 7200 - ticks;
        } else if (ticks < 12000) { // 6 ~ 10분 (전투 Level 2 - 4분)
            level = 2;
            isCombat = true;
            timer = 12000 - ticks;
        } else if (ticks < 16800) { // 10 ~ 14분 (휴식 2 - 4분)
            level = 2;
            isCombat = false;
            timer = 16800 - ticks;
        } else if (ticks < 24000) { // 14 ~ 20분 (전투 Level 3 - 6분)
            level = 3;
            isCombat = true;
            timer = 24000 - ticks;
        } else if (ticks < 28800) { // 20 ~ 24분 (휴식 3 - 4분)
            level = 3;
            isCombat = false;
            timer = 28800 - ticks;
        } else if (ticks < 38400) { // 24 ~ 32분 (전투 Level 4 - 8분)
            level = 4;
            isCombat = true;
            timer = 38400 - ticks;
        } else if (ticks < 43200) { // 32 ~ 36분 (휴식 4 - 4분)
            level = 4;
            isCombat = false;
            timer = 43200 - ticks;
        } else { // 36분 이상 (무한 루프: 10분 전투 + 4분 휴식의 14분 순환)
            int endlessTicks = ticks - 43200;
            int cycleLength = 16800; // 14분 주기
            int offset = endlessTicks % cycleLength;

            level = 5; // Infinite
            if (offset < 12000) { // 10분 전투
                isCombat = true;
                timer = 12000 - offset;
            } else { // 4분 휴식
                isCombat = false;
                timer = 16800 - offset;
            }
        }

        if (this.getTurretLevel() != level) {
            this.setTurretLevel(level);
            updateMaxHealth(level);
        }
        if (this.isCombatPhase() != isCombat) this.setCombatPhase(isCombat);
        if (this.getPhaseTimer() != timer) this.setPhaseTimer(timer);
    }

    private int getMaxZombiesLimit(int level) {
        if (level == 1) return 10;
        if (level == 2) return 20;
        if (level == 3) return 30;
        return 40;
    }

    private void updateMaxHealth(int level) {
        double newMaxHealth = 50.0D + (level - 1) * 10.0D;
        var attribute = this.getAttribute(Attributes.MAX_HEALTH);
        if (attribute != null) {
            attribute.setBaseValue(newMaxHealth);
            this.setHealth((float) newMaxHealth);
        }
    }

    // 🌟 [수정] 모든 스폰 기댓값을 원본 대비 1/5(20%) 확률로 감소시켰으며, 응축된 에메랄드를 완벽 제외시킴
    private void spawnResourceAndGainXp() {
        int level = this.getTurretLevel();

        if (level == 1) {
            // 기존 100% 드롭 -> 20% 확률로 감소
            if (this.random.nextFloat() < 0.20F) {
                this.spawnAtLocation(new ItemStack(Items.EMERALD, 1 + this.random.nextInt(2)));
            }
            // 기존 20% -> 4% 확률로 감소
            if (this.random.nextFloat() < 0.04F) {
                this.spawnAtLocation(new ItemStack(Items.COAL, 1));
            }
            // 기존 20% -> 4% 확률로 감소
            if (this.random.nextFloat() < 0.04F) {
                ItemStack stew = new ItemStack(Items.SUSPICIOUS_STEW);
                SuspiciousStewItem.saveMobEffect(stew, MobEffects.SATURATION, 7);
                this.spawnAtLocation(stew);
            }
        } else if (level == 2) {
            // 기존 100% 드롭 -> 20% 확률로 감소
            if (this.random.nextFloat() < 0.20F) {
                this.spawnAtLocation(new ItemStack(Items.EMERALD, 2 + this.random.nextInt(2)));
            }
            // 기존 30% -> 6% 확률로 감소
            if (this.random.nextFloat() < 0.06F) {
                this.spawnAtLocation(new ItemStack(Items.IRON_INGOT, 1));
            }
            // 기존 50% -> 10% 확률로 감소
            if (this.random.nextFloat() < 0.10F) {
                this.spawnAtLocation(new ItemStack(Items.BREAD, 1));
            }
            // 기존 30% -> 6% 확률로 감소
            if (this.random.nextFloat() < 0.06F) {
                this.spawnAtLocation(new ItemStack(Items.HONEY_BOTTLE, 1));
            }
            // 기존 20% -> 4% 확률로 감소
            if (this.random.nextFloat() < 0.04F) {
                ItemStack pot = new ItemStack(Items.POTION);
                PotionUtils.setPotion(pot, Potions.SWIFTNESS);
                this.spawnAtLocation(pot);
            }
            // 기존 15% -> 3% 확률로 감소
            if (this.random.nextFloat() < 0.03F) {
                ItemStack pot = new ItemStack(Items.POTION);
                PotionUtils.setPotion(pot, Potions.NIGHT_VISION);
                this.spawnAtLocation(pot);
            }
        } else if (level == 3) {
            // 기존 100% 드롭 -> 20% 확률로 감소
            if (this.random.nextFloat() < 0.20F) {
                this.spawnAtLocation(new ItemStack(Items.EMERALD, 3 + this.random.nextInt(3)));
            }
            // 기존 40% -> 8% 확률로 감소
            if (this.random.nextFloat() < 0.08F) {
                this.spawnAtLocation(new ItemStack(Items.GOLD_INGOT, 1));
            }
            // 기존 50% -> 10% 확률로 감소
            if (this.random.nextFloat() < 0.10F) {
                this.spawnAtLocation(new ItemStack(Items.EXPERIENCE_BOTTLE, 1));
            }
            // 기존 25% -> 5% 확률로 감소
            if (this.random.nextFloat() < 0.05F) {
                this.spawnAtLocation(getRandomEnchantedBook(1, 2));
            }
            // 기존 50% -> 10% 확률로 감소
            if (this.random.nextFloat() < 0.10F) {
                this.spawnAtLocation(new ItemStack(Items.LAPIS_LAZULI, 2 + this.random.nextInt(3)));
            }
            // 기존 15% -> 3% 확률로 감소
            if (this.random.nextFloat() < 0.03F) {
                this.spawnAtLocation(new ItemStack(Items.SHIELD, 1));
            }
            // 기존 20% -> 4% 확률로 감소
            if (this.random.nextFloat() < 0.04F) {
                ItemStack pot = new ItemStack(Items.POTION);
                PotionUtils.setPotion(pot, Potions.STRENGTH);
                this.spawnAtLocation(pot);
            }
            // 기존 15% -> 3% 확률로 감소
            if (this.random.nextFloat() < 0.03F) {
                ItemStack pot = new ItemStack(Items.POTION);
                PotionUtils.setPotion(pot, Potions.SLOW_FALLING);
                this.spawnAtLocation(pot);
            }
        } else if (level == 4) {
            // 🌟 응축된 에메랄드 원본 제외 처리 -> 오직 에메랄드 5개 드롭을 1/5 확률(20%)로 제공
            if (this.random.nextFloat() < 0.20F) {
                this.spawnAtLocation(new ItemStack(Items.EMERALD, 5));
            }
            // 기존 40% -> 8% 확률로 감소
            if (this.random.nextFloat() < 0.08F) {
                this.spawnAtLocation(new ItemStack(Items.DIAMOND, 1 + this.random.nextInt(2)));
            }
            // 기존 70% -> 14% 확률로 감소
            if (this.random.nextFloat() < 0.14F) {
                this.spawnAtLocation(new ItemStack(Items.EXPERIENCE_BOTTLE, 2));
            }
            // 기존 35% -> 7% 확률로 감소
            if (this.random.nextFloat() < 0.07F) {
                this.spawnAtLocation(getRandomEnchantedBook(1, 3));
            }
            // 기존 20% -> 4% 확률로 감소
            if (this.random.nextFloat() < 0.04F) {
                this.spawnAtLocation(new ItemStack(Items.ENDER_PEARL, 4));
            }
            // 기존 30% -> 6% 확률로 감소
            if (this.random.nextFloat() < 0.06F) {
                ItemStack pot = new ItemStack(Items.SPLASH_POTION);
                PotionUtils.setPotion(pot, Potions.STRONG_HEALING);
                this.spawnAtLocation(pot);
            }
            // 기존 10% -> 2% 확률로 감소
            if (this.random.nextFloat() < 0.02F) {
                ItemStack pot = new ItemStack(Items.POTION);
                PotionUtils.setPotion(pot, Potions.TURTLE_MASTER);
                this.spawnAtLocation(pot);
            }
            // 기존 20% -> 4% 확률로 감소
            if (this.random.nextFloat() < 0.04F) {
                this.spawnAtLocation(new ItemStack(Items.GOLDEN_APPLE, 1));
            }
            // 기존 25% -> 5% 확률로 감소
            if (this.random.nextFloat() < 0.05F) {
                this.spawnAtLocation(new ItemStack(Items.SPECTRAL_ARROW, 16));
            }
        } else { // Level 5 (INFINITE)
            // 🌟 응축된 에메랄드 대형 배제 -> 대신 20%(1/5) 확률로 일반 에메랄드 9개 드롭 연산 교체
            if (this.random.nextFloat() < 0.20F) {
                this.spawnAtLocation(new ItemStack(Items.EMERALD, 9));
            }
            // 기존 40% -> 8% 확률로 감소
            if (this.random.nextFloat() < 0.08F) {
                this.spawnAtLocation(new ItemStack(Items.DIAMOND, 1 + this.random.nextInt(2)));
            }
            // 기존 100% -> 20% 확률로 감소
            if (this.random.nextFloat() < 0.20F) {
                this.spawnAtLocation(new ItemStack(Items.EXPERIENCE_BOTTLE, 3));
            }
            // 기존 45% -> 9% 확률로 감소
            if (this.random.nextFloat() < 0.09F) {
                this.spawnAtLocation(getRandomEnchantedBook(3, 3));
            }
            // 기존 5% -> 1% 확률로 감소
            if (this.random.nextFloat() < 0.01F) {
                this.spawnAtLocation(new ItemStack(Items.TOTEM_OF_UNDYING, 1));
            }
            // 기존 10% -> 2% 확률로 감소
            if (this.random.nextFloat() < 0.02F) {
                this.spawnAtLocation(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
            }
            // 기존 5% -> 1% 확률로 감소
            if (this.random.nextFloat() < 0.01F) {
                this.spawnAtLocation(new ItemStack(Items.ELYTRA, 1));
            }
            // 기존 5% -> 1% 확률로 감소
            if (this.random.nextFloat() < 0.01F) {
                this.spawnAtLocation(new ItemStack(Items.FIREWORK_ROCKET, 16));
            }
            // 기존 10% -> 2% 확률로 감소
            if (this.random.nextFloat() < 0.02F) {
                this.spawnAtLocation(new ItemStack(Items.NETHERITE_INGOT, 1));
            }
            // 기존 3% -> 0.6% 확률로 감소
            if (this.random.nextFloat() < 0.006F) {
                this.spawnAtLocation(new ItemStack(Items.BEACON, 1));
            }
            // 기존 10% -> 2% 확률로 감소
            if (this.random.nextFloat() < 0.02F) {
                this.spawnAtLocation(getRandomShulkerBox());
            }
        }
    }

    private ItemStack getRandomShulkerBox() {
        net.minecraft.world.item.Item[] boxes = {
                Items.SHULKER_BOX, Items.WHITE_SHULKER_BOX, Items.ORANGE_SHULKER_BOX,
                Items.MAGENTA_SHULKER_BOX, Items.LIGHT_BLUE_SHULKER_BOX, Items.YELLOW_SHULKER_BOX,
                Items.LIME_SHULKER_BOX, Items.PINK_SHULKER_BOX, Items.GRAY_SHULKER_BOX,
                Items.LIGHT_GRAY_SHULKER_BOX, Items.CYAN_SHULKER_BOX, Items.PURPLE_SHULKER_BOX,
                Items.BLUE_SHULKER_BOX, Items.BROWN_SHULKER_BOX, Items.GREEN_SHULKER_BOX,
                Items.RED_SHULKER_BOX, Items.BLACK_SHULKER_BOX
        };
        return new ItemStack(boxes[this.random.nextInt(boxes.length)]);
    }

    // 🌟 [수정] 최종 인챈트 레벨 3 고정 장치 추가 (4레벨 도달 원천 차단)
    private ItemStack getRandomEnchantedBook(int minLvl, int maxLvl) {
        int resolvedMax = Math.min(maxLvl, 3);
        int resolvedMin = Math.min(minLvl, resolvedMax);

        List<Enchantment> enchantments = Registry.ENCHANTMENT.stream().toList();
        if (!enchantments.isEmpty()) {
            Enchantment randomEnch = enchantments.get(this.random.nextInt(enchantments.size()));
            int minPossible = Math.max(randomEnch.getMinLevel(), resolvedMin);
            int maxPossible = Math.min(randomEnch.getMaxLevel(), resolvedMax);
            if (minPossible > maxPossible) {
                minPossible = randomEnch.getMinLevel();
                maxPossible = Math.min(randomEnch.getMaxLevel(), resolvedMax);
            }
            int lvl = minPossible;
            if (maxPossible > minPossible) {
                lvl = minPossible + this.random.nextInt(maxPossible - minPossible + 1);
            }
            lvl = Math.min(lvl, 3); // 4레벨 드롭 원천 차단 2중 안전 장치
            return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(randomEnch, lvl));
        }
        return new ItemStack(Items.BOOK);
    }

    public int getXp() {
        return RESOURCE_COOLDOWN_MAX - this.getResourceTimer();
    }

    public int getNeededXp() {
        return RESOURCE_COOLDOWN_MAX;
    }

    private void spawnZombieWave() {
        this.spawnedZombies.removeIf(zombie -> zombie == null || !zombie.isAlive() || zombie.isRemoved());

        int level = this.getTurretLevel();
        int maxZombies = getMaxZombiesLimit(level);
        int maxAllowable = maxZombies - this.spawnedZombies.size();

        if (maxAllowable <= 0) {
            return;
        }

        int spawnCount = 1;
        if (level == 1) {
            spawnCount = 1 + this.random.nextInt(3);
        } else if (level == 2) {
            spawnCount = 2 + this.random.nextInt(4);
        } else if (level == 3) {
            spawnCount = 4 + this.random.nextInt(5);
        } else if (level == 4) {
            spawnCount = 8 + this.random.nextInt(8);
        } else {
            spawnCount = 15 + this.random.nextInt(16);
        }

        spawnCount = Math.min(spawnCount, maxAllowable);

        for (int i = 0; i < spawnCount; i++) {
            double angle = this.random.nextDouble() * 2 * Math.PI;
            double radius = 8.0D + this.random.nextDouble() * 4.0D;
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            double y = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) x, (int) z);

            Mob mob = createRandomZombieByLevel(level);
            if (mob != null) {
                mob.moveTo(x, y, z, this.random.nextFloat() * 360F, 0.0F);
                this.level.addFreshEntity(mob);
                mob.setTarget(this);

                this.spawnedZombies.add(mob);
            }
        }
    }

    // 🌟 [수정] 모드 좀비 제거 및 바닐라 3종으로 세팅 교체
    private Mob createRandomZombieByLevel(int level) {
        float r = this.random.nextFloat();
        Zombie zombie;

        if (level == 1) {
            zombie = EntityType.ZOMBIE.create(this.level);
        } else if (level == 2) {
            if (r < 0.30F) {
                zombie = EntityType.HUSK.create(this.level);
            } else {
                zombie = EntityType.ZOMBIE.create(this.level);
            }
        } else if (level == 3) {
            if (r < 0.20F) {
                zombie = EntityType.DROWNED.create(this.level);
            } else if (r < 0.50F) {
                zombie = EntityType.HUSK.create(this.level);
            } else {
                zombie = EntityType.ZOMBIE.create(this.level);
            }
        } else if (level == 4) {
            if (r < 0.30F) {
                zombie = EntityType.DROWNED.create(this.level);
            } else if (r < 0.60F) {
                zombie = EntityType.HUSK.create(this.level);
            } else {
                zombie = EntityType.ZOMBIE.create(this.level);
            }
        } else { // Level 5 (INFINITE)
            if (r < 0.40F) {
                zombie = EntityType.DROWNED.create(this.level);
            } else if (r < 0.70F) {
                zombie = EntityType.HUSK.create(this.level);
            } else {
                zombie = EntityType.ZOMBIE.create(this.level);
            }
        }

        if (zombie != null) {
            // 세세하게 정립된 파트별 무작위 비대칭 갑옷 계산 시스템 탑재
            applyRandomGear(zombie, level);
        }
        return zombie;
    }

    // 🌟 [수정] 비대칭 독립 롤링 장비 배정 알고리즘
    private void applyRandomGear(Zombie zombie, int level) {
        // 1. 머리 장막 설정 (초반 가벼운 발사 및 햇빛 면역 연산)
        float helmChance;
        if (level == 1) helmChance = 0.85F;
        else if (level == 2) helmChance = 0.70F;
        else if (level == 3) helmChance = 0.50F;
        else if (level == 4) helmChance = 0.55F;
        else helmChance = 0.75F;

        if (this.random.nextFloat() < helmChance) {
            ItemStack helm = getArmorPiece(EquipmentSlot.HEAD, level);
            if (!helm.isEmpty()) zombie.setItemSlot(EquipmentSlot.HEAD, helm);
        }

        // 2. 가슴 갑바 설정
        float chestChance;
        if (level == 1) chestChance = 0.15F;
        else if (level == 2) chestChance = 0.30F;
        else if (level == 3) chestChance = 0.50F;
        else if (level == 4) chestChance = 0.60F;
        else chestChance = 0.85F;

        if (this.random.nextFloat() < chestChance) {
            ItemStack chest = getArmorPiece(EquipmentSlot.CHEST, level);
            if (!chest.isEmpty()) zombie.setItemSlot(EquipmentSlot.CHEST, chest);
        }

        // 3. 다리 바지 설정
        float legsChance;
        if (level == 1) legsChance = 0.05F;
        else if (level == 2) legsChance = 0.20F;
        else if (level == 3) legsChance = 0.40F;
        else if (level == 4) legsChance = 0.50F;
        else legsChance = 0.70F;

        if (this.random.nextFloat() < legsChance) {
            ItemStack legs = getArmorPiece(EquipmentSlot.LEGS, level);
            if (!legs.isEmpty()) zombie.setItemSlot(EquipmentSlot.LEGS, legs);
        }

        // 4. 신발 설정
        float bootsChance;
        if (level == 1) bootsChance = 0.05F;
        else if (level == 2) bootsChance = 0.20F;
        else if (level == 3) bootsChance = 0.40F;
        else if (level == 4) bootsChance = 0.50F;
        else bootsChance = 0.70F;

        if (this.random.nextFloat() < bootsChance) {
            ItemStack boots = getArmorPiece(EquipmentSlot.FEET, level);
            if (!boots.isEmpty()) zombie.setItemSlot(EquipmentSlot.FEET, boots);
        }

        // 5. 무기 권장 확률 설정
        float weaponChance;
        if (level == 1) weaponChance = 0.10F;
        else if (level == 2) weaponChance = 0.20F;
        else if (level == 3) weaponChance = 0.40F;
        else if (level == 4) weaponChance = 0.60F;
        else weaponChance = 0.80F;

        if (this.random.nextFloat() < weaponChance) {
            ItemStack weapon = getWeaponPiece(level);
            if (!weapon.isEmpty()) zombie.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        }
    }

    // 슬롯 재질 확률 연산부
    private ItemStack getArmorPiece(EquipmentSlot slot, int level) {
        float r = this.random.nextFloat();
        if (level == 1) {
            // 90% 가죽, 10% 금
            if (r < 0.90F) return new ItemStack(getLeatherItem(slot));
            else return new ItemStack(getGoldItem(slot));
        } else if (level == 2) {
            // 60% 가죽, 30% 금, 10% 사슬
            if (r < 0.60F) return new ItemStack(getLeatherItem(slot));
            else if (r < 0.90F) return new ItemStack(getGoldItem(slot));
            else return new ItemStack(getChainItem(slot));
        } else if (level == 3) {
            // 20% 금, 50% 사슬, 30% 철
            if (r < 0.20F) return new ItemStack(getGoldItem(slot));
            else if (r < 0.70F) return new ItemStack(getChainItem(slot));
            else return new ItemStack(getIronItem(slot));
        } else if (level == 4) {
            // 20% 사슬, 60% 철, 20% 다이아몬드
            if (r < 0.20F) return new ItemStack(getChainItem(slot));
            else if (r < 0.80F) return new ItemStack(getIronItem(slot));
            else return new ItemStack(getDiamondItem(slot));
        } else {
            // Level 5 (INFINITE): 15% 철, 55% 다이아몬드, 30% 마법부여 다이아몬드
            if (r < 0.15F) return new ItemStack(getIronItem(slot));
            else if (r < 0.70F) return new ItemStack(getDiamondItem(slot));
            else {
                ItemStack stack = new ItemStack(getDiamondItem(slot));
                stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 1 + this.random.nextInt(4));
                return stack;
            }
        }
    }

    private net.minecraft.world.item.Item getLeatherItem(EquipmentSlot slot) {
        switch(slot) {
            case HEAD: return Items.LEATHER_HELMET;
            case CHEST: return Items.LEATHER_CHESTPLATE;
            case LEGS: return Items.LEATHER_LEGGINGS;
            default: return Items.LEATHER_BOOTS;
        }
    }

    private net.minecraft.world.item.Item getGoldItem(EquipmentSlot slot) {
        switch(slot) {
            case HEAD: return Items.GOLDEN_HELMET;
            case CHEST: return Items.GOLDEN_CHESTPLATE;
            case LEGS: return Items.GOLDEN_LEGGINGS;
            default: return Items.GOLDEN_BOOTS;
        }
    }

    private net.minecraft.world.item.Item getChainItem(EquipmentSlot slot) {
        switch(slot) {
            case HEAD: return Items.CHAINMAIL_HELMET;
            case CHEST: return Items.CHAINMAIL_CHESTPLATE;
            case LEGS: return Items.CHAINMAIL_LEGGINGS;
            default: return Items.CHAINMAIL_BOOTS;
        }
    }

    private net.minecraft.world.item.Item getIronItem(EquipmentSlot slot) {
        switch(slot) {
            case HEAD: return Items.IRON_HELMET;
            case CHEST: return Items.IRON_CHESTPLATE;
            case LEGS: return Items.IRON_LEGGINGS;
            default: return Items.IRON_BOOTS;
        }
    }

    private net.minecraft.world.item.Item getDiamondItem(EquipmentSlot slot) {
        switch(slot) {
            case HEAD: return Items.DIAMOND_HELMET;
            case CHEST: return Items.DIAMOND_CHESTPLATE;
            case LEGS: return Items.DIAMOND_LEGGINGS;
            default: return Items.DIAMOND_BOOTS;
        }
    }

    private ItemStack getWeaponPiece(int level) {
        float r = this.random.nextFloat();
        if (level == 1) {
            return new ItemStack(r < 0.50F ? Items.WOODEN_SWORD : Items.WOODEN_SHOVEL);
        } else if (level == 2) {
            return new ItemStack(r < 0.50F ? Items.STONE_SWORD : Items.WOODEN_SWORD);
        } else if (level == 3) {
            if (r < 0.40F) return new ItemStack(Items.STONE_SWORD);
            else if (r < 0.80F) return new ItemStack(Items.IRON_SWORD);
            else return new ItemStack(Items.TRIDENT);
        } else if (level == 4) {
            ItemStack weapon;
            if (r < 0.40F) weapon = new ItemStack(Items.IRON_SWORD);
            else if (r < 0.80F) weapon = new ItemStack(Items.IRON_AXE);
            else weapon = new ItemStack(Items.TRIDENT);
            if (this.random.nextFloat() < 0.15F) {
                weapon.enchant(Enchantments.SHARPNESS, 1);
            }
            return weapon;
        } else {
            ItemStack weapon;
            if (r < 0.40F) weapon = new ItemStack(Items.DIAMOND_SWORD);
            else if (r < 0.70F) weapon = new ItemStack(Items.TRIDENT);
            else {
                weapon = new ItemStack(Items.DIAMOND_SWORD);
                weapon.enchant(Enchantments.SHARPNESS, 2 + this.random.nextInt(4));
            }
            if (weapon.is(Items.TRIDENT) && this.random.nextFloat() < 0.30F) {
                weapon.enchant(Enchantments.LOYALTY, 1 + this.random.nextInt(3));
            }
            return weapon;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TurretLevel", this.getTurretLevel());
        tag.putInt("ResourceTimer", this.getResourceTimer());
        tag.putInt("TotalTicksAlive", this.totalTicksAlive);
        tag.putBoolean("IsCombatPhase", this.isCombatPhase());
        tag.putInt("PhaseTimer", this.getPhaseTimer());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setTurretLevel(tag.contains("TurretLevel") ? tag.getInt("TurretLevel") : 1);
        if (tag.contains("ResourceTimer")) this.setResourceTimer(tag.getInt("ResourceTimer"));
        if (tag.contains("TotalTicksAlive")) this.totalTicksAlive = tag.getInt("TotalTicksAlive");
        if (tag.contains("IsCombatPhase")) this.setCombatPhase(tag.getBoolean("IsCombatPhase"));
        if (tag.contains("PhaseTimer")) this.setPhaseTimer(tag.getInt("PhaseTimer"));

        updateMaxHealth(this.getTurretLevel());
    }

    public int getWaveCount() { return 1; }
    public void setWaveCount(int wave) {}

    public int getTurretLevel() { return this.entityData.get(DATA_LEVEL); }
    public void setTurretLevel(int lvl) { this.entityData.set(DATA_LEVEL, lvl); }
    public int getResourceTimer() { return this.entityData.get(DATA_RESOURCE_TIMER); }
    public void setResourceTimer(int timer) { this.entityData.set(DATA_RESOURCE_TIMER, timer); }

    public boolean isCombatPhase() { return this.entityData.get(DATA_IS_COMBAT); }
    public void setCombatPhase(boolean combat) { this.entityData.set(DATA_IS_COMBAT, combat); }
    public int getPhaseTimer() { return this.entityData.get(DATA_PHASE_TIMER); }
    public void setPhaseTimer(int timer) { this.entityData.set(DATA_PHASE_TIMER, timer); }
}
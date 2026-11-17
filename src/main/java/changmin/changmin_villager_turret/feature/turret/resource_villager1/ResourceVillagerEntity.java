package changmin.changmin_villager_turret.feature.turret.resource_villager1;

import changmin.changmin_villager_turret.ally.IAlly;
import changmin.changmin_villager_turret.registry.ModEntityTypes;
import changmin.changmin_villager_turret.registry.ModItems;
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

public class ResourceVillagerEntity extends PathfinderMob implements VillagerDataHolder, IAlly {
    private static final EntityDataAccessor<Integer> DATA_LEVEL = SynchedEntityData.defineId(ResourceVillagerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_RESOURCE_TIMER = SynchedEntityData.defineId(ResourceVillagerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_COMBAT = SynchedEntityData.defineId(ResourceVillagerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_PHASE_TIMER = SynchedEntityData.defineId(ResourceVillagerEntity.class, EntityDataSerializers.INT);

    // 15초 통합 주기 설정 (15초 * 20틱 = 300틱)
    public static final int RESOURCE_COOLDOWN_MAX = 300;

    // 주민이 살아있던 누적 시간 기록 (전체 진행 제어 및 세이브 보존용)
    private int totalTicksAlive = 0;

    // 소환된 정예 및 보스 몹을 일괄 카운트하기 위한 리스트
    private final List<Mob> spawnedZombies = new ArrayList<>();

    public ResourceVillagerEntity(EntityType<? extends PathfinderMob> type, Level level) {
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

        if (!this.level().isClientSide) {
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

    private void spawnResourceAndGainXp() {
        int level = this.getTurretLevel();

        if (level == 1) {
            this.spawnAtLocation(new ItemStack(Items.EMERALD, 1 + this.random.nextInt(2)));
            if (this.random.nextFloat() < 0.20F) {
                this.spawnAtLocation(new ItemStack(Items.COAL, 1));
            }
            if (this.random.nextFloat() < 0.20F) {
                ItemStack stew = new ItemStack(Items.SUSPICIOUS_STEW);
                SuspiciousStewItem.saveMobEffect(stew, MobEffects.SATURATION, 7);
                this.spawnAtLocation(stew);
            }
        } else if (level == 2) {
            this.spawnAtLocation(new ItemStack(Items.EMERALD, 2 + this.random.nextInt(2)));
            if (this.random.nextFloat() < 0.30F) {
                this.spawnAtLocation(new ItemStack(Items.IRON_INGOT, 1));
            }
            if (this.random.nextFloat() < 0.50F) {
                this.spawnAtLocation(new ItemStack(Items.BREAD, 1));
            }
            if (this.random.nextFloat() < 0.30F) {
                this.spawnAtLocation(new ItemStack(Items.HONEY_BOTTLE, 1));
            }
            if (this.random.nextFloat() < 0.20F) {
                ItemStack pot = new ItemStack(Items.POTION);
                PotionUtils.setPotion(pot, Potions.SWIFTNESS);
                this.spawnAtLocation(pot);
            }
            if (this.random.nextFloat() < 0.15F) {
                ItemStack pot = new ItemStack(Items.POTION);
                PotionUtils.setPotion(pot, Potions.NIGHT_VISION);
                this.spawnAtLocation(pot);
            }
        } else if (level == 3) {
            this.spawnAtLocation(new ItemStack(Items.EMERALD, 3 + this.random.nextInt(3)));
            if (this.random.nextFloat() < 0.40F) {
                this.spawnAtLocation(new ItemStack(Items.GOLD_INGOT, 1));
            }
            if (this.random.nextFloat() < 0.50F) {
                this.spawnAtLocation(new ItemStack(Items.EXPERIENCE_BOTTLE, 1));
            }
            if (this.random.nextFloat() < 0.25F) {
                this.spawnAtLocation(getRandomEnchantedBook(1, 2));
            }
            if (this.random.nextFloat() < 0.50F) {
                this.spawnAtLocation(new ItemStack(Items.LAPIS_LAZULI, 2 + this.random.nextInt(3)));
            }
            if (this.random.nextFloat() < 0.15F) {
                this.spawnAtLocation(new ItemStack(Items.SHIELD, 1));
            }
            if (this.random.nextFloat() < 0.20F) {
                ItemStack pot = new ItemStack(Items.POTION);
                PotionUtils.setPotion(pot, Potions.STRENGTH);
                this.spawnAtLocation(pot);
            }
            if (this.random.nextFloat() < 0.15F) {
                ItemStack pot = new ItemStack(Items.POTION);
                PotionUtils.setPotion(pot, Potions.SLOW_FALLING);
                this.spawnAtLocation(pot);
            }
        } else if (level == 4) {
            if (this.random.nextFloat() < 0.20F) {
                this.spawnAtLocation(new ItemStack(ModItems.CONDENSED_EMERALD.get(), 1));
            } else {
                this.spawnAtLocation(new ItemStack(Items.EMERALD, 5));
            }
            if (this.random.nextFloat() < 0.40F) {
                this.spawnAtLocation(new ItemStack(Items.DIAMOND, 1 + this.random.nextInt(2)));
            }
            if (this.random.nextFloat() < 0.70F) {
                this.spawnAtLocation(new ItemStack(Items.EXPERIENCE_BOTTLE, 2));
            }
            if (this.random.nextFloat() < 0.35F) {
                this.spawnAtLocation(getRandomEnchantedBook(1, 3));
            }
            if (this.random.nextFloat() < 0.20F) {
                this.spawnAtLocation(new ItemStack(Items.ENDER_PEARL, 4));
            }
            if (this.random.nextFloat() < 0.30F) {
                ItemStack pot = new ItemStack(Items.SPLASH_POTION);
                PotionUtils.setPotion(pot, Potions.STRONG_HEALING);
                this.spawnAtLocation(pot);
            }
            if (this.random.nextFloat() < 0.10F) {
                ItemStack pot = new ItemStack(Items.POTION);
                PotionUtils.setPotion(pot, Potions.TURTLE_MASTER);
                this.spawnAtLocation(pot);
            }
            if (this.random.nextFloat() < 0.20F) {
                this.spawnAtLocation(new ItemStack(Items.GOLDEN_APPLE, 1));
            }
            if (this.random.nextFloat() < 0.25F) {
                this.spawnAtLocation(new ItemStack(Items.SPECTRAL_ARROW, 16));
            }
        } else { // Level 5 (INFINITE)
            this.spawnAtLocation(new ItemStack(ModItems.CONDENSED_EMERALD.get(), 1));
            if (this.random.nextFloat() < 0.40F) {
                this.spawnAtLocation(new ItemStack(Items.DIAMOND, 1 + this.random.nextInt(2)));
            }
            this.spawnAtLocation(new ItemStack(Items.EXPERIENCE_BOTTLE, 3));
            if (this.random.nextFloat() < 0.45F) {
                this.spawnAtLocation(getRandomEnchantedBook(3, 4));
            }
            if (this.random.nextFloat() < 0.05F) {
                this.spawnAtLocation(new ItemStack(Items.TOTEM_OF_UNDYING, 1));
            }
            if (this.random.nextFloat() < 0.10F) {
                this.spawnAtLocation(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
            }
            if (this.random.nextFloat() < 0.05F) {
                this.spawnAtLocation(new ItemStack(Items.ELYTRA, 1));
            }
            if (this.random.nextFloat() < 0.05F) {
                this.spawnAtLocation(new ItemStack(Items.FIREWORK_ROCKET, 16));
            }
            if (this.random.nextFloat() < 0.10F) {
                this.spawnAtLocation(new ItemStack(Items.NETHERITE_INGOT, 1));
            }
            if (this.random.nextFloat() < 0.03F) {
                this.spawnAtLocation(new ItemStack(Items.BEACON, 1));
            }
            if (this.random.nextFloat() < 0.10F) {
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

    private ItemStack getRandomEnchantedBook(int minLvl, int maxLvl) {
        List<Enchantment> enchantments = net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.stream().toList();
        if (!enchantments.isEmpty()) {
            Enchantment randomEnch = enchantments.get(this.random.nextInt(enchantments.size()));
            int minPossible = Math.max(randomEnch.getMinLevel(), minLvl);
            int maxPossible = Math.min(randomEnch.getMaxLevel(), maxLvl);
            if (minPossible > maxPossible) {
                minPossible = randomEnch.getMinLevel();
                maxPossible = randomEnch.getMaxLevel();
            }
            int lvl = minPossible + this.random.nextInt(maxPossible - minPossible + 1);
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
            double y = this.level().getHeight(Heightmap.Types.WORLD_SURFACE, (int) x, (int) z);

            Mob mob = createRandomZombieByLevel(level);
            if (mob != null) {
                mob.moveTo(x, y, z, this.random.nextFloat() * 360F, 0.0F);
                this.level().addFreshEntity(mob);
                mob.setTarget(this);

                this.spawnedZombies.add(mob);
            }
        }
    }

    private Mob createRandomZombieByLevel(int level) {
        if (level == 1) {
            return EntityType.ZOMBIE.create(this.level());
        }
        if (level == 2) {
            Zombie zombie = EntityType.ZOMBIE.create(this.level());
            if (zombie != null) {
                applyLeatherGoldGear(zombie);
            }
            return zombie;
        }
        if (level == 3) {
            float r = this.random.nextFloat();
            if (r < 0.15F) return ModEntityTypes.HEALER_ZOMBIE.get().create(this.level());
            if (r < 0.30F) return ModEntityTypes.ASSASSIN2.get().create(this.level());
            Zombie zombie = EntityType.ZOMBIE.create(this.level());
            if (zombie != null) {
                applyChainmailIronGear(zombie);
            }
            return zombie;
        }
        if (level == 4) {
            float r = this.random.nextFloat();
            if (r < 0.20F) return ModEntityTypes.ZOMBIE_COMMANDER.get().create(this.level());
            if (r < 0.40F) return ModEntityTypes.ANGEL_ZOMBIE.get().create(this.level());
            if (r < 0.50F) return ModEntityTypes.ZOMBIE_BOSS.get().create(this.level());
            Zombie zombie = EntityType.ZOMBIE.create(this.level());
            if (zombie != null) {
                applyIronDiamondGear(zombie);
            }
            return zombie;
        }

        int choice = this.random.nextInt(8);
        switch (choice) {
            case 0 -> {
                Zombie zombie = EntityType.ZOMBIE.create(this.level());
                if (zombie != null) applyGodGear(zombie);
                return zombie;
            }
            case 1 -> { return ModEntityTypes.HEALER_ZOMBIE.get().create(this.level()); }
            case 2 -> { return ModEntityTypes.ASSASSIN2.get().create(this.level()); }
            case 3 -> { return ModEntityTypes.ZOMBIE_COMMANDER.get().create(this.level()); }
            case 4 -> { return ModEntityTypes.ANGEL_ZOMBIE.get().create(this.level()); }
            case 5 -> { return ModEntityTypes.RAGED_ANGEL_ZOMBIE.get().create(this.level()); }
            case 6 -> { return ModEntityTypes.ZOMBIE_BOSS.get().create(this.level()); }
            default -> { return ModEntityTypes.APOSTLE_OF_THE_END.get().create(this.level()); }
        }
    }

    private void applyLeatherGoldGear(Zombie zombie) {
        zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(this.random.nextBoolean() ? Items.LEATHER_HELMET : Items.GOLDEN_HELMET));
        zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_SWORD));
    }

    private void applyChainmailIronGear(Zombie zombie) {
        zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
        zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
    }

    private void applyIronDiamondGear(Zombie zombie) {
        zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        zombie.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        zombie.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
    }

    private void applyGodGear(Zombie zombie) {
        ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET);
        helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        zombie.setItemSlot(EquipmentSlot.HEAD, helmet);

        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chestplate.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        zombie.setItemSlot(EquipmentSlot.CHEST, chestplate);

        ItemStack leggings = new ItemStack(Items.DIAMOND_LEGGINGS);
        leggings.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        zombie.setItemSlot(EquipmentSlot.LEGS, leggings);

        ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
        boots.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        zombie.setItemSlot(EquipmentSlot.FEET, boots);

        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 5);
        zombie.setItemSlot(EquipmentSlot.MAINHAND, sword);
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

        // 저장된 체력 유지: updateMaxHealth()가 체력을 가득 채우므로, 불러오기(재접속·회수 후 재설치) 때는 원래 체력으로 되돌립니다.
        float savedHealth = this.getHealth();
        updateMaxHealth(this.getTurretLevel());
        if (tag.contains("Health")) {
            this.setHealth(Math.min(savedHealth, this.getMaxHealth()));
        }
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
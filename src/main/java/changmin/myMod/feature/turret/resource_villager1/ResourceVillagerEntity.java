package changmin.myMod.feature.turret.resource_villager1;

import changmin.myMod.registry.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

public class ResourceVillagerEntity extends PathfinderMob implements VillagerDataHolder {
    private static final EntityDataAccessor<Integer> DATA_WAVE_COUNT = SynchedEntityData.defineId(ResourceVillagerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LEVEL = SynchedEntityData.defineId(ResourceVillagerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CURRENT_XP = SynchedEntityData.defineId(ResourceVillagerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_RESOURCE_TIMER = SynchedEntityData.defineId(ResourceVillagerEntity.class, EntityDataSerializers.INT);

//    private static final int RESOURCE_COOLDOWN_MAX = 1200; // 60초
    private static final int RESOURCE_COOLDOWN_MAX = 200; // 10초 (10초 * 20틱)
    private static final int ZOMBIE_COOLDOWN_MAX = RESOURCE_COOLDOWN_MAX/2;    // 절반

    private int zombieTimer = ZOMBIE_COOLDOWN_MAX;

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
        this.entityData.define(DATA_WAVE_COUNT, 1);
        this.entityData.define(DATA_LEVEL, 1);
        this.entityData.define(DATA_CURRENT_XP, 0);
        this.entityData.define(DATA_RESOURCE_TIMER, RESOURCE_COOLDOWN_MAX);
    }

    // 주민 렌더러가 정글 바이옴 텍스처를 오버레이할 수 있도록 데이터 제공
    @Override
    public VillagerData getVillagerData() {
        return new VillagerData(VillagerType.JUNGLE, VillagerProfession.NONE, 1);
    }

    @Override
    public void setVillagerData(VillagerData data) {
        // 읽기 전용으로 유지하거나 필요 시 처리
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
            int rTimer = this.getResourceTimer();
            if (rTimer > 0) {
                this.setResourceTimer(rTimer - 1);
            } else {
                spawnResourceAndGainXp();
                this.setResourceTimer(RESOURCE_COOLDOWN_MAX);
            }

            if (this.zombieTimer > 0) {
                this.zombieTimer--;
            } else {
                spawnZombieWave();
                this.zombieTimer = ZOMBIE_COOLDOWN_MAX;
            }
        }
    }

    private void spawnResourceAndGainXp() {
        int level = this.getTurretLevel();
        int emeraldCount = level;

        if (level >= 5) {
            this.spawnAtLocation(new ItemStack(ModItems.CONDENSED_EMERALD.get(), emeraldCount / 5 + 1));
            if (this.random.nextFloat() < 0.10F) {
                this.spawnAtLocation(new ItemStack(Items.NETHERITE_SCRAP, 1));
            }
            this.spawnAtLocation(getRandomEnchantedBook());
        } else {
            this.spawnAtLocation(new ItemStack(Items.EMERALD, emeraldCount));
        }

        if (level >= 3) {
            if (this.random.nextFloat() < 0.30F) {
                this.spawnAtLocation(new ItemStack(Items.GOLD_INGOT, 1));
            }
            if (this.random.nextFloat() < 0.15F) {
                this.spawnAtLocation(new ItemStack(Items.DIAMOND, 1));
            }
            if (this.random.nextFloat() < 0.35F) {
                this.spawnAtLocation(new ItemStack(Items.EXPERIENCE_BOTTLE, 1 + (level - 3)));
            }
        }

        if (level >= 4 && this.random.nextFloat() < 0.15F) {
            this.spawnAtLocation(getRandomEnchantedBook());
        }

        this.addXp(1);
    }

    private ItemStack getRandomEnchantedBook() {
        List<Enchantment> enchantments = Registry.ENCHANTMENT.stream().toList();
        if (!enchantments.isEmpty()) {
            Enchantment randomEnch = enchantments.get(this.random.nextInt(enchantments.size()));
            int minLvl = randomEnch.getMinLevel();
            int maxLvl = randomEnch.getMaxLevel();
            int lvl = minLvl + this.random.nextInt(maxLvl - minLvl + 1);

            return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(randomEnch, lvl));
        }
        return new ItemStack(Items.BOOK);
    }

    public int getNeededXp() {
        return this.getTurretLevel();
    }

    public void addXp(int amount) {
        int nextXp = this.getXp() + amount;
        this.setXp(nextXp);

        if (nextXp >= getNeededXp()) {
            levelUp();
        }
    }

    private void levelUp() {
        this.setXp(0);
        int nextLevel = this.getTurretLevel() + 1;
        this.setTurretLevel(nextLevel);

        double newMaxHealth = 50.0D + (nextLevel - 1) * 10.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        this.setHealth((float) newMaxHealth);
    }

    private void spawnZombieWave() {
        int currentWave = this.getWaveCount();
        int spawnCount = 1 + (currentWave - 1) / 3;
        spawnCount = Math.min(spawnCount, 30);

        for (int i = 0; i < spawnCount; i++) {
            double angle = this.random.nextDouble() * 2 * Math.PI;
            double radius = 8.0D + this.random.nextDouble() * 4.0D;
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            double y = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) x, (int) z);

            Zombie zombie = EntityType.ZOMBIE.create(this.level);
            if (zombie != null) {
                zombie.moveTo(x, y, z, this.random.nextFloat() * 360F, 0.0F);
                this.applyProgressionEquipment(zombie, currentWave);
                this.level.addFreshEntity(zombie);
                zombie.setTarget(this);
            }
        }
        this.setWaveCount(currentWave + 1);
    }

    private void applyProgressionEquipment(Zombie zombie, int wave) {
        int lvl = this.getTurretLevel();

        if (lvl < 3) {
            return;
        }

        if (wave >= 3 && wave < 6) {
            zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
            zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        } else if (wave >= 6 && wave < 10) {
            zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        } else if (wave >= 10 && wave < 15) {
            // 풀 철 갑옷 세트 및 다이아몬드 검
            zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            zombie.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
            zombie.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
            zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        } else if (wave >= 15 && wave < 20) {
            // 일반 다이아몬드 갑옷 세트 및 다이아몬드 검
            zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
            zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
            zombie.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
            zombie.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
            zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        } else if (wave >= 20) {
            // 종결 스펙: 보호 IV 인챈트가 된 다이아몬드 갑옷 세트 + 날카로움 V 인챈트 다이아몬드 검
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
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("WaveCount", this.getWaveCount());
        tag.putInt("TurretLevel", this.getTurretLevel());
        tag.putInt("CurrentXp", this.getXp());
        tag.putInt("ResourceTimer", this.getResourceTimer());
        tag.putInt("ZombieTimer", this.zombieTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setWaveCount(tag.getInt("WaveCount"));
        this.setTurretLevel(tag.contains("TurretLevel") ? tag.getInt("TurretLevel") : 1);
        this.setXp(tag.contains("CurrentXp") ? tag.getInt("CurrentXp") : 0);
        if (tag.contains("ResourceTimer")) this.setResourceTimer(tag.getInt("ResourceTimer"));
        if (tag.contains("ZombieTimer")) this.zombieTimer = tag.getInt("ZombieTimer");

        double newMaxHealth = 50.0D + (this.getTurretLevel() - 1) * 10.0D;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
    }

    public int getWaveCount() { return this.entityData.get(DATA_WAVE_COUNT); }
    public void setWaveCount(int wave) { this.entityData.set(DATA_WAVE_COUNT, wave); }
    public int getTurretLevel() { return this.entityData.get(DATA_LEVEL); }
    public void setTurretLevel(int lvl) { this.entityData.set(DATA_LEVEL, lvl); }
    public int getXp() { return this.entityData.get(DATA_CURRENT_XP); }
    public void setXp(int xp) { this.entityData.set(DATA_CURRENT_XP, xp); }
    public int getResourceTimer() { return this.entityData.get(DATA_RESOURCE_TIMER); }
    public void setResourceTimer(int timer) { this.entityData.set(DATA_RESOURCE_TIMER, timer); }
}
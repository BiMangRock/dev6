package changmin.changmin_villager_turret.feature.turret.common;

import changmin.changmin_villager_turret.changmin_villager_turret;
import changmin.changmin_villager_turret.feature.turret.resource_easy.ResourceVillagerEasyEntity;
import changmin.changmin_villager_turret.feature.turret.resource_villager1.ResourceVillagerEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 🖐️ 터렛 상호작용 공통 처리 (모든 터렛에 적용)
 *
 * 1. 📦 터렛 회수: 웅크린 채 **빈손**으로 터렛을 우클릭하면 스폰알로 돌아옵니다.
 *    스폰알 안에 터렛의 저장 데이터(레벨·경험치·업그레이드·체력)가 그대로 들어가서,
 *    다시 설치하면 이전 상태 그대로 복원됩니다. (바닐라 스폰알의 EntityTag 기능 사용)
 *
 * 2. 🚫 업그레이드 인증서 재사용 차단:
 *    업그레이드를 사면 효과는 즉시 적용되고, 거래 결과물로 인증서 아이템이 손에 남습니다.
 *    예전에는 이 인증서로 터렛을 우클릭하면 같은 업그레이드가 한 번 더 들어가서 무한 강화가 가능했습니다.
 *    - 인증서로 터렛을 우클릭하면 적용하지 않고 안내만 합니다.
 *    - 인벤토리에 남은 인증서는 0.5초 안에 자동으로 사라집니다. (이미 적용된 영수증이므로)
 */
@Mod.EventBusSubscriber(modid = changmin_villager_turret.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TurretInteractionHandler {

    private static final int RECEIPT_CLEAN_INTERVAL = 10;

    /** 회수할 때 스폰알에 저장하지 않을 항목 (위치·이동·고유ID 등은 새로 설치할 때 새로 정해져야 함) */
    private static final List<String> STRIP_KEYS = List.of(
            "Pos", "Motion", "Rotation", "UUID", "FallDistance", "Fire", "Air", "OnGround",
            "PortalCooldown", "Passengers", "Leash", "Brain", "HurtTime", "HurtByTimestamp", "DeathTime");

    private TurretInteractionHandler() {}

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        Player player = event.getPlayer();
        ItemStack held = player.getItemInHand(event.getHand());

        // 🚫 인증서로 터렛 우클릭 → 재적용 차단
        if (target instanceof AbstractTurretEntity && TurretTradeUtil.isUpgradeReceipt(held)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(player.level.isClientSide));
            if (!player.level.isClientSide) {
                player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.receipt.already_applied")
                        .withStyle(ChatFormatting.RED), true);
            }
            return;
        }

        // 📦 웅크린 채 빈손 우클릭 → 회수
        if (!player.isShiftKeyDown() || event.getHand() != InteractionHand.MAIN_HAND || !held.isEmpty()) return;
        if (!isRecoverable(target) || !target.isAlive()) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(player.level.isClientSide));
        if (player.level.isClientSide) return;

        LivingEntity turret = (LivingEntity) target;

        // 다른 플레이어가 거래(업그레이드) 화면을 열고 있으면 회수 불가
        if (turret instanceof AbstractTurretEntity t && t.getTradingPlayer() != null && t.getTradingPlayer() != player) {
            player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.packed.busy")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        ItemStack egg = packTurret(turret);
        if (egg == null) return;

        if (!player.getInventory().add(egg)) {
            player.drop(egg, false);
        }

        if (turret.level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF, turret.getX(), turret.getY() + turret.getBbHeight() * 0.5D, turret.getZ(),
                    12, 0.3D, 0.4D, 0.3D, 0.02D);
        }
        turret.level.playSound(null, turret.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 0.8F);
        player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.packed.success",
                turret.getType().getDescription()).withStyle(ChatFormatting.GREEN), true);

        turret.discard();
    }

    private static boolean isRecoverable(Entity entity) {
        return entity instanceof AbstractTurretEntity
                || entity instanceof ResourceVillagerEntity
                || entity instanceof ResourceVillagerEasyEntity;
    }

    private static int levelOf(Entity entity) {
        if (entity instanceof AbstractTurretEntity t) return t.getTurretLevel();
        if (entity instanceof ResourceVillagerEntity r) return r.getTurretLevel();
        if (entity instanceof ResourceVillagerEasyEntity r) return r.getTurretLevel();
        return 1;
    }

    /** 터렛을 저장 데이터가 담긴 스폰알로 만듭니다. 스폰알이 없는 종류면 null */
    @Nullable
    public static ItemStack packTurret(LivingEntity turret) {
        EntityType<?> type = turret.getType();
        SpawnEggItem eggItem = ForgeSpawnEggItem.fromEntityType(type);
        if (eggItem == null) eggItem = SpawnEggItem.byId(type);
        if (eggItem == null) return null;

        CompoundTag data = turret.saveWithoutId(new CompoundTag());
        for (String key : STRIP_KEYS) {
            data.remove(key);
        }
        data.putString("id", String.valueOf(type.getRegistryName()));

        ItemStack stack = new ItemStack(eggItem);
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("EntityTag", data);

        // 이름은 바꾸지 않고(바꾸면 설치한 터렛에 이름이 붙음) 설명 줄로 레벨을 표시
        CompoundTag display = new CompoundTag();
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(
                TurretTradeUtil.tr("changmin_villager_turret.packed.lore_level", levelOf(turret))
                        .withStyle(style -> style.withColor(0xFFD700).withItalic(false)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(
                TurretTradeUtil.tr("changmin_villager_turret.packed.lore_keep")
                        .withStyle(style -> style.withColor(0xAAAAAA)))));
        display.put("Lore", lore);
        tag.put("display", display);
        return stack;
    }

    // 🧹 인벤토리에 남은 업그레이드 인증서 정리 (서버, 0.5초마다)
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.SERVER) return;
        Player player = event.player;
        if (player.tickCount % RECEIPT_CLEAN_INTERVAL != 0) return;

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (TurretTradeUtil.isUpgradeReceipt(inventory.getItem(i))) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        if (TurretTradeUtil.isUpgradeReceipt(player.containerMenu.getCarried())) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }
    }
}

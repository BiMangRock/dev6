package changmin.changmin_villager_turret.feature.hero.healer_hero;

import changmin.changmin_villager_turret.feature.turret.common.TurretTradeUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class GuardLinkerItem extends Item {
    public GuardLinkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (!level.isClientSide && player != null) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt("TargetX", pos.getX());
            tag.putInt("TargetY", pos.getY());
            tag.putInt("TargetZ", pos.getZ());
            tag.putBoolean("HasCoords", true);

            player.displayClientMessage(TurretTradeUtil.tr("changmin_villager_turret.text.misc.2", pos.getX(), pos.getY(), pos.getZ()), true);
            level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getBoolean("HasCoords")) {
            int x = tag.getInt("TargetX");
            int y = tag.getInt("TargetY");
            int z = tag.getInt("TargetZ");
            tooltip.add(TurretTradeUtil.tr("changmin_villager_turret.text.misc.3", x, y, z).withStyle(style -> style.withColor(0x55FF55)));
        } else {
            tooltip.add(TurretTradeUtil.tr("changmin_villager_turret.text.misc.4").withStyle(style -> style.withColor(0xAAAAAA)));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
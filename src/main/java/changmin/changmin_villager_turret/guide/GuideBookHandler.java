package changmin.changmin_villager_turret.guide;

import changmin.changmin_villager_turret.changmin_villager_turret;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 📖 가이드북: 처음 접속한 플레이어에게 한 번만 지급합니다.
 *
 * 바닐라 '완성된 책'을 사용하고, 각 페이지는 번역 키({"translate": "..."})로 적어서
 * 게임 언어에 따라 ko_kr.json / en_us.json 의 문구가 표시됩니다.
 * 페이지 내용을 고치려면 언어 파일의 guide.changmin_villager_turret.pageN 값을 수정하세요.
 * 페이지를 늘리려면 PAGE_COUNT를 올리고 언어 파일에 pageN 키를 추가하면 됩니다.
 */
@Mod.EventBusSubscriber(modid = changmin_villager_turret.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GuideBookHandler {

    public static final int PAGE_COUNT = 9;
    private static final String RECEIVED_KEY = "changmin_turret_guide_received";

    private GuideBookHandler() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        TurretAdvancements.award(player, TurretAdvancements.ROOT);

        CompoundTag persisted = TurretAdvancements.getPersisted(player);
        if (persisted.getBoolean(RECEIVED_KEY)) return;

        ItemStack book = createGuideBook();
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
        player.displayClientMessage(new TranslatableComponent("guide.changmin_villager_turret.received"), false);

        persisted.putBoolean(RECEIVED_KEY, true);
        TurretAdvancements.savePersisted(player, persisted);
    }

    public static ItemStack createGuideBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("title", "Villager Turret Guide");
        tag.putString("author", "Villager Turret");
        tag.putInt("generation", 0);
        tag.putBoolean("resolved", false);

        ListTag pages = new ListTag();
        for (int i = 1; i <= PAGE_COUNT; i++) {
            Component page = new TranslatableComponent("guide.changmin_villager_turret.page" + i);
            pages.add(StringTag.valueOf(Component.Serializer.toJson(page)));
        }
        tag.put("pages", pages);

        // 아이템 이름도 언어 파일을 따르도록 표시 이름을 번역 키로 지정
        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(
                new TranslatableComponent("item.changmin_villager_turret.guide_book")
                        .withStyle(style -> style.withItalic(false))));
        tag.put("display", display);
        return book;
    }
}

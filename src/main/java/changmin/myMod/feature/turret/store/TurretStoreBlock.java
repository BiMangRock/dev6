package changmin.myMod.feature.turret.store;

import changmin.myMod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.OptionalInt;

public class TurretStoreBlock extends Block {
    public TurretStoreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            ClientSideMerchant merchant = new ClientSideMerchant(player);
            MerchantOffers offers = new MerchantOffers();

            // 거래 1: 에메랄드 9개 ➔ 응축된 에메랄드 1개 (응축)
            offers.add(new MerchantOffer(
                    new ItemStack(Items.EMERALD, 9),
                    new ItemStack(ModItems.CONDENSED_EMERALD.get(), 1),
                    999, 2, 0.05F
            ));

            // 거래 2: 응축된 에메랄드 1개 ➔ 에메랄드 9개 (분해)
            offers.add(new MerchantOffer(
                    new ItemStack(ModItems.CONDENSED_EMERALD.get(), 1),
                    new ItemStack(Items.EMERALD, 9),
                    999, 2, 0.05F
            ));

            // 거래 3: 에메랄드 10개 ➔ 주민 터렛 스폰알 1개 (구매)
            offers.add(new MerchantOffer(
                    new ItemStack(Items.EMERALD, 10),
                    new ItemStack(ModItems.VILLAGER_TURRET_SPAWN_EGG.get(), 1),
                    999, 2, 0.05F
            ));

            // 🆕 [수정] 거래 4: 조약돌 16개 ➔ 자원 수확 주민 터렛 스폰알 1개 (구매)
            // 나무 원목 대신 조약돌(COBBLESTONE) 단일 아이템으로 지정하여 UI가 지저분해지는 현상을 해결했습니다.
            offers.add(new MerchantOffer(
                    new ItemStack(Items.COBBLESTONE, 3),
                    new ItemStack(ModItems.RESOURCE_VILLAGER_SPAWN_EGG.get(), 1),
                    999, 2, 0.05F
            ));


            // 🆕 [추가] 거래 5: 금 블록 1개 ➔ 치유 토템 터렛 스폰알 1개 (구매)
            offers.add(new MerchantOffer(
                    new ItemStack(Items.GOLD_BLOCK, 1),
                    new ItemStack(ModItems.HEALER_TURRET_SPAWN_EGG.get(), 1),
                    999, 2, 0.05F
            ));

            // 거래 5: 하급 토큰 9개 ➔ 중급 토큰 1개 (환전)
            offers.add(new MerchantOffer(
                    new ItemStack(ModItems.TURRET_POINT_TOKEN_LOW.get(), 9),
                    new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 1),
                    999, 2, 0.05F
            ));

            // 거래 6: 중급 토큰 9개 ➔ 상급 토큰 1개 (환전)
            offers.add(new MerchantOffer(
                    new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 9),
                    new ItemStack(ModItems.TURRET_POINT_TOKEN_HIGH.get(), 1),
                    999, 2, 0.05F
            ));

            merchant.overrideOffers(offers);

            OptionalInt containerId = player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new MerchantMenu(id, inv, merchant),
                    new TranslatableComponent("container.turret_store")
            ));

            if (containerId.isPresent()) {
                player.sendMerchantOffers(
                        containerId.getAsInt(),
                        offers,
                        1,
                        0,
                        false,
                        false
                );
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
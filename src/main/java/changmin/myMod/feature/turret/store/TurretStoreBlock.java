package changmin.myMod.feature.turret.store;

import changmin.myMod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.OptionalInt; // 🆕 화면 ID 추적을 위해 임포트 추가

public class TurretStoreBlock extends Block {
    public TurretStoreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            // 가상 임시 상인 객체 생성
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

            // 거래 4: 하급 토큰 9개 ➔ 중급 토큰 1개 (환전)
            offers.add(new MerchantOffer(
                    new ItemStack(ModItems.TURRET_POINT_TOKEN_LOW.get(), 9),
                    new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 1),
                    999, 2, 0.05F
            ));

            // 거래 5: 중급 토큰 9개 ➔ 상급 토큰 1개 (환전)
            offers.add(new MerchantOffer(
                    new ItemStack(ModItems.TURRET_POINT_TOKEN_MID.get(), 9),
                    new ItemStack(ModItems.TURRET_POINT_TOKEN_HIGH.get(), 1),
                    999, 2, 0.05F
            ));

            merchant.overrideOffers(offers);

            // 💡 [자료] 화면을 열면서 반환되는 고유 화면 번호(Container ID)를 가져옵니다.
            OptionalInt containerId = player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new MerchantMenu(id, inv, merchant),
                    new TranslatableComponent("container.turret_store")
            ));

            // 💡 [자료] 화면이 정상적으로 열렸다면, 서버에서 클라이언트로 거래 목록 패킷을 수동으로 전송해 줍니다.
            if (containerId.isPresent()) {
                player.sendMerchantOffers(
                        containerId.getAsInt(), // 화면 ID
                        offers,                 // 전송할 거래 목록
                        1,                      // 가상 주민 레벨
                        0,                      // 가상 주민 경험치
                        false,                  // 레벨업 진행도 바 표시 여부
                        false                   // 거래 품목 재고 자동 충전 활성화 여부
                );
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
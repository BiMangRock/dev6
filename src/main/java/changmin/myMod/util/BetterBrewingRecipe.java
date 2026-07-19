package changmin.myMod.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.brewing.IBrewingRecipe;

public class BetterBrewingRecipe implements IBrewingRecipe {
    private final Potion input;
    private final net.minecraft.world.item.Item ingredient;
    private final Potion output;

    public BetterBrewingRecipe(Potion input, net.minecraft.world.item.Item ingredient, Potion output) {
        this.input = input;
        this.ingredient = ingredient;
        this.output = output;
    }

    // 1. 넣은 물약이 우리가 설정한 조합 재료용 물약(예: 어색한 물약)이 맞는지 검사
    @Override
    public boolean isInput(ItemStack input) {
        return PotionUtils.getPotion(input) == this.input;
    }

    // 2. 가마솥/양조기 위에 올려놓은 재료 아이템(예: 레드스톤)이 맞는지 검사
    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return ingredient.getItem() == this.ingredient;
    }

    // 3. 조건이 맞다면 물약 내용물 데이터(NBT)만 분노 포션으로 치환하여 출력
    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if(!isInput(input) || !isIngredient(ingredient)) {
            return ItemStack.EMPTY;
        }

        ItemStack mockOutput = input.copy();
        PotionUtils.setPotion(mockOutput, this.output);
        return mockOutput;
    }
}
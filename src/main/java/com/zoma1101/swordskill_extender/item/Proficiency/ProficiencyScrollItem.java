package com.zoma1101.swordskill_extender.item.Proficiency;

import net.minecraft.ChatFormatting; // ★追加
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProficiencyScrollItem extends Item {
    public ProficiencyScrollItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        // 翻訳キー: tooltip.swordskill_extender.proficiency_scroll.usage
        pTooltipComponents.add(Component.translatable("tooltip.swordskill_extender.proficiency_scroll.usage").withStyle(ChatFormatting.GRAY));

        // NBTから上昇量を取得して表示 (デフォルト100)
        int xpAmount = 100;
        if (pStack.hasTag() && pStack.getTag().contains("xp_amount")) {
            xpAmount = pStack.getTag().getInt("xp_amount");
        }

        // 翻訳キー: tooltip.swordskill_extender.proficiency_scroll.amount
        // 引数として xpAmount を渡すことで、言語ファイル側で "%s" の部分に数値を埋め込めます
        pTooltipComponents.add(Component.translatable("tooltip.swordskill_extender.proficiency_scroll.amount", xpAmount).withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
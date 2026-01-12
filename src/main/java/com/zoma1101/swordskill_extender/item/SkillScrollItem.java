package com.zoma1101.swordskill_extender.item;

import com.zoma1101.swordskill.capability.PlayerSkillsProvider;
import com.zoma1101.swordskill.swordskills.SkillData;
import com.zoma1101.swordskill.swordskills.SwordSkillRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SkillScrollItem extends Item {
    public SkillScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            CompoundTag tag = stack.getTag();
            // NBTにSkillIDが含まれているかチェック
            if (tag == null || !tag.contains("SkillID")) {
                player.sendSystemMessage(Component.translatable("message.swordskill_extender.scroll_invalid").withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            int skillId = tag.getInt("SkillID");
            SkillData skill = SwordSkillRegistry.SKILLS.get(skillId);

            // スキルが存在するかチェック
            if (skill == null) {
                player.sendSystemMessage(Component.translatable("message.swordskill_extender.skill_not_found").withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            // ★修正: スキル名を翻訳キーから取得するように変更
            Component skillName = Component.translatable("swordskill.skill." + skill.getName());

            // Capabilityにアクセスしてスキルを解放
            player.getCapability(PlayerSkillsProvider.PLAYER_SKILLS).ifPresent(skills -> {
                if (skills.isSkillUnlocked(skillId)) {
                    player.sendSystemMessage(Component.translatable("message.swordskill_extender.skill_already_unlocked", skillName).withStyle(ChatFormatting.YELLOW));
                } else {
                    skills.unlockSkill(skillId);
                    // 必要に応じて同期処理 (skills.sync(player) 等)

                    player.sendSystemMessage(Component.translatable("message.swordskill_extender.skill_unlocked", skillName).withStyle(ChatFormatting.GREEN));
                    level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);

                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                }
            });
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("SkillID")) {
            int skillId = tag.getInt("SkillID");
            SkillData skill = SwordSkillRegistry.SKILLS.get(skillId);
            if (skill != null) {
                Component skillName = Component.translatable("swordskill.skill." + skill.getName());
                tooltipComponents.add(Component.translatable("tooltip.swordskill_extender.skill_scroll_skill", skillName).withStyle(ChatFormatting.GOLD));
            } else {
                tooltipComponents.add(Component.translatable("tooltip.swordskill_extender.skill_not_found").withStyle(ChatFormatting.RED));
            }
        } else {
            tooltipComponents.add(Component.translatable("tooltip.swordskill_extender.skill_scroll_empty").withStyle(ChatFormatting.GRAY));
        }
    }
}
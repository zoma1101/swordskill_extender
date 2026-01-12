package com.zoma1101.swordskill_extender.item.weapon;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class BattleaxeItem extends SwordItem {
    public BattleaxeItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    // 斧はスイープ攻撃しない
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        if (toolAction == ToolActions.SWORD_SWEEP) {
            return false;
        }
        return super.canPerformAction(stack, toolAction);
    }

    // 攻撃時: 防御貫通ダメージを追加
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide && entity instanceof LivingEntity target) {

            // クールダウンが溜まっている時のみ発動
            if (player.getAttackStrengthScale(0.5F) > 0.9F) {
                float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);

                // 攻撃力の40%分を「防御無視ダメージ (GENERIC)」として追加で与える
                // これにより、実質的に防御を部分的に無効化してダメージを通すことができる
                float penetrationDamage = baseDamage * 0.4F;

                // 無敵時間をリセットして即座にダメージを入れる
                target.invulnerableTime = 0;
                target.hurt(player.damageSources().generic(), penetrationDamage);

                // 重い打撃音
                player.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.PLAYERS, 0.5F, 0.5F);
            }
        }

        // falseを返すことで、バニラの通常攻撃（物理ダメージ）もそのまま実行させる
        // 結果として「物理ダメージ + 防御無視ダメージ」の複合ダメージとなる
        return super.onLeftClickEntity(stack, player, entity);
    }
}
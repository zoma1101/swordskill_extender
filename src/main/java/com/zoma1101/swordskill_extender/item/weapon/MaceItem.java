package com.zoma1101.swordskill_extender.item.weapon;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class MaceItem extends SwordItem {
    public MaceItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    // メイスは範囲攻撃（スイープ）しないように設定
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        if (toolAction == ToolActions.SWORD_SWEEP) {
            return false;
        }
        return super.canPerformAction(stack, toolAction);
    }

    // 攻撃命中時の処理
    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        if (pAttacker instanceof Player player) {
            // プレイヤーの落下距離を取得
            float fallDistance = player.fallDistance;

            // 落下中（1.5ブロック以上）で、水中でなく、ハシゴ等にも登っていない場合
            if (fallDistance > 1.5F && !player.onGround() && !player.onClimbable() && !player.isInWater()) {

                // ボーナスダメージ計算: 落下ブロック数 × 3.0
                // (例: 5ブロック落下で +15ダメージ)
                float bonusDamage = fallDistance * 3.0F;

                // 通常攻撃の直後で発生する無敵時間をリセットして、即座に追加ダメージを与える
                pTarget.invulnerableTime = 0;
                pTarget.hurt(player.damageSources().playerAttack(player), bonusDamage);

                // 成功報酬としてプレイヤーの落下距離をリセット（着地時の落下ダメージを無効化）
                player.fallDistance = 0;

                // 重厚な打撃音を再生 (金床の着地音をピッチ下げて使用)
                pTarget.level().playSound(null, pTarget.getX(), pTarget.getY(), pTarget.getZ(),
                        SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.5F, 0.5F);

                // ノックバックを少し強化
                pTarget.knockback(0.4, player.getX() - pTarget.getX(), player.getZ() - pTarget.getZ());
            }
        }
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }
}
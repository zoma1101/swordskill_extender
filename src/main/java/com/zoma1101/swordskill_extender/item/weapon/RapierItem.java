package com.zoma1101.swordskill_extender.item.weapon;

import net.minecraft.core.particles.ParticleTypes; // 追加
import net.minecraft.server.level.ServerLevel;     // 追加
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class RapierItem extends SwordItem {

    private static final float CRIT_CHANCE = 0.1F;

    public RapierItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    // 凪払い（スイープ）無効化
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        if (toolAction == ToolActions.SWORD_SWEEP) {
            return false;
        }
        return super.canPerformAction(stack, toolAction);
    }

    // 攻撃時の処理 (左クリック)
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        // サーバー側でのみ判定を行う
        if (!player.level().isClientSide && entity instanceof LivingEntity target) {

            // クールダウンがしっかり溜まっている時のみクリティカル判定を行う
            if (player.getAttackStrengthScale(0.5F) > 0.9F) {

                // 10%の確率でクリティカル発動
                if (player.getRandom().nextFloat() < CRIT_CHANCE) {

                    float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    float multiplier = 5.0F; // 通常モブは5倍

                    // BOSSタグを持つモブの場合は2倍
                    if (target.getType().is(Tags.EntityTypes.BOSSES)) {
                        multiplier = 2.0F;
                    }

                    float finalDamage = baseDamage * multiplier;
                    target.hurt(player.damageSources().playerAttack(player), finalDamage);

                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.END_ROD,
                                target.getX(), target.getY(), target.getZ(),
                                100, 0.0, 0.0, 0.0, 0.2);
                    }

                    player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.ANVIL_LAND, SoundSource.MASTER, 1.0F, 1.87F);

                    // 耐久値を消費
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));

                    // 攻撃後のクールダウンをリセット
                    player.resetAttackStrengthTicker();

                    return true;
                }
            }
        }

        // クリティカルが出なかった場合は false を返し、バニラの通常攻撃を行わせる
        return super.onLeftClickEntity(stack, player, entity);
    }
}
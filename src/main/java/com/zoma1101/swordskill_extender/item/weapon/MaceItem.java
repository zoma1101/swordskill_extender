package com.zoma1101.swordskill_extender.item.weapon;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public class MaceItem extends SwordItem {
    public MaceItem(Tier tier, int damage, float speed, Properties properties) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, damage, speed)));
    }

    // メイスは範囲攻撃（スイープ）しないように設定
    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        if (itemAbility == ItemAbilities.SWORD_SWEEP) {
            return false;
        }
        return super.canPerformAction(stack, itemAbility);
    }

    // 攻撃命中時の処理
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            float fallDistance = player.fallDistance;

            if (fallDistance > 1.5F && !player.onGround() && !player.onClimbable() && !player.isInWater()) {
                float bonusDamage = fallDistance * 3.0F;

                target.invulnerableTime = 0;
                target.hurt(player.damageSources().playerAttack(player), bonusDamage);
                player.fallDistance = 0;

                target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.5F, 0.5F);
                target.knockback(0.4, player.getX() - target.getX(), player.getZ() - target.getZ());
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}

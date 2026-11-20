package com.zoma1101.swordskill_extender.item.weapon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.Tags;

public class RapierItem extends SwordItem {
    private static final float CRIT_CHANCE = 0.1F;

    public RapierItem(Tier tier, int damage, float speed, Properties properties) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, damage, speed)));
    }

    // 凪払い（スイープ）無効化
    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        if (itemAbility == ItemAbilities.SWORD_SWEEP) {
            return false;
        }
        return super.canPerformAction(stack, itemAbility);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide && entity instanceof LivingEntity target) {
            if (player.getAttackStrengthScale(0.5F) > 0.9F) {
                if (player.getRandom().nextFloat() < CRIT_CHANCE) {
                    float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    float multiplier = target.getType().is(Tags.EntityTypes.BOSSES) ? 2.0F : 5.0F;
                    float finalDamage = baseDamage * multiplier;

                    target.hurt(player.damageSources().playerAttack(player), finalDamage);

                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.END_ROD,
                                target.getX(), target.getY(), target.getZ(), 100, 0.0, 0.0, 0.0, 0.2);
                    }

                    player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.ANVIL_LAND, SoundSource.MASTER, 1.0F, 1.87F);

                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                    player.resetAttackStrengthTicker();
                    return true;
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}

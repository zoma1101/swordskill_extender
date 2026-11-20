package com.zoma1101.swordskill_extender.item.weapon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class KatanaItem extends SwordItem {
    public KatanaItem(Tier tier, int damage, float speed, Properties properties) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, damage, speed)));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int useDuration = this.getUseDuration(stack, entity) - timeLeft;
            if (useDuration >= 20) {
                performIaiSlash(player, level, stack, useDuration / 20.0F);
            }
        }
    }

    private void performIaiSlash(Player player, Level level, ItemStack stack, float chargeSeconds) {
        Vec3 look = player.getLookAngle();
        double dashDistance = 5.0;
        Vec3 dashVec = new Vec3(look.x, look.y * 0.5, look.z).normalize().scale(dashDistance);

        Vec3 startPos = player.position();
        Vec3 endPos = startPos.add(dashVec);

        player.push(dashVec.x, 0.1, dashVec.z);
        player.hurtMarked = true;

        if (!level.isClientSide) {
            AABB slashBox = new AABB(startPos, endPos).inflate(1.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, slashBox);

            float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float damage = baseDamage * (chargeSeconds * 0.5F + 1.0F);

            for (LivingEntity target : targets) {
                if (target != player && player.hasLineOfSight(target) && target.isAlive() && !player.isAlliedTo(target)) {
                    target.hurt(player.damageSources().playerAttack(player), damage);
                    target.push(0, 0.2, 0);
                }
            }

            stack.hurtAndBreak(2 + (int) (chargeSeconds * 0.25F), player, EquipmentSlot.MAINHAND);
            player.getCooldowns().addCooldown(this, 40);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS, 1.0F, 1.5F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 2.0F);

        if (level.isClientSide) {
            for (int i = 0; i < 10; i++) {
                double d = i / 10.0;
                double px = startPos.x + (endPos.x - startPos.x) * d;
                double py = startPos.y + (endPos.y - startPos.y) * d + 1.0;
                double pz = startPos.z + (endPos.z - startPos.z) * d;
                level.addParticle(ParticleTypes.SWEEP_ATTACK, px, py, pz, 0, 0, 0);
            }
        }
    }
}

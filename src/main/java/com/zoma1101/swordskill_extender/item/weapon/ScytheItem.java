package com.zoma1101.swordskill_extender.item.weapon;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ScytheItem extends SwordItem {
    private static final ResourceLocation REACH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "scythe_reach_increase");

    private final Tier tier;
    private final int damage;
    private final float speed;

    public ScytheItem(Tier tier, int damage, float speed, Properties properties) {
        super(tier, properties);
        this.tier = tier;
        this.damage = damage;
        this.speed = speed;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return SwordItem.createAttributes(tier, damage, speed)
                .withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(REACH_MODIFIER_ID, 1.0, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
    }

    // 右クリック: ソウルプル (敵を引き寄せる)
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            Vec3 look = player.getLookAngle();
            Vec3 center = player.position().add(look.scale(2.5));
            AABB pullArea = AABB.ofSize(center, 5.0, 3.0, 5.0);

            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, pullArea);
            for (LivingEntity target : targets) {
                if (target != player && !player.isAlliedTo(target)) {
                    Vec3 pullVec = player.position().subtract(target.position()).normalize().scale(0.8);
                    target.push(pullVec.x, 0.3, pullVec.z);
                    target.hurtMarked = true;
                }
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 1.0F, 0.5F);
            player.getCooldowns().addCooldown(this, 30);
        }

        player.swing(hand, true);
        return InteractionResultHolder.success(itemstack);
    }

    // 攻撃時: スイートスポット判定
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide && entity instanceof LivingEntity target) {
            float distance = player.distanceTo(target);
            float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float finalDamage;

            if (distance >= 2.5F) {
                finalDamage = baseDamage * 1.5F;
                player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.2F);
            } else {
                finalDamage = baseDamage * 0.5F;
                player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.WOOD_HIT, SoundSource.PLAYERS, 1.0F, 0.8F);
            }

            target.hurt(player.damageSources().playerAttack(player), finalDamage);
            player.resetAttackStrengthTicker();
            stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            return true;
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}

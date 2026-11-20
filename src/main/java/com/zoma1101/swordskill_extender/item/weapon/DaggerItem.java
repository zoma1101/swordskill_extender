package com.zoma1101.swordskill_extender.item.weapon;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.List;

public class DaggerItem extends SwordItem {
    private static final ResourceLocation REACH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "dagger_reach_reduction");
    private static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "dagger_speed_increase");

    private final Tier tier;
    private final int damage;
    private final float speed;

    public DaggerItem(Tier tier, int damage, float speed, Properties properties) {
        super(tier, properties);
        this.tier = tier;
        this.damage = damage;
        this.speed = speed;
    }

    // バニラのスイープ攻撃を無効化
    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        if (itemAbility == ItemAbilities.SWORD_SWEEP) {
            return false;
        }
        return super.canPerformAction(stack, itemAbility);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return SwordItem.createAttributes(tier, damage, speed)
                .withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(REACH_MODIFIER_ID, -1.0, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .withModifierAdded(Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(SPEED_MODIFIER_ID, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        EquipmentSlotGroup.MAINHAND);
    }

    // 武器を振った時の処理 (投擲物の反射)
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            Vec3 look = player.getLookAngle();
            double range = 2.5;

            Vec3 eyePos = player.getEyePosition();
            Vec3 targetPos = eyePos.add(look.scale(range));
            AABB aabb = new AABB(eyePos, targetPos).inflate(1.0);

            List<Projectile> projectiles = player.level().getEntitiesOfClass(Projectile.class, aabb);
            for (Projectile projectile : projectiles) {
                Vec3 toProjectile = projectile.position().subtract(eyePos).normalize();
                if (projectile.getOwner() != player && look.dot(toProjectile) > 0.5) {
                    double projectileSpeed = projectile.getDeltaMovement().length();
                    if (projectileSpeed < 0.1) projectileSpeed = 1.0;

                    projectile.shoot(look.x, look.y, look.z, (float) projectileSpeed * 1.5F, 0.0F);
                    projectile.setOwner(player);

                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.0F, 2.0F);
                }
            }
        }
        return super.onEntitySwing(stack, entity, hand);
    }

    // 左クリック（攻撃）時の処理
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide && entity instanceof LivingEntity target) {
            if (player.getAttackStrengthScale(0.5F) > 0.9F) {
                float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double reach = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) * 2.0;

                AABB aabb = player.getBoundingBox().inflate(reach, 1.0, reach);
                List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(LivingEntity.class, aabb);

                for (LivingEntity nearby : nearbyEntities) {
                    if (nearby != player && nearby != target && !player.isAlliedTo(nearby)) {
                        if (player.distanceTo(nearby) <= reach) {
                            nearby.hurt(player.damageSources().playerAttack(player), baseDamage * 0.75F);
                        }
                    }
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}

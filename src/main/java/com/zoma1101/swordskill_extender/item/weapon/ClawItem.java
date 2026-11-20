package com.zoma1101.swordskill_extender.item.weapon;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.List;

public class ClawItem extends SwordItem {
    private static final ResourceLocation REACH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "claw_reach_reduction");

    private final Tier tier;
    private final int damage;
    private final float speed;

    public ClawItem(Tier tier, int damage, float speed, Properties properties) {
        super(tier, properties);
        this.tier = tier;
        this.damage = damage;
        this.speed = speed;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return SwordItem.createAttributes(tier, damage, speed)
                .withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(REACH_MODIFIER_ID, -1.25, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
    }

    // 爪は範囲攻撃（スイープ）しない
    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        if (itemAbility == ItemAbilities.SWORD_SWEEP) {
            return false;
        }
        return super.canPerformAction(stack, itemAbility);
    }

    // 右クリック: シャドウステップ (前方への高速ダッシュ + 無敵 + ターゲット解除)
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        Vec3 look = player.getLookAngle();
        player.push(look.x * 1.5, look.y * 1.5, look.z * 1.5);
        player.hurtMarked = true;

        player.resetFallDistance();
        player.invulnerableTime = 20;

        if (!level.isClientSide) {
            List<Mob> mobs = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(10.0));
            for (Mob mob : mobs) {
                if (mob.getTarget() == player) {
                    mob.setTarget(null);
                }
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 2.0F);
        player.getCooldowns().addCooldown(this, 20);

        return InteractionResultHolder.success(itemstack);
    }

    // 攻撃時: バックスタブ判定
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide && entity instanceof LivingEntity target) {
            if (player.getAttackStrengthScale(0.5F) > 0.9F) {
                Vec3 playerLook = player.getLookAngle();
                Vec3 targetLook = target.getLookAngle();

                if (playerLook.dot(targetLook) > 0.6) {
                    float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    float bonusDamage = (baseDamage * 5.0F) + 10.0F;

                    target.hurt(player.damageSources().playerAttack(player), bonusDamage);
                    player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.5F);

                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.5), target.getZ(),
                                5, 0.2, 0.2, 0.2, 0.0);
                    }

                    player.resetAttackStrengthTicker();
                    stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                    return true;
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}

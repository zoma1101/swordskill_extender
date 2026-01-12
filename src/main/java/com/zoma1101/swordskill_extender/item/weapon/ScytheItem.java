package com.zoma1101.swordskill_extender.item.weapon;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import java.util.List;
import java.util.UUID;

public class ScytheItem extends SwordItem {
    // 射程距離増加用のUUID
    private static final UUID REACH_MODIFIER_UUID = UUID.fromString("c91b0028-1234-4567-8901-234567890456");

    public ScytheItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    // ★追加: 射程を +1.0 する
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create(super.getAttributeModifiers(slot, stack));
        if (slot == EquipmentSlot.MAINHAND) {
            modifiers.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(REACH_MODIFIER_UUID, "Weapon reach increase", 1.0, AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }

    // 右クリック: ソウルプル (敵を引き寄せる)
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (!pLevel.isClientSide) {
            // 前方5ブロック、幅3ブロックの範囲
            Vec3 look = pPlayer.getLookAngle();
            Vec3 center = pPlayer.position().add(look.scale(2.5));
            AABB pullArea = AABB.ofSize(center, 5.0, 3.0, 5.0);

            List<LivingEntity> targets = pLevel.getEntitiesOfClass(LivingEntity.class, pullArea);

            for (LivingEntity target : targets) {
                if (target != pPlayer && !pPlayer.isAlliedTo(target)) {
                    // プレイヤーに向かうベクトル
                    Vec3 pullVec = pPlayer.position().subtract(target.position()).normalize().scale(0.8);
                    // 引き寄せる (Y軸は少し浮かせる)
                    target.push(pullVec.x, 0.3, pullVec.z);
                    target.hurtMarked = true;
                }
            }

            // 音
            pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 1.0F, 0.5F);

            // クールタイム
            pPlayer.getCooldowns().addCooldown(this, 30);
        }

        pPlayer.swing(pHand, true);
        return InteractionResultHolder.success(itemstack);
    }

    // 攻撃時: スイートスポット判定
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide && entity instanceof LivingEntity target) {
            float distance = player.distanceTo(target);
            float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float finalDamage;

            // 2.5ブロック以上離れていれば「刃先」ヒット (1.5倍)
            if (distance >= 2.5F) {
                finalDamage = baseDamage * 1.5F;
                player.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.2F);
            }
            // 近すぎると「柄」ヒット (0.5倍)
            else {
                finalDamage = baseDamage * 0.5F;
                player.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WOOD_HIT, SoundSource.PLAYERS, 1.0F, 0.8F);
            }

            target.hurt(player.damageSources().playerAttack(player), finalDamage);

            // 通常攻撃キャンセル
            player.resetAttackStrengthTicker();
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            return true;
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}
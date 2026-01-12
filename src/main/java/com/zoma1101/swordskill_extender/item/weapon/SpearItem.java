package com.zoma1101.swordskill_extender.item.weapon;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.List;
import java.util.UUID;

public class SpearItem extends SwordItem {
    // 射程距離増加用のUUID
    private static final UUID REACH_MODIFIER_UUID = UUID.fromString("c91b0028-1234-4567-8901-234567890123");

    public SpearItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    // 凪払い（スイープ攻撃）を無効化
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        if (toolAction == ToolActions.SWORD_SWEEP) {
            return false;
        }
        return super.canPerformAction(stack, toolAction);
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

    // 右クリック: 突き（ダッシュ攻撃）
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand pHand) {
        ItemStack itemstack = player.getItemInHand(pHand);

        // 1. ダッシュ処理 (クライアント・サーバー両方で実行して動きを滑らかにする)
        Vec3 look = player.getLookAngle();
        double dashSpeed = 1.2; // ダッシュの強さ
        player.hurtMarked = true;
        player.push(look.x * dashSpeed, 0.2, look.z * dashSpeed);

        // 2. ダメージ判定処理 (サーバー側のみ)
        if (!pLevel.isClientSide) {
            // 現在の運動量（勢い）を取得
            // pushした直後なので、ダッシュ分の速度が加算されています
            double momentum = player.getDeltaMovement().length();

            // 攻撃範囲の計算 (前方3ブロック、幅1ブロック程度の範囲)
            Vec3 reachVec = look.scale(3.0); // 突進攻撃の判定リーチ
            AABB attackBox = player.getBoundingBox().expandTowards(reachVec).inflate(1.0);

            // 範囲内の敵を取得
            List<LivingEntity> entities = pLevel.getEntitiesOfClass(LivingEntity.class, attackBox,
                    e -> e != player && e.isAlive() && player.hasLineOfSight(e));

            // プレイヤーの基本攻撃力を取得
            float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);

            for (LivingEntity target : entities) {
                // 勢い(Motion)に応じてダメージ倍率を計算 (基本1.0 + 速度分)
                float damage = baseDamage * (float) (1.0 + momentum);

                // ダメージを与える
                target.hurt(player.damageSources().playerAttack(player), damage);

                // ノックバックを与える
                target.knockback(0.5, player.getX() - target.getX(), player.getZ() - target.getZ());
            }

            // クールタイム設定 (40tick = 2秒)
            player.getCooldowns().addCooldown(this, 40);

            // 耐久値を消費
            itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(pHand));
        }

        // 手を振るアニメーション
        player.swing(pHand, true);

        return InteractionResultHolder.success(itemstack);
    }
}
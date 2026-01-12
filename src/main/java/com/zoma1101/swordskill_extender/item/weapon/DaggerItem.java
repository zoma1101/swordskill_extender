package com.zoma1101.swordskill_extender.item.weapon;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.sounds.SoundEvents; // 追加
import net.minecraft.sounds.SoundSource; // 追加
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile; // 追加
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3; // 追加
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.List;
import java.util.UUID;

public class DaggerItem extends SwordItem {
    // 射程距離短縮用のUUID
    private static final UUID REACH_MODIFIER_UUID = UUID.fromString("7f03e228-1234-4567-8901-234567890abc");
    // 移動速度上昇用のUUID
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("c91b0028-1234-4567-8901-234567890def");

    public DaggerItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    // バニラのスイープ攻撃を無効化
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        if (toolAction == ToolActions.SWORD_SWEEP) {
            return false;
        }
        return super.canPerformAction(stack, toolAction);
    }

    // ★追加: 武器を振った時の処理 (投擲物の反射)
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        // サーバー側かつプレイヤーの場合のみ処理
        if (!entity.level().isClientSide && entity instanceof Player player) {

            // 視線方向と反射判定の範囲 (ダガーなので短めの 2.5 ブロック)
            Vec3 look = player.getLookAngle();
            double range = 2.5;

            Vec3 eyePos = player.getEyePosition();
            Vec3 targetPos = eyePos.add(look.scale(range));

            // プレイヤーの前方に検索範囲ボックスを作成
            AABB aabb = new AABB(eyePos, targetPos).inflate(1.0);

            // 範囲内の Projectile (矢、火の玉など) を取得
            List<Projectile> projectiles = player.level().getEntitiesOfClass(Projectile.class, aabb);

            for (Projectile projectile : projectiles) {
                // 1. 自分が投げたものではない
                // 2. 視線方向にある程度合っている (内積で判定)
                Vec3 toProjectile = projectile.position().subtract(eyePos).normalize();
                if (projectile.getOwner() != player && look.dot(toProjectile) > 0.5) {

                    // 現在の速度を取得
                    double speed = projectile.getDeltaMovement().length();
                    if (speed < 0.1) speed = 1.0; // 止まっているものも飛ばせるように

                    // プレイヤーが向いている方向に、1.5倍速で反射させる
                    projectile.shoot(look.x, look.y, look.z, (float)speed * 1.5F, 0.0F);

                    // 所有者をプレイヤーに書き換え (プレイヤーの攻撃扱いにする)
                    projectile.setOwner(player);

                    // 反射音 (トライデントが何かに当たった音を使用)
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.0F, 2.0F);
                }
            }
        }
        return super.onEntitySwing(stack, entity);
    }

    // 属性（攻撃力・攻撃速度・射程・移動速度）の設定
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create(super.getAttributeModifiers(slot, stack));

        if (slot == EquipmentSlot.MAINHAND) {
            // 射程距離を -2.0 する
            modifiers.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(REACH_MODIFIER_UUID, "Weapon reach reduction", -1.0, AttributeModifier.Operation.ADDITION));

            // 移動速度を 20% 上昇させる
            modifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(SPEED_MODIFIER_UUID, "Weapon speed increase", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        return modifiers;
    }

    // 左クリック（攻撃）時の処理
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        // サーバー側かつ、攻撃対象がLivingEntityの場合
        if (!player.level().isClientSide && entity instanceof LivingEntity target) {

            // クールダウンが溜まりきっている場合のみ範囲攻撃を発動
            if (player.getAttackStrengthScale(0.5F) > 0.9F) {

                float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double reach = player.getAttributeValue(ForgeMod.ENTITY_REACH.get()) * 2.0;

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
package com.zoma1101.swordskill_extender.item.weapon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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
    public KatanaItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    // 右クリックのアニメーションを「弓引き（BOW）」にして、納刀して構えているっぽく見せる
    // （本来はカスタムアニメーションが良いですが、バニラならこれが近いです）
    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(itemstack);
    }

    // 使用を中止（右クリックを離した）した時の処理
    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeLeft) {
        if (pLivingEntity instanceof Player player) {
            // 使用していた時間 (tick)
            int useDuration = this.getUseDuration(pStack) - pTimeLeft;

            // 20tick (1秒) 以上溜めた場合のみ発動
            if (useDuration >= 20) {
                float chargeSeconds = useDuration / 20.0F;
                performIaiSlash(player, pLevel, pStack, chargeSeconds);
            }
        }
    }

    private void performIaiSlash(Player player, Level level, ItemStack stack, float chargeSeconds) {
        // 1. 移動ベクトル計算
        Vec3 look = player.getLookAngle();
        // 水平方向の移動を重視するため、Y成分を少し抑える
        double dashDistance = 5.0; // 5ブロック移動
        Vec3 dashVec = new Vec3(look.x, look.y * 0.5, look.z).normalize().scale(dashDistance);

        // 2. 始点と終点
        Vec3 startPos = player.position();
        Vec3 endPos = startPos.add(dashVec);

        // 3. プレイヤーを移動させる（テレポートに近い高速移動）
        // クライアント側の補間が効くように push を使うか、直接 teleportTo を使うか
        // ここでは勢いよく飛び出す push を採用
        player.push(dashVec.x, 0.1, dashVec.z);
        player.hurtMarked = true; // 更新通知

        // 4. 軌道上の敵への攻撃判定
        if (!level.isClientSide) {
            // 移動経路を含むボックスを作成
            AABB slashBox = new AABB(startPos, endPos).inflate(1.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, slashBox);

            float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            // 居合ボーナスダメージ (1.5倍)
            float damage = baseDamage * (chargeSeconds * 0.5F + 1.0F);

            for (LivingEntity target : targets) {
                if (target != player && player.hasLineOfSight(target) && target.isAlive()) {
                    // プレイヤーと味方以外を攻撃
                    if (!player.isAlliedTo(target)) {
                        target.hurt(player.damageSources().playerAttack(player), damage);
                        // 演出: 少し浮かす
                        target.push(0, 0.2, 0);
                    }
                }
            }

            // 耐久値消費
            stack.hurtAndBreak(2 + (int) (chargeSeconds * 0.25f), player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));

            // クールタイム (2秒)
            player.getCooldowns().addCooldown(this, 40);
        }

        // 5. 音とパーティクルの演出
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS, 1.0F, 1.5F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 2.0F);

        // 軌道上にパーティクルを出す
        if (level.isClientSide) {
            for (int i = 0; i < 10; i++) {
                double d = i / 10.0;
                double px = startPos.x + (endPos.x - startPos.x) * d;
                double py = startPos.y + (endPos.y - startPos.y) * d + 1.0; // 胴体の高さ
                double pz = startPos.z + (endPos.z - startPos.z) * d;
                level.addParticle(ParticleTypes.SWEEP_ATTACK, px, py, pz, 0, 0, 0);
            }
        }
    }
}
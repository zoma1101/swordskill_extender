package com.zoma1101.swordskill_extender.item.weapon;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob; // 追加
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.List; // 追加
import java.util.UUID;

public class ClawItem extends SwordItem {
    private static final UUID REACH_MODIFIER_UUID = UUID.fromString("12345678-1234-1234-1234-1234567890ab");

    public ClawItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    // 爪は範囲攻撃（スイープ）しない
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        if (toolAction == ToolActions.SWORD_SWEEP) {
            return false;
        }
        return super.canPerformAction(stack, toolAction);
    }

    // リーチを短くする (-1.25)
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create(super.getAttributeModifiers(slot, stack));
        if (slot == EquipmentSlot.MAINHAND) {
            modifiers.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(REACH_MODIFIER_UUID, "Weapon reach reduction", -1.25, AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }

    // 右クリック: シャドウステップ (前方への高速ダッシュ + 無敵 + ターゲット解除)
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        // 向いている方向にダッシュ
        Vec3 look = pPlayer.getLookAngle();
        pPlayer.push(look.x * 1.5, look.y * 1.5, look.z * 1.5);
        pPlayer.hurtMarked = true;

        pPlayer.resetFallDistance();
        pPlayer.invulnerableTime = 20;

        // ★追加: 周囲の敵のターゲットを外す
        if (!pLevel.isClientSide) {
            // プレイヤーの周囲 10ブロック以内の敵を探す
            List<Mob> mobs = pLevel.getEntitiesOfClass(Mob.class, pPlayer.getBoundingBox().inflate(10.0));
            for (Mob mob : mobs) {
                // 自分をターゲットにしている敵のターゲットを解除
                if (mob.getTarget() == pPlayer) {
                    mob.setTarget(null);
                }
            }
        }

        // 音の演出
        pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 2.0F);

        // クールタイム (1秒)
        pPlayer.getCooldowns().addCooldown(this, 20);

        return InteractionResultHolder.success(itemstack);
    }

    // 攻撃時: バックスタブ判定
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide && entity instanceof LivingEntity target) {
            // クールダウン完了時のみ
            if (player.getAttackStrengthScale(0.5F) > 0.9F) {

                // プレイヤーとターゲットの視線方向の内積を計算
                Vec3 playerLook = player.getLookAngle();
                Vec3 targetLook = target.getLookAngle();

                // 内積が正の数なら、だいたい同じ方向を向いている（＝背後をとっている）
                // 0.6以上で「背後」とみなす
                if (playerLook.dot(targetLook) > 0.6) {
                    float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    // ★修正: 5倍ダメージに加え、固定で10ダメージを追加して確実に倒せるようにする
                    float bonusDamage = (baseDamage * 5.0F) + 10.0F;

                    target.hurt(player.damageSources().playerAttack(player), bonusDamage);

                    // クリティカル音
                    player.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.5F);

                    // 手動でパーティクルを出す
                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.5), target.getZ(), 5, 0.2, 0.2, 0.2, 0.0);
                    }

                    // 通常攻撃をキャンセル
                    player.resetAttackStrengthTicker();
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
                    return true;
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}
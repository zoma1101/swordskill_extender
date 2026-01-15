package com.zoma1101.swordskill_extender.item.weapon;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zoma1101.swordskill_extender.Swordskill_extender;
import com.zoma1101.swordskill_extender.network.PacketHandler;
import com.zoma1101.swordskill_extender.network.S2CPlayAnimationPacket;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class SpearItem extends SwordItem {
    private static final UUID REACH_MODIFIER_UUID = UUID.fromString("c91b0028-1234-4567-8901-234567890123");

    public static final int SPEAR_THRUST_DURATION = 13;

    public SpearItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        if (toolAction == ToolActions.SWORD_SWEEP) {
            return false;
        }
        return super.canPerformAction(stack, toolAction);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create(super.getAttributeModifiers(slot, stack));
        if (slot == EquipmentSlot.MAINHAND) {
            modifiers.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(REACH_MODIFIER_UUID, "Weapon reach increase", 1.0, AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return SPEAR_THRUST_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand pHand) {
        ItemStack itemstack = player.getItemInHand(pHand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(itemstack);
        }
        player.startUsingItem(pHand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        this.releaseUsing(pStack, pLevel, pLivingEntity, 0);
        return pStack;
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeLeft) {
        if (!(pLivingEntity instanceof Player player)) {
            return;
        }

        int useDuration = this.getUseDuration(pStack) - pTimeLeft;

        if (useDuration < 2 && pTimeLeft > 0) {
            return;
        }

        Vec3 look = player.getLookAngle();

        // 空中にいる場合のみダッシュ処理
        if (!player.onGround()) {
            double dashSpeed = 1.2;
            player.hurtMarked = true;
            player.push(look.x * dashSpeed, 0.2, look.z * dashSpeed);
        }

        // サーバー側でのみ実行する処理
        if (!pLevel.isClientSide && player instanceof ServerPlayer) {
            ResourceLocation animId = ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "spear_attack");
            PacketHandler.sendToTracking(new S2CPlayAnimationPacket(player.getId(), animId), player);

            // 攻撃判定処理
            double momentum = player.getDeltaMovement().length();
            Vec3 reachVec = look.scale(3.0);
            AABB attackBox = player.getBoundingBox().expandTowards(reachVec).inflate(1.0);
            List<LivingEntity> entities = pLevel.getEntitiesOfClass(LivingEntity.class, attackBox,
                    e -> e != player && e.isAlive() && player.hasLineOfSight(e));

            float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);

            for (LivingEntity target : entities) {
                float damage = baseDamage * (float) (1.0 + momentum);
                target.hurt(player.damageSources().playerAttack(player), damage);
                target.knockback(0.5, player.getX() - target.getX(), player.getZ() - target.getZ());
            }

            player.getCooldowns().addCooldown(this, 40);
            pStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
        }
    }

    // ★追加: 一人称視点のアニメーション処理
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
                // アニメーション進行度の計算
                float progress = 0f;
                var data = PlayerAnimationAccess.getPlayerAssociatedData(player);
                var layerObj = data.get(ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "animation_layer"));
                if (layerObj instanceof ModifierLayer<?> layer) {
                    var animation = layer.getAnimation();
                    if (animation instanceof KeyframeAnimationPlayer playerAnim && playerAnim.isActive()) {
                        float tick = playerAnim.getTick() + partialTick;
                        float maxTick = 20.0f;
                        progress = Mth.clamp(tick / maxTick, 0f, 1f);
                    }
                }

                int side = (arm == HumanoidArm.RIGHT) ? 1 : -1;

                // --- 1. バニラの基本位置補正 ---
                poseStack.translate(side * 0.56, -0.52, -0.72);

                // --- 2. アニメーション設定 ---
                // Idle (標準位置) - JSONモデルの値をそのまま使用するため0
                float idle_rot_x = 0f;
                float idle_rot_y = 0f;
                float idle_rot_z = 0f;
                float idle_trans_x = 0f;
                float idle_trans_y = 0f;
                float idle_trans_z = 0f;

                // Windup (引き)
                float windup_trans_z = 4.0f / 16.0f;

                // Thrust (突き)
                float thrust_rot_x = -77.5f;
                float thrust_trans_y = 0f;
                float thrust_trans_z = -12.0f / 16.0f;

                // --- 3. 進行度に応じた計算 ---
                float cur_rot_x, cur_rot_y, cur_rot_z;
                float cur_trans_x, cur_trans_y, cur_trans_z;

                if (progress <= 0f) {
                    // Idle
                    cur_rot_x = idle_rot_x;
                    cur_trans_x = idle_trans_x;
                    cur_trans_y = idle_trans_y;
                    cur_trans_z = idle_trans_z;
                } else if (progress < 0.1f) {
                    // Windup (引き)
                    float t = progress / 0.1f;
                    cur_rot_x = idle_rot_x;
                    cur_trans_x = idle_trans_x;
                    cur_trans_y = idle_trans_y;
                    cur_trans_z = Mth.lerp(t, idle_trans_z, windup_trans_z);
                } else if (progress < 0.25f) {
                    // Thrust (突き)
                    float t = (progress - 0.1f) / 0.15f;

                    // ★修正: 回転は即時変更 (補間せず thrust_rot_x を直接代入)
                    cur_rot_x = thrust_rot_x;

                    cur_trans_x = idle_trans_x;
                    cur_trans_y = Mth.lerp(t, idle_trans_y, thrust_trans_y);
                    cur_trans_z = Mth.lerp(t, windup_trans_z, thrust_trans_z);
                } else if (progress < 0.85f) {
                    // Hold (維持)
                    cur_rot_x = thrust_rot_x;
                    cur_trans_x = idle_trans_x;
                    cur_trans_y = thrust_trans_y;
                    cur_trans_z = thrust_trans_z;
                } else {
                    // Return (戻り)
                    float t = (progress - 0.85f) / 0.15f;
                    cur_rot_x = Mth.lerp(t, thrust_rot_x, idle_rot_x);
                    cur_trans_x = idle_trans_x;
                    cur_trans_y = Mth.lerp(t, thrust_trans_y, idle_trans_y);
                    cur_trans_z = Mth.lerp(t, thrust_trans_z, idle_trans_z);
                }

                cur_rot_y = idle_rot_y;
                cur_rot_z = idle_rot_z;
                float scale = 1.2f;

                // --- 4. 適用 ---
                poseStack.translate(cur_trans_x * side, cur_trans_y, cur_trans_z);
                poseStack.mulPose(Axis.ZP.rotationDegrees(cur_rot_z * side));
                poseStack.mulPose(Axis.YP.rotationDegrees(cur_rot_y * side));
                poseStack.mulPose(Axis.XP.rotationDegrees(cur_rot_x));
                poseStack.scale(scale, scale, scale);

                return true;
            }
        });
    }
}
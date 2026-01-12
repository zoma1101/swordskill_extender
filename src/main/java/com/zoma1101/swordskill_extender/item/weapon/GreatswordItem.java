package com.zoma1101.swordskill_extender.item.weapon;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.zoma1101.swordskill_extender.Swordskill_extender;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags; // ★追加: ダメージタグのインポート
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
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

public class GreatswordItem extends SwordItem {

    private static final UUID CHARGE_BONUS_UUID = UUID.fromString("c83a0028-1234-4567-8901-234567890abc");

    public GreatswordItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 30; // 1.5秒チャージ
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(itemstack);
    }

    // チャージ完了時の処理
    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (!pLevel.isClientSide && pLivingEntity instanceof Player player) {
            // NBTに強化状態と期限を書き込む
            CompoundTag tag = pStack.getOrCreateTag();
            tag.putBoolean("buff_active", true);
            // 現在時刻 + 200tick (10秒間有効)
            tag.putLong("buff_expiry", pLevel.getGameTime() + 100);

            // クールタイム 5秒
            player.getCooldowns().addCooldown(this, 200);
        }
        return super.finishUsingItem(pStack, pLevel, pLivingEntity);
    }

    // 毎tick呼ばれる処理 (期限切れのチェック)
    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (!pLevel.isClientSide && pStack.hasTag()) {
            CompoundTag tag = pStack.getTag();
            if (tag.getBoolean("buff_active")) {
                // 期限を過ぎていたら強化状態を解除
                if (pLevel.getGameTime() >= tag.getLong("buff_expiry")) {
                    tag.remove("buff_active");
                    tag.remove("buff_expiry");
                }
            }
        }
    }

    // アイテムの属性（攻撃力など）を取得するメソッドをオーバーライド
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        // 元々の属性（基本攻撃力など）を取得して、変更可能なMapにコピー
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create(super.getAttributeModifiers(slot, stack));

        // メインハンドに持っている時、かつ強化状態なら
        if (slot == EquipmentSlot.MAINHAND) {
            if (stack.hasTag() && stack.getTag().getBoolean("buff_active")) {
                modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(CHARGE_BONUS_UUID, "Greatsword charge bonus", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        return modifiers;
    }

    // ★追加: ダメージ軽減処理用のイベントハンドラ
    @Mod.EventBusSubscriber(modid = Swordskill_extender.MODID)
    public static class GreatswordEventHandler {
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (event.getEntity() instanceof Player player) {
                // プレイヤーが大剣を使用中（チャージ中）かチェック
                if (player.isUsingItem() && player.getUseItem().getItem() instanceof GreatswordItem) {

                    // ガード可能かチェック (防御無視ダメージなどは防げない)
                    // ★修正: isBypassArmor() ではなくタグを使用する
                    boolean isGuardable = !event.getSource().is(DamageTypeTags.BYPASSES_ARMOR);

                    // 方向チェック (前方からの攻撃のみガード可能)
                    if (isGuardable) {
                        Vec3 sourcePos = event.getSource().getSourcePosition();
                        if (sourcePos != null) {
                            Vec3 viewVec = player.getViewVector(1.0F);
                            Vec3 sourceToPlayer = sourcePos.vectorTo(player.position()).normalize();
                            // Y軸の影響を無視して水平方向で判定
                            sourceToPlayer = new Vec3(sourceToPlayer.x, 0.0D, sourceToPlayer.z);

                            // 内積が0以上なら「後ろまたは真横」からの攻撃なのでガード不可
                            // (sourceToPlayerは「敵→自分」のベクトルなので、自分が向いている方向(viewVec)と逆向きなら内積はマイナスになる)
                            if (sourceToPlayer.dot(viewVec) >= 0.0D) {
                                isGuardable = false;
                            }
                        }
                    }

                    if (isGuardable) {
                        // ダメージを60%軽減 (受けるダメージは40%)
                        event.setAmount(event.getAmount() * 0.4F);

                        // ガード音を再生
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.SHIELD_BLOCK, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }
}
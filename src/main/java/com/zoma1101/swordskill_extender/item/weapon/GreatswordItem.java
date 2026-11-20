package com.zoma1101.swordskill_extender.item.weapon;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class GreatswordItem extends SwordItem {
    private static final ResourceLocation CHARGE_BONUS_ID =
            ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "greatsword_charge_bonus");

    private final Tier tier;
    private final int damage;
    private final float speed;

    public GreatswordItem(Tier tier, int damage, float speed, Properties properties) {
        super(tier, properties);
        this.tier = tier;
        this.damage = damage;
        this.speed = speed;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 30;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }

    // チャージ完了時にカスタムデータへ強化状態と期限を書き込む
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                tag.putBoolean("buff_active", true);
                tag.putLong("buff_expiry", level.getGameTime() + 100);
            });
            player.getCooldowns().addCooldown(this, 200);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide) {
            CustomData data = stack.get(DataComponents.CUSTOM_DATA);
            if (data != null) {
                CompoundTag tag = data.copyTag();
                if (tag.getBoolean("buff_active") && level.getGameTime() >= tag.getLong("buff_expiry")) {
                    CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> {
                        t.remove("buff_active");
                        t.remove("buff_expiry");
                    });
                }
            }
        }
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers modifiers = SwordItem.createAttributes(tier, damage, speed);
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null && data.copyTag().getBoolean("buff_active")) {
            modifiers = modifiers.withModifierAdded(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(CHARGE_BONUS_ID, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                    EquipmentSlotGroup.MAINHAND);
        }
        return modifiers;
    }

    public static class GreatswordEventHandler {
        @SubscribeEvent
        public static void onLivingHurt(LivingIncomingDamageEvent event) {
            if (event.getEntity() instanceof Player player) {
                if (player.isUsingItem() && player.getUseItem().getItem() instanceof GreatswordItem) {
                    boolean isGuardable = !event.getSource().is(DamageTypeTags.BYPASSES_ARMOR);

                    if (isGuardable) {
                        Vec3 sourcePos = event.getSource().getSourcePosition();
                        if (sourcePos != null) {
                            Vec3 viewVec = player.getViewVector(1.0F);
                            Vec3 sourceToPlayer = sourcePos.vectorTo(player.position()).normalize();
                            sourceToPlayer = new Vec3(sourceToPlayer.x, 0.0D, sourceToPlayer.z);

                            if (sourceToPlayer.dot(viewVec) >= 0.0D) {
                                isGuardable = false;
                            }
                        }
                    }

                    if (isGuardable) {
                        event.setAmount(event.getAmount() * 0.4F);
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }
}

package com.zoma1101.swordskill_extender.screen;

import com.zoma1101.swordskill_extender.block.entity.WeaponWorkbenchBlockEntity;
import com.zoma1101.swordskill_extender.init.ModBlocks;
import com.zoma1101.swordskill_extender.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class WeaponWorkbenchMenu extends AbstractContainerMenu {
    public final WeaponWorkbenchBlockEntity blockEntity;
    private final ContainerData data;

    public WeaponWorkbenchMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(3));
    }

    public WeaponWorkbenchMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.WEAPON_WORKBENCH_MENU.get(), pContainerId);
        checkContainerSize(inv, 7);
        blockEntity = ((WeaponWorkbenchBlockEntity) entity);
        this.data = data;

        addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu pContainerToSend, int pSlotInd, ItemStack pStack) {}
            @Override
            public void dataChanged(AbstractContainerMenu pContainerMenu, int pSlotId, int pValue) {}
        });

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // 入力スロット (6つ)
            this.addSlot(new SlotItemHandler(handler, 0, 97, 16));
            this.addSlot(new SlotItemHandler(handler, 1, 97, 35));
            this.addSlot(new SlotItemHandler(handler, 2, 97, 54));
            this.addSlot(new SlotItemHandler(handler, 3, 116, 16));
            this.addSlot(new SlotItemHandler(handler, 4, 116, 35));
            this.addSlot(new SlotItemHandler(handler, 5, 116, 54));

            // ★変更: 出力スロット (1つ)
            // ここで onTake をオーバーライドして、アイテム取得時に素材を消費させる
            this.addSlot(new SlotItemHandler(handler, 6, 142, 54) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false; // プレイヤーはこのスロットにアイテムを置けない
                }

                @Override
                public void onTake(Player player, ItemStack stack) {
                    // アイテムを取り出した時に BlockEntity の craftItem を呼ぶ
                    blockEntity.craftItem();
                    super.onTake(player, stack);
                }
            });
        });

        addDataSlots(data);

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    public int getSelectedWeaponId() {
        return this.data.get(0);
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 7;

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(pPlayer.level(), blockEntity.getBlockPos()),
                pPlayer, ModBlocks.WEAPON_WORKBENCH.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
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
        // スロット数チェック: 25入力 + 1出力 = 26
        checkContainerSize(inv, 26);
        blockEntity = ((WeaponWorkbenchBlockEntity) entity);
        this.data = data;

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // --- 5x5 入力スロット (インデックス 0-24) ---
            // GUI上の開始位置 (テクスチャに合わせて調整してください)
            int gridStartX = 14;
            int gridStartY = 18;

            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    // addSlotで座標を指定するだけで、アイテムの配置・操作が可能になります
                    this.addSlot(new SlotItemHandler(handler, col + row * 5, gridStartX + col * 18, gridStartY + row * 18));
                }
            }

            // --- 出力スロット (インデックス 25) ---
            // グリッドの右側に配置
            this.addSlot(new SlotItemHandler(handler, 25, 128, 54) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false; // プレイヤーはここに置けない
                }

                @Override
                public void onTake(Player player, ItemStack stack) {
                    blockEntity.craftItem(); // 取り出し時にクラフト処理を実行
                    super.onTake(player, stack);
                }
            });
        });

        addDataSlots(data);

        // 5x5グリッドで縦に長くなるため、プレイヤーインベントリの位置を下にずらします
        // yOffset: 122 (調整値)
        addPlayerInventory(inv, 122);
        addPlayerHotbar(inv, 180);
    }

    public int getSelectedWeaponId() {
        return this.data.get(0);
    }

    // Shiftクリック（クイックムーブ）のロジック
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int VANILLA_SLOT_COUNT = 36;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 26; // 25 + 1

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // プレイヤーインベントリ -> 作業台
        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // 出力スロット(最後)には移動させないため -1
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT - 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        // 作業台 -> プレイヤーインベントリ
        else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
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

    private void addPlayerInventory(Inventory playerInventory, int yOffset) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, yOffset + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory, int yOffset) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, yOffset));
        }
    }
}
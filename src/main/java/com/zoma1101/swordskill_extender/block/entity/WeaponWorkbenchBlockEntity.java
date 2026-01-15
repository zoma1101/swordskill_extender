package com.zoma1101.swordskill_extender.block.entity;

import com.zoma1101.swordskill_extender.init.ModBlockEntities;
import com.zoma1101.swordskill_extender.recipe.WeaponWorkbenchRecipe;
import com.zoma1101.swordskill_extender.screen.WeaponWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class WeaponWorkbenchBlockEntity extends BlockEntity implements MenuProvider {
    private boolean isCrafting = false;

    // 25入力 + 1出力 = 26スロット
    private final ItemStackHandler itemHandler = new ItemStackHandler(26) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            // 入力スロット(0-24)が変更された場合、出力スロットを更新する
            if (!isCrafting && slot < 25 && level != null && !level.isClientSide) {
                updateOutput();
            }
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected final ContainerData data;

    public WeaponWorkbenchBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.WEAPON_WORKBENCH_BE.get(), pPos, pBlockState);
        this.data = new SimpleContainerData(3) {
            @Override
            public int get(int pIndex) { return 0; }
            @Override
            public void set(int pIndex, int pValue) {}
        };
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        // 何もしない
    }

    // 全レシピを走査してマッチするものを探す
    private void updateOutput() {
        if (level == null) return;

        Optional<WeaponWorkbenchRecipe> match = level.getRecipeManager()
                .getAllRecipesFor(WeaponWorkbenchRecipe.Type.INSTANCE).stream()
                .filter(this::canCraft)
                .findFirst();

        if (match.isPresent()) {
            ItemStack result = match.get().getResultItem(level.registryAccess()).copy();
            itemHandler.setStackInSlot(25, result);
        } else {
            itemHandler.setStackInSlot(25, ItemStack.EMPTY);
        }
    }

    private boolean canCraft(WeaponWorkbenchRecipe recipe) {
        NonNullList<ItemStack> ingredients = recipe.getInputs();
        // レシピの材料数がスロット数(25)を超えていないか
        if (ingredients.size() > 25) return false;

        // 各スロットと材料を比較
        for (int i = 0; i < ingredients.size(); i++) {
            ItemStack required = ingredients.get(i);
            ItemStack input = itemHandler.getStackInSlot(i);

            if (input.isEmpty() || !input.getItem().equals(required.getItem()) || input.getCount() < required.getCount()) {
                return false;
            }
        }
        return true;
    }

    public void craftItem() {
        if (level == null) return;

        // 現在の状態にマッチするレシピを再取得
        Optional<WeaponWorkbenchRecipe> match = level.getRecipeManager()
                .getAllRecipesFor(WeaponWorkbenchRecipe.Type.INSTANCE).stream()
                .filter(this::canCraft)
                .findFirst();

        if (match.isPresent()) {
            WeaponWorkbenchRecipe recipe = match.get();
            isCrafting = true;

            NonNullList<ItemStack> ingredients = recipe.getInputs();
            for (int i = 0; i < ingredients.size(); i++) {
                itemHandler.extractItem(i, ingredients.get(i).getCount(), false);
            }

            isCrafting = false;
            updateOutput();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.swordskill_extender.weapon_workbench");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new WeaponWorkbenchMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        if (itemHandler.getSlots() != 26) {
            itemHandler.setSize(26);
        }
    }
}
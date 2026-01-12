package com.zoma1101.swordskill_extender.block.entity;

import com.zoma1101.swordskill_extender.init.ModBlockEntities;
import com.zoma1101.swordskill_extender.recipe.WeaponWorkbenchRecipe;
import com.zoma1101.swordskill_extender.screen.WeaponWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
    // クラフト処理中かどうかを防ぐフラグ
    private boolean isCrafting = false;

    private final ItemStackHandler itemHandler = new ItemStackHandler(7) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            // 入力スロット(0-5)が変更された場合、出力スロットを更新する
            if (!isCrafting && slot < 6 && level != null && !level.isClientSide) {
                updateOutput();
            }
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected final ContainerData data;

    private ResourceLocation selectedRecipeId = null;

    // progress関連は不要になりましたが、ContainerDataの互換性のために残すか、ダミーにします
    private int progress = 0;
    private int maxProgress = 0;

    public WeaponWorkbenchBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.WEAPON_WORKBENCH_BE.get(), pPos, pBlockState);

        this.data = new SimpleContainerData(3) {
            @Override
            public int get(int pIndex) {
                return 0;
            }
            @Override
            public void set(int pIndex, int pValue) {
            }
        };
    }

    public void setSelectedRecipe(ResourceLocation recipeId) {
        this.selectedRecipeId = recipeId;
        setChanged();
        // レシピが変更されたら出力を再計算
        if (level != null && !level.isClientSide) {
            updateOutput();
        }
    }

    // tickメソッドでのクラフトロジックは削除します
    public void tick(Level level, BlockPos pos, BlockState state) {
        // 何もしない
    }

    // 出力スロットの内容を更新する（プレビュー表示）
    private void updateOutput() {
        if (level == null) return;

        if (selectedRecipeId == null) {
            itemHandler.setStackInSlot(6, ItemStack.EMPTY);
            return;
        }

        Optional<WeaponWorkbenchRecipe> recipeOpt = level.getRecipeManager()
                .byKey(selectedRecipeId)
                .filter(r -> r.getType() == WeaponWorkbenchRecipe.Type.INSTANCE)
                .map(r -> (WeaponWorkbenchRecipe) r);

        if (recipeOpt.isPresent()) {
            WeaponWorkbenchRecipe recipe = recipeOpt.get();
            if (canCraft(recipe)) {
                // クラフト可能なら完成品をセット（まだ消費はしない）
                ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
                itemHandler.setStackInSlot(6, result);
            } else {
                itemHandler.setStackInSlot(6, ItemStack.EMPTY);
            }
        } else {
            itemHandler.setStackInSlot(6, ItemStack.EMPTY);
        }
    }

    private boolean canCraft(WeaponWorkbenchRecipe recipe) {
        // 入力スロットのチェックのみ行う（出力スロットの空きチェックは上書きするので不要）
        NonNullList<ItemStack> ingredients = recipe.getInputs();
        if (ingredients.size() > 6) return false;

        for (int i = 0; i < ingredients.size(); i++) {
            ItemStack required = ingredients.get(i);
            ItemStack input = itemHandler.getStackInSlot(i);

            if (input.isEmpty() || !input.getItem().equals(required.getItem()) || input.getCount() < required.getCount()) {
                return false;
            }
        }
        return true;
    }

    // Menuから呼ばれる：アイテムを取り出した時に素材を消費する
    public void craftItem() {
        if (selectedRecipeId == null || level == null) return;

        Optional<WeaponWorkbenchRecipe> recipeOpt = level.getRecipeManager()
                .byKey(selectedRecipeId)
                .filter(r -> r.getType() == WeaponWorkbenchRecipe.Type.INSTANCE)
                .map(r -> (WeaponWorkbenchRecipe) r);

        if (recipeOpt.isPresent()) {
            WeaponWorkbenchRecipe recipe = recipeOpt.get();

            // フラグを立てて onContentsChanged による updateOutput のループを防ぐ
            isCrafting = true;

            // 素材を消費
            NonNullList<ItemStack> ingredients = recipe.getInputs();
            for (int i = 0; i < ingredients.size(); i++) {
                itemHandler.extractItem(i, ingredients.get(i).getCount(), false);
            }

            isCrafting = false;

            // 消費後の状態で再度出力スロットを更新（連続作成のため）
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
        if (selectedRecipeId != null) {
            pTag.putString("weapon_workbench.selected_recipe", selectedRecipeId.toString());
        }
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        if (itemHandler.getSlots() != 7) {
            itemHandler.setSize(7);
        }
        if (pTag.contains("weapon_workbench.selected_recipe")) {
            selectedRecipeId = ResourceLocation.parse(pTag.getString("weapon_workbench.selected_recipe"));
        }
    }
}
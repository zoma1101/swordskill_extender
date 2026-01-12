package com.zoma1101.swordskill_extender.network;

import com.zoma1101.swordskill_extender.block.entity.WeaponWorkbenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketC2SSelectRecipe {
    private final ResourceLocation recipeId;
    private final BlockPos pos;

    public PacketC2SSelectRecipe(ResourceLocation recipeId, BlockPos pos) {
        this.recipeId = recipeId;
        this.pos = pos;
    }

    public PacketC2SSelectRecipe(FriendlyByteBuf buf) {
        this.recipeId = buf.readResourceLocation();
        this.pos = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(recipeId);
        buf.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                // サーバー側でブロックエンティティを取得し、レシピIDを設定する
                BlockEntity be = player.level().getBlockEntity(pos);
                if (be instanceof WeaponWorkbenchBlockEntity workbench) {
                    workbench.setSelectedRecipe(recipeId);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
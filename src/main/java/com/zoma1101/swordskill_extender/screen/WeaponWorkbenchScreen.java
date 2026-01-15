package com.zoma1101.swordskill_extender.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zoma1101.swordskill_extender.Swordskill_extender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot; // 追加

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class WeaponWorkbenchScreen extends AbstractContainerScreen<WeaponWorkbenchMenu> {
    private static final ResourceLocation TEXTURE =
            fromNamespaceAndPath(Swordskill_extender.MODID, "textures/gui/weapon_workbench.png");

    // ★追加: バニラのチェストGUIのテクスチャ（ここからスロットの絵柄を借ります）
    private static final ResourceLocation VANILLA_CHEST_TEXTURE = ResourceLocation.parse("textures/gui/container/generic_54.png");

    public WeaponWorkbenchScreen(WeaponWorkbenchMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 204;
        this.inventoryLabelY = 110;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 1. メインの背景（枠のみの画像など）を描画
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // 2. バニラのスロット画像を全てのスロット位置に重ねて描画
        // これにより、背景画像にスロット枠を描き込む必要がなくなります
        RenderSystem.setShaderTexture(0, VANILLA_CHEST_TEXTURE);

        for (Slot slot : this.menu.slots) {
            // スロットの座標はアイテムの描画位置なので、枠は -1 して 18x18 で描画します
            // generic_54.png の (7, 17) にスロットの画像があります
            guiGraphics.blit(VANILLA_CHEST_TEXTURE, x + slot.x - 1, y + slot.y - 1, 7, 17, 18, 18);
        }
    }
}
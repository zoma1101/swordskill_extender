package com.zoma1101.swordskill_extender.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zoma1101.swordskill_extender.Swordskill_extender;
import com.zoma1101.swordskill_extender.network.ClientboundWeaponProficiencySyncPacket;
import com.zoma1101.swordskill_extender.network.PacketC2SSelectRecipe;
import com.zoma1101.swordskill_extender.network.PacketHandler;
import com.zoma1101.swordskill_extender.recipe.WeaponWorkbenchRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.stream.Collectors;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class WeaponWorkbenchScreen extends AbstractContainerScreen<WeaponWorkbenchMenu> {
    private static final ResourceLocation TEXTURE =
            fromNamespaceAndPath(Swordskill_extender.MODID, "textures/gui/weapon_workbench.png");

    // Recipe Data
    private final Map<ResourceLocation, List<WeaponWorkbenchRecipe>> recipeGroups = new LinkedHashMap<>();
    private final List<ResourceLocation> groupKeys = new ArrayList<>();

    // State
    private ResourceLocation currentGroup = null; // null = Showing Groups, not null = Showing Recipes
    private WeaponWorkbenchRecipe selectedRecipe = null;

    // Scroll
    private float scrollOffs = 0.0F;
    private boolean scrolling = false;

    // Layout Constants
    private static final int LIST_X = 7;
    private static final int LIST_Y = 25;
    private static final int SLOT_SIZE = 18;
    private static final int VISIBLE_ROWS = 3;
    private static final int LIST_WIDTH = 76; // Width of the list area
    private static final int COLUMNS = LIST_WIDTH / SLOT_SIZE; // 76 / 18 = 4 columns

    private static final int SCROLLBAR_X = LIST_X + LIST_WIDTH;
    private static final int SCROLLBAR_Y = 25;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 54;

    public WeaponWorkbenchScreen(WeaponWorkbenchMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        initRecipes();
    }

    private void initRecipes() {
        this.recipeGroups.clear();
        this.groupKeys.clear();

        if (this.minecraft != null) {
            this.minecraft.getResourceManager()
                    .listResources("textures/icon/weapon_type", location ->
                            location.getNamespace().equals(Swordskill_extender.MODID) &&
                                    location.getPath().endsWith(".png")
                    )
                    .keySet()
                    .forEach(icon -> this.recipeGroups.put(icon, new ArrayList<>()));
        }

        if (this.minecraft != null && this.minecraft.level != null) {
            List<WeaponWorkbenchRecipe> foundRecipes = this.minecraft.level.getRecipeManager()
                    .getAllRecipesFor(WeaponWorkbenchRecipe.Type.INSTANCE);

            System.out.println("WeaponWorkbenchScreen: Loaded " + foundRecipes.size() + " recipes.");

            for (WeaponWorkbenchRecipe recipe : foundRecipes) {
                this.recipeGroups.computeIfAbsent(recipe.getIcon(), k -> new ArrayList<>()).add(recipe);
            }
        }

        this.groupKeys.addAll(this.recipeGroups.keySet());
        this.groupKeys.sort(Comparator.comparing(ResourceLocation::getPath));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);

        renderGrid(guiGraphics, mouseX, mouseY);
        renderScrollbar(guiGraphics);

        if (this.selectedRecipe != null) {
            renderGhostIngredients(guiGraphics);
        }

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderGrid(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<?> displayList = getDisplayList();
        int listSize = displayList.size();

        if (listSize == 0) {
            guiGraphics.drawString(this.font, "No Data", this.leftPos + LIST_X, this.topPos + LIST_Y, 0xFF0000, false);
            return;
        }

        int totalRows = (listSize + COLUMNS - 1) / COLUMNS;
        int maxScroll = Math.max(0, totalRows - VISIBLE_ROWS);
        int startRow = (int) (this.scrollOffs * maxScroll + 0.5f);
        int startIndex = startRow * COLUMNS;
        int endIndex = Math.min(startIndex + (VISIBLE_ROWS * COLUMNS), listSize);

        for (int i = startIndex; i < endIndex; i++) {
            int relativeIndex = i - startIndex;
            int col = relativeIndex % COLUMNS;
            int row = relativeIndex / COLUMNS;

            int x = this.leftPos + LIST_X + (col * SLOT_SIZE);
            int y = this.topPos + LIST_Y + (row * SLOT_SIZE);

            // Draw Slot Background
            boolean isHovered = mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE;
            renderSlot(guiGraphics, x, y, isHovered);

            // Draw Content
            Object item = displayList.get(i);
            if (item instanceof ResourceLocation icon) {
                // Group Icon
                guiGraphics.blit(icon, x + 1, y + 1, 0, 0, 16, 16, 16, 16);
            } else if (item instanceof WeaponWorkbenchRecipe recipe) {
                // Recipe Item
                ItemStack output = recipe.getResultItem(this.minecraft.level.registryAccess());
                guiGraphics.renderItem(output, x + 1, y + 1);
                // Highlight selected
                if (this.selectedRecipe == recipe) {
                    guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0x80FFFFFF);
                }
            } else if (item instanceof String && item.equals("BACK")) {
                // Back Button (Arrow)
                guiGraphics.renderItem(new ItemStack(Items.ARROW), x + 1, y + 1);
            }

            // Tooltip
            if (isHovered) {
                if (item instanceof ResourceLocation icon) {
                    guiGraphics.renderTooltip(this.font, Component.literal(formatPath(icon)), mouseX, mouseY);
                } else if (item instanceof WeaponWorkbenchRecipe recipe) {
                    ItemStack output = recipe.getResultItem(this.minecraft.level.registryAccess());
                    guiGraphics.renderTooltip(this.font, output, mouseX, mouseY);
                } else if (item instanceof String && item.equals("BACK")) {
                    guiGraphics.renderTooltip(this.font, Component.literal("Back"), mouseX, mouseY);
                }
            }
        }
    }

    private void renderSlot(GuiGraphics guiGraphics, int x, int y, boolean isHovered) {
        int colorTopLeft = 0xFF373737;
        int colorBottomRight = 0xFFFFFFFF;
        int colorBg = 0xFF8B8B8B;

        if (isHovered) {
            colorBg = 0xFFC6C6C6; // Lighter when hovered
        }

        guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, colorBg);
        guiGraphics.fill(x, y, x + SLOT_SIZE, y + 1, colorTopLeft);
        guiGraphics.fill(x, y, x + 1, y + SLOT_SIZE, colorTopLeft);
        guiGraphics.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, colorBottomRight);
        guiGraphics.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, colorBottomRight);
    }

    private List<?> getDisplayList() {
        if (currentGroup == null) {
            return groupKeys;
        } else {
            List<Object> list = new ArrayList<>();
            list.add("BACK"); // First item is Back button
            List<WeaponWorkbenchRecipe> recipes = recipeGroups.get(currentGroup);
            if (recipes != null) {
                list.addAll(recipes);
            }
            return list;
        }
    }

    private String formatPath(ResourceLocation icon) {
        String path = icon.getPath();
        String fileName = path.substring(path.lastIndexOf('/') + 1).replace(".png", "");
        return Arrays.stream(fileName.split("_"))
                .map(s -> s.isEmpty() ? "" : s.substring(0, 1).toUpperCase() + s.substring(1))
                .collect(Collectors.joining(" "));
    }

    private void renderGhostIngredients(GuiGraphics guiGraphics) {
        NonNullList<ItemStack> inputs = this.selectedRecipe.getInputs();
        int[] slotX = {97, 97, 97, 116, 116, 116};
        int[] slotY = {16, 35, 54, 16, 35, 54};

        for (int i = 0; i < inputs.size() && i < slotX.length; i++) {
            ItemStack stack = inputs.get(i);
            if (!stack.isEmpty()) {
                int x = this.leftPos + slotX[i];
                int y = this.topPos + slotY[i];

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
                guiGraphics.renderItem(stack, x, y);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                guiGraphics.renderItemDecorations(this.font, stack, x, y);
            }
        }
    }

    private void renderScrollbar(GuiGraphics guiGraphics) {
        int sx = this.leftPos + SCROLLBAR_X;
        int sy = this.topPos + SCROLLBAR_Y;
        guiGraphics.fill(sx, sy, sx + SCROLLBAR_WIDTH, sy + SCROLLBAR_HEIGHT, 0xFF202020);
        int thumbHeight = 15;
        int trackHeight = SCROLLBAR_HEIGHT;
        int thumbY = sy + (int)((trackHeight - thumbHeight) * this.scrollOffs);

        if (isScrollBarActive()) {
            guiGraphics.fill(sx + 1, thumbY, sx + SCROLLBAR_WIDTH - 1, thumbY + thumbHeight, 0xFFC6C6C6);
        } else {
            guiGraphics.fill(sx + 1, sy, sx + SCROLLBAR_WIDTH - 1, sy + thumbHeight, 0xFF555555);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    private boolean isScrollBarActive() {
        int listSize = getDisplayList().size();
        int totalRows = (listSize + COLUMNS - 1) / COLUMNS;
        return totalRows > VISIBLE_ROWS;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Grid Click
            if (mouseX >= leftPos + LIST_X && mouseX < leftPos + LIST_X + LIST_WIDTH &&
                    mouseY >= topPos + LIST_Y && mouseY < topPos + LIST_Y + (VISIBLE_ROWS * SLOT_SIZE)) {

                List<?> displayList = getDisplayList();
                int listSize = displayList.size();
                int totalRows = (listSize + COLUMNS - 1) / COLUMNS;
                int maxScroll = Math.max(0, totalRows - VISIBLE_ROWS);
                int startRow = (int) (this.scrollOffs * maxScroll + 0.5f);

                int clickedCol = (int) ((mouseX - (leftPos + LIST_X)) / SLOT_SIZE);
                int clickedRow = (int) ((mouseY - (topPos + LIST_Y)) / SLOT_SIZE);

                int actualIndex = (startRow + clickedRow) * COLUMNS + clickedCol;

                if (actualIndex >= 0 && actualIndex < listSize) {
                    Object clickedItem = displayList.get(actualIndex);
                    handleItemClick(clickedItem);
                    return true;
                }
            }

            // Scrollbar Click
            if (this.isScrollBarActive()) {
                int sx = this.leftPos + SCROLLBAR_X;
                int sy = this.topPos + SCROLLBAR_Y;
                if (mouseX >= sx && mouseX < sx + SCROLLBAR_WIDTH && mouseY >= sy && mouseY < sy + SCROLLBAR_HEIGHT) {
                    this.scrolling = true;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleItemClick(Object item) {
        if (item instanceof ResourceLocation icon) {
            // Open Group
            this.currentGroup = icon;
            this.scrollOffs = 0.0F; // Reset scroll
        } else if (item instanceof String && item.equals("BACK")) {
            // Go Back
            this.currentGroup = null;
            this.scrollOffs = 0.0F; // Reset scroll
        } else if (item instanceof WeaponWorkbenchRecipe recipe) {
            // Select Recipe
            this.selectedRecipe = recipe;
            System.out.println("Selected Recipe: " + selectedRecipe.getId());

            PacketHandler.sendToServer(new PacketC2SSelectRecipe(recipe.getId(), this.menu.blockEntity.getBlockPos()));
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling && this.isScrollBarActive()) {
            int sy = this.topPos + SCROLLBAR_Y;
            int trackHeight = SCROLLBAR_HEIGHT;
            int thumbHeight = 15;
            this.scrollOffs = ((float)mouseY - sy - thumbHeight / 2.0F) / ((float)trackHeight - thumbHeight);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.isScrollBarActive()) {
            int listSize = getDisplayList().size();
            int totalRows = (listSize + COLUMNS - 1) / COLUMNS;
            int maxScroll = Math.max(1, totalRows - VISIBLE_ROWS);
            float scrollStep = 1.0F / maxScroll;
            this.scrollOffs = Mth.clamp(this.scrollOffs - (float)delta * scrollStep, 0.0F, 1.0F);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.scrolling = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
package fi.dy.masa.lowtechcrafting.gui;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.lowtechcrafting.inventory.container.ContainerCrafting;
import fi.dy.masa.lowtechcrafting.tileentity.TileEntityCrafting;

public class GUICraftingTable extends GuiContainer  implements IRecipeShownListener
{
    private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/crafting_table.png");
    protected final TileEntityCrafting te;
    protected final ContainerCrafting containerCrafting;
    protected ResourceLocation guiTexture;
    private GuiButtonImage recipeButton;
    private final GuiRecipeBook recipeBookGui;
    private boolean widthTooNarrow;

    public GUICraftingTable(ContainerCrafting container, TileEntityCrafting te)
    {
        super(container);

        this.te = te;
        this.containerCrafting = container;

        // Use the vanilla Dropper/Dispenser GUI texture
        this.guiTexture = new ResourceLocation("textures/gui/container/crafting_table.png");
        this.recipeBookGui = new GuiRecipeBook();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(this.guiTexture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = I18n.format(this.te.getName());
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 5, 0x404040);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 73, 0x404040);
    }

    protected void bindTexture(ResourceLocation rl)
    {
        this.mc.getTextureManager().bindTexture(rl);
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.widthTooNarrow = this.width < 379;
        this.recipeBookGui.func_194303_a(this.width, this.height, this.mc, this.widthTooNarrow, this.te.getCraftingGridWrapperInventory());
        this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
        this.recipeButton = new GuiButtonImage(10, this.guiLeft + 5, this.height / 2 - 49, 20, 18, 0, 168, 19, CRAFTING_TABLE_GUI_TEXTURES);
        this.buttonList.add(this.recipeButton);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        this.recipeBookGui.tick();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();

        if (this.recipeBookGui.isVisible() && this.widthTooNarrow)
        {
            this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
            this.recipeBookGui.render(mouseX, mouseY, partialTicks);
        }
        else
        {
            this.recipeBookGui.render(mouseX, mouseY, partialTicks);
            super.drawScreen(mouseX, mouseY, partialTicks);
            this.recipeBookGui.renderGhostRecipe(this.guiLeft, this.guiTop, true, partialTicks);
        }

        this.renderHoveredToolTip(mouseX, mouseY);
        this.recipeBookGui.renderTooltip(this.guiLeft, this.guiTop, mouseX, mouseY);
    }

    @Override
    protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY)
    {
        return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (!this.recipeBookGui.mouseClicked(mouseX, mouseY, mouseButton))
        {
            if (!this.widthTooNarrow || !this.recipeBookGui.isVisible())
            {
                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    protected boolean hasClickedOutside(int p_193983_1_, int p_193983_2_, int p_193983_3_, int p_193983_4_)
    {
        boolean flag = p_193983_1_ < p_193983_3_ || p_193983_2_ < p_193983_4_ || p_193983_1_ >= p_193983_3_ + this.xSize || p_193983_2_ >= p_193983_4_ + this.ySize;
        return this.recipeBookGui.hasClickedOutside(p_193983_1_, p_193983_2_, this.guiLeft, this.guiTop, this.xSize, this.ySize) && flag;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 10)
        {
            this.recipeBookGui.initVisuals(this.widthTooNarrow, this.te.getCraftingGridWrapperInventory());
            this.recipeBookGui.toggleVisibility();
            this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
            this.recipeButton.setPosition(this.guiLeft + 5, this.height / 2 - 49);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (!this.recipeBookGui.keyPressed(typedChar, keyCode))
        {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        super.handleMouseClick(slotIn, slotId, mouseButton, type);
        this.recipeBookGui.slotClicked(slotIn);
    }

    @Override
    public void recipesUpdated()
    {
        this.recipeBookGui.recipesUpdated();
    }

    @Override
    public void onGuiClosed()
    {
        this.recipeBookGui.removed();
        super.onGuiClosed();
    }

    @Override
    public GuiRecipeBook func_194310_f()
    {
        return this.recipeBookGui;
    }
}

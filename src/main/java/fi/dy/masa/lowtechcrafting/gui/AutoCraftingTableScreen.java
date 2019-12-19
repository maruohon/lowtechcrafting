package fi.dy.masa.lowtechcrafting.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import fi.dy.masa.lowtechcrafting.inventory.container.ContainerCrafting;

public class AutoCraftingTableScreen extends ContainerScreen<ContainerCrafting>// implements IRecipeShownListener
{
    //private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/crafting_table.png");

    protected final ContainerCrafting containerCrafting;
    protected ResourceLocation guiTexture;
    //private final RecipeBookGui recipeBookGui;
    //private boolean widthTooNarrow;

    public AutoCraftingTableScreen(ContainerCrafting container, PlayerInventory playerInv, ITextComponent title)
    {
        super(container, playerInv, title);

        this.containerCrafting = container;

        // Use the vanilla Dropper/Dispenser GUI texture
        this.guiTexture = new ResourceLocation("textures/gui/container/crafting_table.png");
        //this.recipeBookGui = new RecipeBookGui();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bindTexture(this.guiTexture);
        this.blit(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String s = this.title.getFormattedText();
        this.font.drawString(s, this.xSize / 2 - this.font.getStringWidth(s) / 2, 5, 0x404040);
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8, 73, 0x404040);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    /*
    @Override
    public void init()
    {
        super.init();

        this.widthTooNarrow = this.width < 379;
        this.recipeBookGui.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.container);
        this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
        this.children.add(this.recipeBookGui);
        this.setFocusedDefault(this.recipeBookGui);

        this.addButton(new ImageButton(this.guiLeft + 5, this.height / 2 - 49, 20, 18, 0, 168, 19, CRAFTING_TABLE_GUI_TEXTURES, (btn) -> {
            this.recipeBookGui.initSearchBar(this.widthTooNarrow);
            this.recipeBookGui.toggleVisibility();
            this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
            ((ImageButton) btn).setPosition(this.guiLeft + 5, this.height / 2 - 49);
         }));
    }

    @Override
    public void tick()
    {
        super.tick();

        this.recipeBookGui.tick();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();

        if (this.recipeBookGui.isVisible() && this.widthTooNarrow)
        {
            this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
            this.recipeBookGui.render(mouseX, mouseY, partialTicks);
        }
        else
        {
            this.recipeBookGui.render(mouseX, mouseY, partialTicks);
            super.render(mouseX, mouseY, partialTicks);
            this.recipeBookGui.renderGhostRecipe(this.guiLeft, this.guiTop, true, partialTicks);
        }

        this.renderHoveredToolTip(mouseX, mouseY);
        this.recipeBookGui.renderTooltip(this.guiLeft, this.guiTop, mouseX, mouseY);
        this.func_212932_b(this.recipeBookGui);
    }

    @Override
    protected boolean isPointInRegion(int x, int y, int width, int height, double mouseX, double mouseY)
    {
        return (! this.widthTooNarrow || ! this.recipeBookGui.isVisible()) && super.isPointInRegion(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if (this.recipeBookGui.mouseClicked(mouseX, mouseY, mouseButton))
        {
            return true;
        }
        else
        {
            return (this.widthTooNarrow && this.recipeBookGui.isVisible()) ? true : super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton)
    {
        boolean flag = mouseX < guiLeftIn || mouseY < guiTopIn || mouseX >= guiLeftIn + this.xSize || mouseY >= guiTopIn + this.ySize;
        return this.recipeBookGui.func_195604_a(mouseX, mouseY, this.guiLeft, this.guiTop, this.xSize, this.ySize, mouseButton) && flag;
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
    public void removed()
    {
        this.recipeBookGui.removed();
        super.removed();
    }

    @Override
    public RecipeBookGui getRecipeGui()
    {
        return this.recipeBookGui;
    }
    */
}

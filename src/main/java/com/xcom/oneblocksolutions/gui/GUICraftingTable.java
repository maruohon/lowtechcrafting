package com.xcom.oneblocksolutions.gui;

import com.xcom.oneblocksolutions.tileentity.TileEntityCrafting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GUICraftingTable extends GuiContainer{
    protected final TileEntityCrafting te;
    protected ResourceLocation guiTexture;

    public GUICraftingTable(Container container, TileEntityCrafting te)
    {
        super(container);

        this.te = te;

        // Use the vanilla Dropper/Dispenser GUI texture
        this.guiTexture = new ResourceLocation("textures/gui/container/crafting_table.png");
    }

    protected void bindTexture(ResourceLocation rl)
    {
        this.mc.getTextureManager().bindTexture(rl);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float gameTicks)
    {
        this.drawDefaultBackground();

        super.drawScreen(mouseX, mouseY, gameTicks);
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

        String s = this.te.hasCustomName() ? this.te.getName() : I18n.format(this.te.getName());
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 5, 0x404040);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 71, 0x404040);
    }
}

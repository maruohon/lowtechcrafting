package fi.dy.masa.lowtechcrafting.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import fi.dy.masa.lowtechcrafting.inventory.container.ContainerCrafting;

public class AutoCraftingTableScreen extends ContainerScreen<ContainerCrafting>// implements IRecipeShownListener
{
    protected final ContainerCrafting containerCrafting;
    protected ResourceLocation guiTexture;

    public AutoCraftingTableScreen(ContainerCrafting container, PlayerInventory playerInv, ITextComponent title)
    {
        super(container, playerInv, title);

        this.containerCrafting = container;

        // Use the vanilla Dropper/Dispenser GUI texture
        this.guiTexture = new ResourceLocation("textures/gui/container/crafting_table.png");
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float gameTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bind(this.guiTexture);
        this.blit(matrixStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY)
    {
        String s = this.title.getString();
        this.font.draw(matrixStack, s, this.imageWidth / 2.0F - this.font.width(s) / 2.0F, 5, 0x404040);
        this.font.draw(matrixStack, this.inventory.getDisplayName().getString(), 8, 73, 0x404040);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }
}

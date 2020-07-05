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
    protected void func_230450_a_(MatrixStack matrixStack, float gameTicks, int mouseX, int mouseY) // drawGuiContainerBackgroundLayer
    {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.field_230706_i_.getTextureManager().bindTexture(this.guiTexture);
        this.func_238474_b_(matrixStack, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize); // blit
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, int mouseX, int mouseY) // drawGuiContainerForegroundLayer
    {
        String s = this.field_230704_d_.getString(); // title.getFormattedText()
        this.field_230712_o_.func_238421_b_(matrixStack, s, this.xSize / 2.0F - this.field_230712_o_.getStringWidth(s) / 2.0F, 5, 0x404040); // drawString
        this.field_230712_o_.func_238421_b_(matrixStack, this.playerInventory.getDisplayName().getString(), 8, 73, 0x404040);
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) // render
    {
        this.func_230446_a_(matrixStack); // renderBackground
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks); // render
        this.func_230459_a_(matrixStack, mouseX, mouseY); // renderHoveredToolTip
    }
}

package fi.dy.masa.lowtechcrafting.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import fi.dy.masa.lowtechcrafting.inventory.container.ContainerCrafting;

public class AutoCraftingTableScreen extends AbstractContainerScreen<ContainerCrafting>// implements IRecipeShownListener
{
    protected final ContainerCrafting containerCrafting;
    protected ResourceLocation guiTexture;

    public AutoCraftingTableScreen(ContainerCrafting container, Inventory playerInv, Component title)
    {
        super(container, playerInv, title);

        this.containerCrafting = container;

        // Use the vanilla Dropper/Dispenser GUI texture
        this.guiTexture = new ResourceLocation("textures/gui/container/crafting_table.png");
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float gameTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.guiTexture);
        this.blit(matrixStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY)
    {
        String s = this.title.getString();
        this.font.draw(matrixStack, s, this.imageWidth / 2.0F - this.font.width(s) / 2.0F, 5, 0x404040);
        this.font.draw(matrixStack, this.playerInventoryTitle.getString(), 8, 73, 0x404040);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }
}

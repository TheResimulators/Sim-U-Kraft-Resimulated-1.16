package com.resimulators.simukraft.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.resimulators.simukraft.common.tileentity.TileMarker;
import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class MarkerEntityRender extends TileEntityRenderer<TileMarker> {
    //Block.makeCuboidShape(6,0,6,10,14,10);
    float yoffset = 0.875f;
    float xoffset = 0.875f;
    float zoffset = 0.625f;
    float width = 0.25f;
    float depth = 0.25f;
    public MarkerEntityRender(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TileMarker marker, float p_225616_2_, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(new ResourceLocation("minecraft","oak_log"));
        if (marker.getCorner() == TileMarker.Corner.ORIGIN){
        if (marker.getOrigin() != null && marker.getBackLeft() != null){
            if (marker.getOrigin().getZ() == marker.getBackLeft().getZ()) {


                matrixStack.push();
                float dx1 = (marker.getPos().getX() + (xoffset - width / 2));
                float dy1 = (marker.getPos().getY() + (yoffset - 2 / 16));
                float dz1 = (marker.getPos().getZ() + (zoffset - depth / 2));
                float dx2 = (marker.getBackLeft().getX() + xoffset - width / 2);
                float dy2 = (marker.getBackLeft().getY() + (yoffset - 2 / 16));
                float dz2 = (marker.getBackLeft().getZ() + (zoffset + depth / 2));
                IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());
                matrixStack.translate(dx1,dy1,dz1);
                add(builder, matrixStack, dx1, dy1, dz1, sprite.getMinU(), sprite.getMinV());
                add(builder, matrixStack, dx1, dy1 - 2 / 16, dz1, sprite.getMinU(), sprite.getMinV());
                add(builder, matrixStack, dx2, dy2 - 2 / 16, dz2, sprite.getMinU(), sprite.getMinV());
                add(builder, matrixStack, dx2, dy2, dz2, sprite.getMinU(), sprite.getMinV());
                add(builder, matrixStack, dx2, dy2, dz2, sprite.getMinU(), sprite.getMinV());
                add(builder, matrixStack, dx2, dy2, dz2, sprite.getMinU(), sprite.getMinV());
                add(builder, matrixStack, dx2, dy2, dz2, sprite.getMinU(), sprite.getMinV());
                add(builder, matrixStack, dx2, dy2, dz2, sprite.getMinU(), sprite.getMinV());
                matrixStack.pop();


            }
        }
    }
}
    private void add(IVertexBuilder renderer, MatrixStack stack, float x, float y, float z, float u, float v) {
        renderer.pos(stack.getLast().getMatrix(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .tex(u, v)
                .lightmap(0, 240)
                .normal(1, 0, 0)
                .endVertex();
    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.MARKER.get(), MarkerEntityRender::new);
    }

}

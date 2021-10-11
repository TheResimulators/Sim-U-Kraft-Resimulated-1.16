package com.resimulators.simukraft.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.resimulators.simukraft.common.tileentity.TileConstructor;
import com.resimulators.simukraft.init.ModTileEntities;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TileConstructorRenderer extends TileEntityRenderer<TileConstructor> {

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(ModTileEntities.CONSTRUCTOR.get(), TileConstructorRenderer::new);
    }


    public TileConstructorRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }
    @Override
    public void render(TileConstructor constructor, float p_225616_2_, MatrixStack stack, IRenderTypeBuffer typeBuffer, int p_225616_5_, int p_225616_6_) {
        if (constructor.isShouldRender()){
            if (constructor.getOrigin() != null && constructor.getCornerPosition() != null) {
                IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                IVertexBuilder builder = buffer.getBuffer(CustomRenderType.OVERLAY_LINES);  //SpellRender.QUADS is a personal RenderType, of VertexFormat POSITION_COLOR.

                stack.pushPose();
                Vector3d pointA = new Vector3d(constructor.getOrigin().getX(), constructor.getOrigin().getY(), constructor.getOrigin().getZ());
                Vector3d pointB = new Vector3d(constructor.getCornerPosition().getX(),constructor.getCornerPosition().getY(),constructor.getCornerPosition().getZ());
                double lowX;
                double lowY;
                double lowZ;
                double highX;
                double highY;
                double highZ;

                if (pointA.x > pointB.x) {
                    lowX = pointB.x;
                    highX = pointA.x;
                } else {
                    lowX = pointA.x;
                    highX = pointB.x;
                }
                if (pointA.y > pointB.y) {
                    lowY = pointB.y;
                    highY = pointA.y;
                } else {
                    lowY = pointA.y;
                    highY = pointB.y;
                }
                if (pointA.z > pointB.z) {
                    lowZ = pointB.z;
                    highZ = pointA.z;
                } else {
                    lowZ = pointA.z;
                    highZ = pointB.z;
                }



                pointA = new Vector3d(lowX, lowY, lowZ);
                pointB = new Vector3d(highX, highY, highZ);
                pointA = pointA.add(-0.01, -0.01, -0.01);
                double dx = Math.abs(pointA.x - pointB.x) + 1.01;
                double dy = Math.abs(pointA.y - pointB.y) + 1.01;
                double dz = Math.abs(pointA.z - pointB.z) + 1.01;
                Vector3d color = new Vector3d(255,255,255);
                stack.translate(-constructor.getBlockPos().getX(), -constructor.getBlockPos().getY(), -constructor.getBlockPos().getZ());
                Matrix4f mat = stack.last().pose();
                /** builder.vertex(mat, (float) pointA.x, (float) pointA.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
                 builder.vertex(mat, (float) pointA.x, (float) pointA.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

                 builder.vertex(mat, (float) pointA.x, (float) pointA.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
                 builder.vertex(mat, (float) pointA.x, (float) pointB.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

                 builder.vertex(mat, (float) pointA.x, (float) pointA.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
                 builder.vertex(mat, (float) pointB.x, (float) pointA.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

                 builder.vertex(mat, (float) pointA.x, (float) pointB.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
                 builder.vertex(mat, (float) pointB.x, (float) pointB.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

                 builder.vertex(mat, (float) pointA.x, (float) pointB.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
                 builder.vertex(mat, (float) pointA.x, (float) pointB.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

                 builder.vertex(mat, (float) pointA.x, (float) pointB.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
                 builder.vertex(mat, (float) pointA.x, (float) pointA.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

                 builder.vertex(mat, (float) pointB.x, (float) pointB.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
                 builder.vertex(mat, (float) pointA.x, (float) pointB.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

                 builder.vertex(mat, (float) pointB.x, (float) pointB.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
                 builder.vertex(mat, (float) pointB.x, (float) pointA.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

                 builder.vertex(mat, (float) pointB.x, (float) pointB.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
                 builder.vertex(mat, (float) pointB.x, (float) pointB.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

                 builder.vertex(mat, (float) pointB.x, (float) pointA.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
                 builder.vertex(mat, (float) pointB.x, (float) pointB.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

                 builder.vertex(mat, (float) pointB.x, (float) pointA.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
                 builder.vertex(mat, (float) pointB.x, (float) pointA.y, (float) pointA.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();

                 builder.vertex(mat, (float) pointB.x, (float) pointA.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
                 builder.vertex(mat, (float) pointA.x, (float) pointA.y, (float) pointB.z).color(0, (int) color.x, (int) color.y, (int) color.z).endVertex();
                 */
                //AB
                builder.vertex(mat,(float)pointA.x, (float)pointA.y, (float)pointA.z).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();          //A
                builder.vertex(mat,(float)pointA.x, (float)pointA.y, (float)(pointA.z+dz)).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();       //B
                //BC
                builder.vertex(mat,(float)pointA.x, (float)pointA.y, (float)(pointA.z+dz)).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();       //B
                builder.vertex(mat,(float)(pointA.x+dx), (float)pointA.y, (float)(pointA.z+dz)).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();    //C
                //CD
                builder.vertex(mat,(float)(pointA.x+dx), (float)pointA.y, (float)(pointA.z+dz)).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();    //C
                builder.vertex(mat,(float)(pointA.x+dx), (float)pointA.y, (float)pointA.z).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();       //D
                //DA
                builder.vertex(mat,(float)(pointA.x+dx), (float)pointA.y, (float)pointA.z).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();       //D
                builder.vertex(mat,(float)pointA.x, (float)pointA.y, (float)pointA.z).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();          //A
                //EF
                builder.vertex(mat,(float)pointA.x, (float)(pointA.y+dy), (float)pointA.z).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();       //E
                builder.vertex(mat,(float)pointA.x, (float)(pointA.y+dy), (float)(pointA.z+dz)).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();    //F
                //FG
                builder.vertex(mat,(float)pointA.x, (float)(pointA.y+dy), (float)(pointA.z+dz)).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();    //F
                builder.vertex(mat,(float)(pointA.x+dx), (float)(pointA.y+dy), (float)(pointA.z+dz)).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex(); //G
                //GH
                builder.vertex(mat,(float)(pointA.x+dx), (float)(pointA.y+dy), (float)(pointA.z+dz)).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex(); //G
                builder.vertex(mat,(float)(pointA.x+dx), (float)(pointA.y+dy), (float)pointA.z).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();    //H
                //HE
                builder.vertex(mat,(float)(pointA.x+dx), (float)(pointA.y+dy), (float)pointA.z).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();    //H
                builder.vertex(mat,(float)pointA.x, (float)(pointA.y+dy), (float)pointA.z).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();       //E
                //AE
                builder.vertex(mat,(float)pointA.x, (float)pointA.y, (float)pointA.z).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();          //A
                builder.vertex(mat,(float)pointA.x, (float)(pointA.y+dy), (float)pointA.z).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();       //E
                //BF
                builder.vertex(mat,(float)pointA.x, (float)pointA.y, (float)(pointA.z+dz)).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();       //B
                builder.vertex(mat,(float)pointA.x, (float)(pointA.y+dy), (float)(pointA.z+dz)).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();    //F
                //CG
                builder.vertex(mat,(float)(pointA.x+dx), (float)pointA.y, (float)(pointA.z+dz)).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();    //C
                builder.vertex(mat,(float)(pointA.x+dx), (float)(pointA.y+dy), (float)(pointA.z+dz)).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex(); //G
                //DH
                builder.vertex(mat,(float)(pointA.x+dx), (float)pointA.y, (float)pointA.z).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();       //D
                builder.vertex(mat,(float)(pointA.x+dx), (float)(pointA.y+dy), (float)pointA.z).color(0,(int)color.x, (int)color.y, (int)color.y).endVertex();    //H

                stack.popPose();
                buffer.endBatch(RenderType.lines());

            }
        }
    }
    @Override
    public boolean shouldRenderOffScreen(TileConstructor p_188185_1_) {
        return true;
    }

}


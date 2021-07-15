package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.Network;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.tileentity.TileResidential;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.packets.RequestHouseOccupantsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.awt.*;
import java.util.ArrayList;


public class GuiResidential extends Screen {

    private final Faction faction;
    private final TileResidential tile;
    private final BlockPos pos;
    private String name;
    private Button Done;
    private ArrayList<Integer> occupants;

    protected GuiResidential(ITextComponent titleIn, BlockPos pos) {
        super(titleIn);
        faction = SavedWorldData.get(SimuKraft.proxy.getClientWorld()).getFactionWithPlayer(SimuKraft.proxy.getClientPlayer().getUUID());
        tile = (TileResidential) SimuKraft.proxy.getClientWorld().getBlockEntity(pos);
        this.pos = pos;
        if (tile != null) {
            if (tile.getHouseID() != null) {
                name = faction.getHouseByID(tile.getHouseID()).getName();
            }
        }
        if (name == null) {
            name = "Under Construction";
        }
        Network.getNetwork().sendToServer(new RequestHouseOccupantsPacket(pos, SimuKraft.proxy.getClientPlayer().getUUID()));
    }


    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        font.draw(stack, getTitle().getString(), (width / 2) - font.width(getTitle().getString()) / 2, height / 6 - 20, Color.WHITE.getRGB());
        font.draw(stack, "Name: " + name.replace("_", " "), 30, height / 2 - 50, Color.WHITE.getRGB());
        font.draw(stack, "Occupants: ", 30, height / 2 - 30, Color.WHITE.getRGB());
        if (occupants != null) {
            int i = 0;
            for (int id : occupants) {
                SimEntity sim = (SimEntity) SimuKraft.proxy.getClientWorld().getEntity(id);
                font.draw(stack, sim.getCustomName().getString(), 40, height / 2 - 10 + i * 20, Color.WHITE.getRGB());
            }
        }
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);

        addButton(Done = new Button(width - 120, height - 30, 100, 20, new StringTextComponent("Done"), Done -> {
            minecraft.setScreen(null);
        }));
    }


    public void setOccupants(ArrayList<Integer> ids) {
        this.occupants = ids;

    }
}

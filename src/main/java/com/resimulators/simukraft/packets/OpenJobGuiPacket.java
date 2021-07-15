package com.resimulators.simukraft.packets;

import com.resimulators.simukraft.client.gui.GuiHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class OpenJobGuiPacket implements IMessage {
    private ArrayList<Integer> ints;
    private BlockPos pos;
    private int id;
    private int guiId;
    private String string;

    public OpenJobGuiPacket(ArrayList<Integer> ints, BlockPos pos, int guiId, String string) {
        this.pos = pos;
        this.ints = ints;
        this.guiId = guiId;
        this.string = string;
    }

    public OpenJobGuiPacket(ArrayList<Integer> ints, BlockPos pos, int id, int guiId, String string) {
        this.pos = pos;
        this.ints = ints;
        this.id = id;
        this.guiId = guiId;
        this.string = string;
    }


    public OpenJobGuiPacket() {
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(guiId);
        buf.writeInt(id);
        buf.writeBlockPos(pos);
        buf.writeInt(ints.size());
        for (int id : ints) {
            buf.writeInt(id);
        }
        buf.writeUtf(string);

    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        guiId = buf.readInt();
        id = buf.readInt();
        pos = buf.readBlockPos();
        ArrayList<Integer> ids = new ArrayList<>();
        int length = buf.readInt();
        for (int i = 0; i < length; i++) {
            ids.add(buf.readInt());
        }
        ints = ids;
        string = buf.readUtf();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        GuiHandler.openGui(ints, pos, id, guiId, string);
    }
}

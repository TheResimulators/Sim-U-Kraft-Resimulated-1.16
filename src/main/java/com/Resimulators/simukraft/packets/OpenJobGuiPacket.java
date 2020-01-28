package com.Resimulators.simukraft.packets;

import com.Resimulators.simukraft.client.gui.BaseJobGui;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.Supplier;

public class OpenJobGuiPacket implements IMessage {
    private ArrayList<Integer> ints;
    private BlockPos pos;
    private int id;
    public OpenJobGuiPacket(ArrayList<Integer> ints, BlockPos pos){
        this.pos = pos;
        this.ints = ints;
    }
    public OpenJobGuiPacket(ArrayList<Integer> ints, BlockPos pos,int id){
        this.pos = pos;
        this.ints = ints;
        this.id = id;
    }
    public OpenJobGuiPacket(){}

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(id);
        buf.writeBlockPos(pos);
        buf.writeInt(ints.size());
        for(int id:ints){
            buf.writeInt(id);
    }
}

    @Override
    public void fromBytes(PacketBuffer buf) {
        id = buf.readInt();
        pos = buf.readBlockPos();
        ArrayList<Integer> ids = new ArrayList<>();
        int length = buf.readInt();
        for(int i = 0;i<length;i++){
            ids.add(buf.readInt());
        }
        ints =ids;
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        Minecraft.getInstance().displayGuiScreen(new BaseJobGui(new StringTextComponent("Base"),ints,pos,id));
    }
}

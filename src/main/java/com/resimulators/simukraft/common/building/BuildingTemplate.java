package com.resimulators.simukraft.common.building;

import com.google.common.collect.ImmutableList;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.tileentity.TileCustomData;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import sun.awt.windows.WPrinterJob;

import java.util.*;
import java.util.concurrent.BlockingDeque;

public class BuildingTemplate extends Template {
    private BlockPos controlBlock = BlockPos.ZERO;
    private float rent; // if applicable
    private float cost;
    private int typeID;
    private BlockPos offSet = BlockPos.ZERO;
    private Mirror mirror = Mirror.NONE;
    private String name = "";
    private Direction direction;
    private Rotation blockRotation = Rotation.NONE;
    HashMap<Item,Integer> items = new HashMap<>();


    public BuildingTemplate() {
    }

    @Override
    public void fillFromWorld(World worldIn, BlockPos startPos, BlockPos size, boolean takeEntities, Block toIgnore) {
        super.fillFromWorld(worldIn, startPos, size, takeEntities, toIgnore);

    }

    @Override
    public List<BlockInfo> filterBlocks(BlockPos p_215381_1_, PlacementSettings p_215381_2_, Block p_215381_3_) {
        return super.filterBlocks(p_215381_1_, p_215381_2_, p_215381_3_);
    }

    @Override
    public List<BlockInfo> filterBlocks(BlockPos p_215386_1_, PlacementSettings p_215386_2_, Block p_215386_3_, boolean p_215386_4_) {
        return super.filterBlocks(p_215386_1_, p_215386_2_, p_215386_3_, p_215386_4_);
    }

    @Override
    public BlockPos calculateConnectedPosition(PlacementSettings placementIn, BlockPos p_186262_2_, PlacementSettings p_186262_3_, BlockPos p_186262_4_) {
        return super.calculateConnectedPosition(placementIn, p_186262_2_, p_186262_3_, p_186262_4_);
    }

    @Override
    public void placeInWorldChunk(IServerWorld p_237144_1_, BlockPos p_237144_2_, PlacementSettings p_237144_3_, Random p_237144_4_) {
        super.placeInWorldChunk(p_237144_1_, p_237144_2_, p_237144_3_, p_237144_4_);
    }

    @Override
    public void placeInWorld(IServerWorld p_237152_1_, BlockPos p_237152_2_, PlacementSettings p_237152_3_, Random p_237152_4_) {
        super.placeInWorld(p_237152_1_, p_237152_2_, p_237152_3_, p_237152_4_);
    }

    @Override
    public boolean placeInWorld(IServerWorld p_237146_1_, BlockPos p_237146_2_, BlockPos p_237146_3_, PlacementSettings p_237146_4_, Random p_237146_5_, int p_237146_6_) {
        return super.placeInWorld(p_237146_1_, p_237146_2_, p_237146_3_, p_237146_4_, p_237146_5_, p_237146_6_);
    }

    @Override
    public BlockPos getSize(Rotation rotationIn) {
        return super.getSize(rotationIn);
    }

    @Override
    public BlockPos getZeroPositionWithTransform(BlockPos p_189961_1_, Mirror p_189961_2_, Rotation p_189961_3_) {
        return super.getZeroPositionWithTransform(p_189961_1_, p_189961_2_, p_189961_3_);
    }

    @Override
    public MutableBoundingBox getBoundingBox(PlacementSettings p_215388_1_, BlockPos p_215388_2_) {
        return super.getBoundingBox(p_215388_1_, p_215388_2_);
    }

    @Override
    public MutableBoundingBox getBoundingBox(BlockPos p_237150_1_, Rotation p_237150_2_, BlockPos p_237150_3_, Mirror p_237150_4_) {
        return super.getBoundingBox(p_237150_1_, p_237150_2_, p_237150_3_, p_237150_4_);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putFloat("rent", rent);
        nbt.putFloat("cost", cost);
        nbt.putInt("typeID", typeID);
        nbt.putInt("direction", direction.get2DDataValue());
        nbt.putString("name", name);
        nbt.putString("author", this.getAuthor());
        nbt.putLong("offset", offSet.asLong());
        nbt.putString("mirror", mirror.toString());
        return super.save(nbt);
    }

    @Override
    public void load(CompoundNBT compound) {
        rent = compound.getFloat("rent");
        cost = compound.getFloat("cost");
        typeID = compound.getInt("typeID");
        if (compound.contains("direction")) {
            direction = Direction.from2DDataValue(compound.getInt("direction"));
        } else {
            direction = Direction.from2DDataValue(2);
            blockRotation = Rotation.CLOCKWISE_180;

        }
        if (compound.contains("name")) {
            name = compound.getString("name");
        } else {
            name = "Placeholder";
        }
        if (compound.contains("offset")) {
            offSet = BlockPos.of(compound.getLong("offset"));
        } else {
            offSet = BlockPos.ZERO;
        }

        if (compound.contains("mirror")) {
            mirror = Mirror.valueOf(compound.getString("mirror"));
        } else {
            mirror = Mirror.LEFT_RIGHT;
        }
        setAuthor(compound.getString("author"));
        System.out.println(name);
        try{
            super.load(compound);
            setBlockList();
        }catch (ResourceLocationException e){
            SimuKraft.LOGGER().error("Invalid block in template: " + name + " skipping, please check for errors in the blocks");
            SimuKraft.LOGGER().error(e.getMessage());

        }

    }


    public List<Template.Palette> getBlocks() {
        return ObfuscationReflectionHelper.getPrivateValue(Template.class, this, "field_204769_a");
    }

    public List<Template.Palette> getEntities() {
        return ObfuscationReflectionHelper.getPrivateValue(Template.class, this, "field_186271_b");
    }

    public BlockPos getControlBlock() {
        return controlBlock;
    }

    public void setControlBlock(BlockPos controlBlock) {
        this.controlBlock = controlBlock;
    }

    public void findControlBox(World worldIn, BlockPos startPos, BlockPos size) {
        BlockPos blockpos = startPos.offset(size).offset(-1, -1, -1);
        BlockPos blockpos1 = new BlockPos(Math.min(startPos.getX(), blockpos.getX()), Math.min(startPos.getY(), blockpos.getY()), Math.min(startPos.getZ(), blockpos.getZ()));
        BlockPos blockpos2 = new BlockPos(Math.max(startPos.getX(), blockpos.getX()), Math.max(startPos.getY(), blockpos.getY()), Math.max(startPos.getZ(), blockpos.getZ()));

        for (BlockPos blockpos3 : BlockPos.betweenClosed(blockpos1, blockpos2)) {
            TileEntity entity = worldIn.getBlockEntity(blockpos3);
            if (worldIn.getBlockEntity(blockpos3) instanceof TileCustomData) {
                if (entity != null) {
                    TileCustomData data = (TileCustomData) entity;
                    setControlBlock(blockpos3);
                    rent = data.getRent();
                    cost = data.getPrice();
                    typeID = data.getBuildingType().id;
                    return;
                }
            }
        }
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction dir) {
        this.direction = dir;
    }

    public float getRent() {
        return rent;
    }

    public void setRent(float rent) {
        this.rent = rent;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

    }

    public BlockPos getOffSet() {
        return offSet;
    }

    public void setOffSet(BlockPos offset) {
        this.offSet = offset;
    }

    public Mirror getMirror() {
        return mirror;
    }

    public void setMirror(Mirror mirror) {
        this.mirror = mirror;
    }

    public Rotation getBlockRotation() {
        return blockRotation;
    }

    private void setBlockList(){
        if (getBlocks().size() > 0){
            Palette palette = getBlocks().get(0);
            for(BlockInfo info: palette.blocks()){
                Block block = info.state.getBlock();
                if (items.get(block.asItem()) == null){
                    items.put(block.asItem(),1);
                }else{
                    int amount = items.get(block.asItem());
                    items.put(block.asItem(),amount + 1);
                }
            }
        }
    }

    public HashMap<Item, Integer> getBlockList(){
        if (items.size() == 0) setBlockList();
        return items;
    }

    public int totalItemsNeeded(){
        HashMap<Item, Integer> items = getBlockList();
        int total = 0;
        for (Item item: items.keySet()){
            total += items.get(item);
        }
        return total;
    }

    public void setBlockList(HashMap<Item,Integer> items){
        this.items = items;
    }
}


package com.resimulators.simukraft.handlers;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.building.CustomTemplateManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

public class StructureHandler {
    private static CustomTemplateManager templateManager;

    public static void createTemplateManager(MinecraftServer server) {
        try {
            Field field = ObfuscationReflectionHelper.findField(MinecraftServer.class, "field_71310_m");
            field.setAccessible(true);
            SaveFormat.LevelSave levelSave = (SaveFormat.LevelSave) field.get(server);
            field.setAccessible(false);
            templateManager = new CustomTemplateManager(server.getDataPackRegistries().getResourceManager(), levelSave, server.getFixerUpper());
        } catch (IllegalAccessException e) {
            //FIXME fabbe50: Make own instance of TemplateManager
            templateManager = (CustomTemplateManager) server.overworld().getStructureManager(); // gets servers template manager
        }
    }
    /**gets instance of template manager*/
    public static CustomTemplateManager getTemplateManager() {
        return templateManager;
    }
    /**saves the structure to file using built-in template manager*/
    public static boolean saveStructure(World world, BlockPos origin,BlockPos min, BlockPos size, String name, String author, Direction dir) {
        if (templateManager == null && world.getServer() != null)
            templateManager = (CustomTemplateManager) world.getServer().overworld().getStructureManager();

        if (templateManager != null) {
            BuildingTemplate template = templateManager.getOrCreate(new ResourceLocation(Reference.MODID, name)); //gets default empty template
            template.fillFromWorld(world, origin, size, false, null); //gets all the blocks that are in the world
            template.setAuthor(author); //sets the author to the person saving the structure
            template.findControlBox(world,origin,size);
            template.setDirection(dir);
            template.setName(name);
//            Vector3i xAxis;
//            Vector3i zAxis;
//            if (dir.getAxis() == Direction.Axis.X){
//                xAxis = dir.getNormal();
//                zAxis= dir.getClockWise().getNormal();
//            }else{
//                xAxis = dir.getClockWise().getNormal();
//                zAxis = dir.getNormal();
//            }
//            BlockPos offset = calculateOffset(size, xAxis,zAxis);
            BlockPos offset = min.subtract(origin);
            System.out.println(offset);

            System.out.println(size);
            template.setOffSet(offset);
            template.setMirror(Mirror.NONE);
            templateManager.save(new ResourceLocation(Reference.MODID, name)); // writes the template to file at given location
            return true; // returns true if successful
        }
        return false;
    }
    /**loads structure using template manager*/
    public static BuildingTemplate loadStructure(String name) {
        if (templateManager != null) {
            System.out.println(name);
            return templateManager.get(new ResourceLocation(Reference.MODID, name));
        }
        return null;
    }

    public static List<Template.BlockInfo> modifyAndConvertTemplate(BuildingTemplate template, World world, BlockPos pos,PlacementSettings settings) {
            List<Template.Palette> blockInfos = template.getBlocks();
            return BuildingTemplate.processBlockInfos(world, pos, pos, settings, blockInfos.get(0).blocks(), template);
    }

    public static BlockPos calculateOffset(BlockPos size, Vector3i xAxis, Vector3i zAxis){
        int xOffset = (size.getX()-1)*xAxis.getX();
        int zOffset = (size.getZ()-1)*zAxis.getZ();

        return new BlockPos(xOffset,0,zOffset);
    }
}

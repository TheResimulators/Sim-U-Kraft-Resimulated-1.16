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
            templateManager = new CustomTemplateManager(server.getDataPackRegistries().getResourceManager(), levelSave, server.getDataFixer());
        } catch (IllegalAccessException e) {
            //FIXME fabbe50: Make own instance of TemplateManager
            templateManager = (CustomTemplateManager) server.func_241755_D_().getStructureTemplateManager(); // gets servers template manager
        }
    }
    /**gets instance of template manager*/
    public static CustomTemplateManager getTemplateManager() {
        return templateManager;
    }
    /**saves the structure to file using built-in template manager*/
    public static boolean saveStructure(World world, BlockPos origin, BlockPos size, String name, String author, Direction dir) {
        if (templateManager == null && world.getServer() != null)
            templateManager = (CustomTemplateManager) world.getServer().func_241755_D_().getStructureTemplateManager();

        if (templateManager != null) {
            BuildingTemplate template = templateManager.getTemplateDefaulted(new ResourceLocation(Reference.MODID, name)); //gets default empty template
            template.takeBlocksFromWorld(world, origin, size, false, null); //gets all the blocks that are in the world
            template.setAuthor(author); //sets the author to the person saving the structure
            template.findControlBox(world,origin,size);
            template.setDirection(dir);
            template.setName(name);
            templateManager.writeToFile(new ResourceLocation(Reference.MODID, name)); // writes the template to file at given location
            return true; // returns true if successful
        }
        return false;
    }
    /**loads structure using template manager*/
    public static BuildingTemplate loadStructure(String name) {
        if (templateManager != null) {
            return templateManager.getTemplate(new ResourceLocation(Reference.MODID, name));
        }
        return null;
    }

    public static List<Template.BlockInfo> modifyAndConvertTemplate(BuildingTemplate template, World world, BlockPos pos,PlacementSettings settings) {
            List<Template.Palette> blockInfos = template.getBlocks();
            return BuildingTemplate.processBlockInfos(world, pos, pos, settings, blockInfos.get(0).func_237157_a_(), template);
    }
}

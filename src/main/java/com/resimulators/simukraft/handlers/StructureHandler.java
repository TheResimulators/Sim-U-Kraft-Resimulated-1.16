package com.resimulators.simukraft.handlers;

import com.resimulators.simukraft.Reference;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.storage.SaveFormat;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

public class StructureHandler {
    private static TemplateManager templateManager;

    public static void createTemplateManager(MinecraftServer server) {
        try {
            Field field = server.getClass().getDeclaredField("anvilConverterForAnvilFile");
            field.setAccessible(true);
            SaveFormat.LevelSave levelSave = (SaveFormat.LevelSave) field.get(server);
            field.setAccessible(false);
            templateManager = new TemplateManager(server.getDataPackRegistries().getResourceManager(), levelSave, server.getDataFixer());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            //FIXME fabbe50: Make own instance of TemplateManager
            templateManager = server.func_241755_D_().getStructureTemplateManager(); // gets servers template manager
        }
    }
    /**gets instance of template manager*/
    public static TemplateManager getTemplateManager() {
        return templateManager;
    }
    /**saves the structure to file using built-in template manager*/
    public static boolean saveStructure(World world, BlockPos origin, BlockPos size, String name, String author) {
        if (templateManager == null && world.getServer() != null)
            templateManager = world.getServer().func_241755_D_().getStructureTemplateManager();

        if (templateManager != null) {
            Template template = templateManager.getTemplateDefaulted(new ResourceLocation(Reference.MODID, name)); //gets default empty template
            template.takeBlocksFromWorld(world, origin, size, false, null); //gets all the blocks that are in the world
            template.setAuthor(author); //sets the author to the person saving the structure
            templateManager.writeToFile(new ResourceLocation(Reference.MODID, name)); // writes the template to file at given location
            return true; // returns true if successful
        }
        return false;
    }
    /**loads structure using template manager*/
    public static Template loadStructure(String name) {
        if (templateManager != null) {
            return templateManager.getTemplate(new ResourceLocation(Reference.MODID, name));
        }
        return null;
    }

    public static List<Template.BlockInfo> modifyAndConvertTemplate(Template template, World world, BlockPos pos, Rotation rotation, Mirror mirror) {
        try {
            Field field = template.getClass().getDeclaredField("blocks");
            field.setAccessible(true);
            List<List<Template.BlockInfo>> blockInfos = (List<List<Template.BlockInfo>>)field.get(template);
            field.setAccessible(false);
            return Template.processBlockInfos(world, pos, pos, new PlacementSettings().setRotation(rotation).setMirror(mirror), blockInfos.get(0), template);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            try {
                Field field = template.getClass().getDeclaredField("field_204769_a "); //TODO (fabbe50): Check if this is the correct field name. Aidie8: found the new field name i think using Forge Bot
                field.setAccessible(true);
                List<List<Template.BlockInfo>> blockInfos = (List<List<Template.BlockInfo>>)field.get(template);
                field.setAccessible(false);
                return Template.processBlockInfos(world, pos, pos, new PlacementSettings().setRotation(rotation).setMirror(mirror), blockInfos.get(0), template);
            } catch (NoSuchFieldException | IllegalAccessException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }
}

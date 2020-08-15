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
            templateManager = new TemplateManager(server.getDataPackRegistries().func_240970_h_(), levelSave, server.getDataFixer());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            //FIXME fabbe50: Make own instance of TemplateManager
            templateManager = server.func_241755_D_().getStructureTemplateManager();
        }
    }

    public static TemplateManager getTemplateManager() {
        return templateManager;
    }

    public static void saveStructure(World world, BlockPos origin, BlockPos size, String name, String author) {
        if (templateManager == null && world.getServer() != null)
            templateManager = world.getServer().func_241755_D_().getStructureTemplateManager();

        if (templateManager != null) {
            Template template = templateManager.getTemplateDefaulted(new ResourceLocation(Reference.MODID, name));
            template.takeBlocksFromWorld(world, origin, size, false, null);
            template.setAuthor(author);
            templateManager.writeToFile(new ResourceLocation(Reference.MODID, name));
        }
    }

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
                Field field = template.getClass().getDeclaredField("field_199719_a"); //TODO (fabbe50): Check if this is the correct field name.
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

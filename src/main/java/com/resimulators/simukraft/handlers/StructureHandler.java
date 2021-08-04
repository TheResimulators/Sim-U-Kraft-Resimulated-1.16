package com.resimulators.simukraft.handlers;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.building.CustomTemplateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

public class StructureHandler {
    public static void createTemplateManager() {
        CustomTemplateManager.initCustomTemplateManager(Minecraft.getInstance().getResourceManager(), Minecraft.getInstance().getFixerUpper());
    }

    /**saves the structure to file using built-in template manager*/
    public static boolean saveStructure(World world, BlockPos origin,BlockPos min, BlockPos size, String name, String author, Direction dir) {
        if (CustomTemplateManager.isInitialized()) {
            BuildingTemplate template = CustomTemplateManager.getOrCreate(new ResourceLocation(Reference.MODID, name)); //gets default empty template
            template.fillFromWorld(world, min, size, false, null); //gets all the blocks that are in the world
            template.setAuthor(author); //sets the author to the person saving the structure
            template.findControlBox(world,min,size);
            template.setDirection(dir);
            template.setName(name);
            BlockPos offset = min.subtract(origin);
            template.setOffSet(offset);
            template.setMirror(Mirror.NONE);
            CustomTemplateManager.save(new ResourceLocation(Reference.MODID, name)); // writes the template to file at given location
            return true; // returns true if successful
        }
        return false;
    }
    /**loads structure using template manager*/
    public static BuildingTemplate loadStructure(String name) {
        if (CustomTemplateManager.isInitialized()) {
            return CustomTemplateManager.get(new ResourceLocation(Reference.MODID, name));
        }
        return null;
    }

    public static List<Template.BlockInfo> modifyAndConvertTemplate(BuildingTemplate template, World world, BlockPos pos,PlacementSettings settings) {
            List<Template.Palette> blockInfos = template.getBlocks();
            return BuildingTemplate.processBlockInfos(world, pos, pos, settings, blockInfos.get(0).blocks(), template);
    }

}

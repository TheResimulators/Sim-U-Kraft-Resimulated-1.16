package com.resimulators.simukraft.common.building;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.common.enums.Category;
import com.resimulators.simukraft.handlers.StructureHandler;
import net.minecraft.crash.ReportedException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.FileUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.datafix.DefaultTypeReferences;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomTemplateManager {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, BuildingTemplate> templates = Maps.newHashMap();
    private static DataFixer fixer;
    private static IResourceManager resourceManager;
    private static Path pathGenerated;
    private static Category category;
    private static boolean initialized = false;

    public static void initCustomTemplateManager(IResourceManager p_i232119_1_, DataFixer p_i232119_3_) {
        resourceManager = p_i232119_1_;
        fixer = p_i232119_3_;
        pathGenerated = new File(".", "resources").toPath().normalize();
        try {
            Files.createDirectories(Files.exists(pathGenerated) ? pathGenerated.toRealPath() : pathGenerated);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initialized = true;
        getAllBuildingTemplates();
    }

    public static ArrayList<BuildingTemplate> getAllBuildingTemplates() {
        List<ResourceLocation> locations = getAllTemplates();
        ArrayList<BuildingTemplate> templates = new ArrayList<>();
        for (ResourceLocation location : locations) {
            String name = location.getPath().replace(".nbt", "");
            BuildingTemplate template = StructureHandler.loadStructure(name);
            if (template != null) {
                templates.add(template);
            } else {
                SimuKraft.LOGGER().warn("Structure with name " + name + " is missing or corrupted and could not be loaded," +
                        " please check if it is in the right location and that it is a valid structure");
            }
        }
        return templates;
    }

    public static List<ResourceLocation> getAllTemplates() {
        Path path = pathGenerated.resolve(Reference.MODID + "/structures");
        ArrayList<File> folders = Arrays.stream(Objects.requireNonNull(path.toFile().listFiles()))
                .filter(File::isDirectory)
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<ResourceLocation> structures;
        structures = Arrays.stream(Objects.requireNonNull(path.toFile().listFiles()))
                .filter(file -> !file.isDirectory())
                .map(resource -> {
                    try
                    {
                        return new ResourceLocation(Reference.MODID, resource.getName());
                    } catch(ResourceLocationException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
        folders.forEach(folder -> structures.addAll(Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                .filter(file -> !file.isDirectory())
                .map(resource -> {
                    try
                    {
                        return new ResourceLocation(Reference.MODID, resource.getName());
                    } catch(ResourceLocationException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).filter(Objects::nonNull)
                .collect(Collectors.toList())));

        return structures;
    }

    public static BuildingTemplate getOrCreate(ResourceLocation p_200220_1_) {
        BuildingTemplate template = get(p_200220_1_);
        if (template == null) {
            template = new BuildingTemplate();
            templates.put(p_200220_1_, template);
        }

        return template;
    }

    @Nullable
    public static BuildingTemplate get(ResourceLocation p_200219_1_) {
        return templates.computeIfAbsent(p_200219_1_, (p_209204_1_) -> {
            BuildingTemplate template = loadTemplateFile(p_209204_1_);
            return template != null ? template : loadTemplateResource(p_209204_1_);
        });
    }

    public static void onResourceManagerReload(IResourceManager resourceManager) {
        CustomTemplateManager.resourceManager = resourceManager;
        templates.clear();
    }

    public static BuildingTemplate readStructure(CompoundNBT p_227458_1_) {
        if (!p_227458_1_.contains("DataVersion", 99)) {
            p_227458_1_.putInt("DataVersion", 500);
        }

        BuildingTemplate template = new BuildingTemplate();
        template.load(NBTUtil.update(fixer, DefaultTypeReferences.STRUCTURE, p_227458_1_, p_227458_1_.getInt("DataVersion")));
        return template;
    }

    public static boolean save(ResourceLocation templateName) {
        BuildingTemplate template = templates.get(templateName);
        if (template == null) {
            return false;
        } else {

            BuildingType type = BuildingType.getById(template.getTypeID());
            if (type != null) {
                category = type.category; // can't figure out how to add this so it puts it into a folder for each type of building
            } else {
                category = Category.SPECIAL;
            }

            Path path = resolvePath(templateName, ".nbt");
            Path path1 = path.getParent();
            if (path1 == null) {
                return false;
            } else {

                try {
                    Files.createDirectories(Files.exists(path1) ? path1.toRealPath() : path1);
                } catch (IOException ioexception) {
                    LOGGER.error("Failed to create parent directory: {}", path1);
                    return false;
                }
                CompoundNBT compoundnbt = template.save(new CompoundNBT());

                try (OutputStream outputstream = new FileOutputStream(path.toFile())) {
                    CompressedStreamTools.writeCompressed(compoundnbt, outputstream);
                    return true;
                } catch (Throwable throwable) {
                    return false;
                }
            }
        }
    }

    private static Path resolvePath(ResourceLocation locationIn, String extIn) {
        if (locationIn.getPath().contains("//")) {
            throw new ResourceLocationException("Invalid resource path: " + locationIn);
        } else {
            Path path = createPathToStructure(locationIn, extIn);
            if (path.startsWith(pathGenerated) && FileUtil.isPathNormalized(path) && FileUtil.isPathPortable(path)) {
                return path;
            } else {
                throw new ResourceLocationException("Invalid resource path: " + path);
            }
        }
    }

    public static Path createPathToStructure(ResourceLocation locationIn, String extIn) {
        try {
            Path path = pathGenerated.resolve(locationIn.getNamespace());
            Path path1 = path.resolve("structures");
            for (Category category : Category.values()) {
                Path path2 = path1.resolve(category.category);
                if (Files.exists(path2.resolve(locationIn.getPath() + ".nbt"))) {
                    path1 = path2;
                    break;
                }
            }
            return FileUtil.createPathToResource(path1, locationIn.getPath(), extIn);
        } catch (InvalidPathException invalidpathexception) {
            throw new ResourceLocationException("Invalid resource path: " + locationIn, invalidpathexception);
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void setInitialized(boolean inited) {
        initialized = inited;
    }

    public static void remove(ResourceLocation templatePath) {
        templates.remove(templatePath);
    }

    @Nullable
    private static BuildingTemplate loadTemplateResource(ResourceLocation p_209201_1_) {
        ResourceLocation resourcelocation = new ResourceLocation(p_209201_1_.getNamespace(), "structures/" + p_209201_1_.getPath() + ".nbt");

        try (IResource iresource = resourceManager.getResource(resourcelocation)) {
            return loadTemplate(iresource.getInputStream());
        } catch (FileNotFoundException filenotfoundexception) {
            return null;
        } catch (Throwable throwable) {
            LOGGER.error("Couldn't load structure {}: {}", p_209201_1_, throwable.toString());
            return null;
        }
    }

    @Nullable
    private static BuildingTemplate loadTemplateFile(ResourceLocation locationIn) {
        if (!pathGenerated.toFile().isDirectory()) {
            SimuKraft.LOGGER().warn("Direction to Folder does not exist");
            return null;
        } else {
            Path path = resolvePath(locationIn, ".nbt");

            try (InputStream inputstream = new FileInputStream(path.toFile())) {
                return loadTemplate(inputstream);
            } catch (FileNotFoundException filenotfoundexception) {
                SimuKraft.LOGGER().error("File not found: " + filenotfoundexception.getMessage());
                return null;
            } catch (IOException ioexception) {
                LOGGER.error("Couldn't load structure from {}", path, ioexception);
                return null;
            }
        }
    }

    private static BuildingTemplate loadTemplate(InputStream inputStreamIn) throws IOException {
        try {
            CompoundNBT compoundnbt = CompressedStreamTools.readCompressed(inputStreamIn);
            return readStructure(compoundnbt);
        }catch (ReportedException e){
            System.out.println("building could not be loaded");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
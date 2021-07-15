package com.resimulators.simukraft.common.building;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.common.enums.Category;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.FileUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomTemplateManager extends TemplateManager {

    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<ResourceLocation, BuildingTemplate> templates = Maps.newHashMap();
    private final DataFixer fixer;
    private final Path pathGenerated;
    private IResourceManager resourceManager;
    private Category category;


    public CustomTemplateManager(IResourceManager p_i232119_1_, SaveFormat.LevelSave p_i232119_2_, DataFixer p_i232119_3_) {
        super(p_i232119_1_, p_i232119_2_, p_i232119_3_);
        this.resourceManager = p_i232119_1_;
        this.fixer = p_i232119_3_;
        //this.pathGenerated = p_i232119_2_.resolveFilePath(FolderName.GENERATED).normalize();
        this.pathGenerated = new File(ServerLifecycleHooks.getCurrentServer().getServerDirectory(), "resources").toPath().normalize();
        try {
            Files.createDirectories(Files.exists(pathGenerated) ? this.pathGenerated.toRealPath() : this.pathGenerated);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BuildingTemplate getOrCreate(ResourceLocation p_200220_1_) {
        BuildingTemplate template = this.get(p_200220_1_);
        if (template == null) {
            template = new BuildingTemplate();
            this.templates.put(p_200220_1_, template);
        }

        return template;
    }

    @Nullable
    public BuildingTemplate get(ResourceLocation p_200219_1_) {
        return this.templates.computeIfAbsent(p_200219_1_, (p_209204_1_) -> {
            BuildingTemplate template = this.loadTemplateFile(p_209204_1_);
            return template != null ? template : this.loadTemplateResource(p_209204_1_);
        });
    }

    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        this.templates.clear();
    }

    public BuildingTemplate readStructure(CompoundNBT p_227458_1_) {
        if (!p_227458_1_.contains("DataVersion", 99)) {
            p_227458_1_.putInt("DataVersion", 500);
        }

        BuildingTemplate template = new BuildingTemplate();
        template.load(NBTUtil.update(this.fixer, DefaultTypeReferences.STRUCTURE, p_227458_1_, p_227458_1_.getInt("DataVersion")));
        return template;
    }

    public boolean save(ResourceLocation templateName) {
        BuildingTemplate template = this.templates.get(templateName);
        if (template == null) {
            return false;
        } else {

            BuildingType type = BuildingType.getById(template.getTypeID());
            if (type != null) {
                category = type.category; // can't figure out how to add this so it puts it into a folder for each type of building
            } else {
                category = Category.SPECIAL;
            }

            Path path = this.resolvePath(templateName, ".nbt");
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

    public Path createPathToStructure(ResourceLocation locationIn, String extIn) {
        try {
            Path path = this.pathGenerated.resolve(locationIn.getNamespace());
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

    public void remove(ResourceLocation templatePath) {
        this.templates.remove(templatePath);
    }

    private Path resolvePath(ResourceLocation locationIn, String extIn) {
        if (locationIn.getPath().contains("//")) {
            throw new ResourceLocationException("Invalid resource path: " + locationIn);
        } else {
            Path path = this.createPathToStructure(locationIn, extIn);
            if (path.startsWith(this.pathGenerated) && FileUtil.isPathNormalized(path) && FileUtil.isPathPortable(path)) {
                return path;
            } else {
                throw new ResourceLocationException("Invalid resource path: " + path);
            }
        }
    }

    @Nullable
    private BuildingTemplate loadTemplateResource(ResourceLocation p_209201_1_) {
        ResourceLocation resourcelocation = new ResourceLocation(p_209201_1_.getNamespace(), "structures/" + p_209201_1_.getPath() + ".nbt");

        try (IResource iresource = this.resourceManager.getResource(resourcelocation)) {
            return this.loadTemplate(iresource.getInputStream());
        } catch (FileNotFoundException filenotfoundexception) {
            return null;
        } catch (Throwable throwable) {
            LOGGER.error("Couldn't load structure {}: {}", p_209201_1_, throwable.toString());
            return null;
        }
    }

    @Nullable
    private BuildingTemplate loadTemplateFile(ResourceLocation locationIn) {
        if (!this.pathGenerated.toFile().isDirectory()) {
            return null;
        } else {
            Path path = this.resolvePath(locationIn, ".nbt");

            try (InputStream inputstream = new FileInputStream(path.toFile())) {
                return this.loadTemplate(inputstream);
            } catch (FileNotFoundException filenotfoundexception) {
                return null;
            } catch (IOException ioexception) {
                LOGGER.error("Couldn't load structure from {}", path, ioexception);
                return null;
            }
        }
    }

    private BuildingTemplate loadTemplate(InputStream inputStreamIn) throws IOException {
        CompoundNBT compoundnbt = CompressedStreamTools.readCompressed(inputStreamIn);
        return this.readStructure(compoundnbt);
    }

    public List<ResourceLocation> getAllTemplates() {
        Path path = pathGenerated.resolve(Reference.MODID + "/structures");
        ArrayList<File> folders = Arrays.stream(Objects.requireNonNull(path.toFile().listFiles()))
                .filter(File::isDirectory)
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<ResourceLocation> structures;
        structures = Arrays.stream(Objects.requireNonNull(path.toFile().listFiles()))
                .filter(file -> !file.isDirectory())
                .map(e -> new ResourceLocation(Reference.MODID, e.getName()))
                .collect(Collectors.toCollection(ArrayList::new));
        folders.forEach(folder -> structures.addAll(Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                .filter(file -> !file.isDirectory())
                .map(e -> new ResourceLocation(Reference.MODID, e.getName()))
                .collect(Collectors.toList())));

        return structures;
    }
}
package com.resimulators.simukraft.utils;

import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.SimuKraft;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TextureUtils {
    public static List<ResourceLocation> getAllFilesInFolder1(String folderLocation) throws URISyntaxException, IOException {
        FileSystem fileSystem;
        final String DEFAULT_PART_OF_PATH = "/assets/simukraft/";
        String stringUrl = DEFAULT_PART_OF_PATH + folderLocation;
        URL url = SimuKraft.class.getResource(stringUrl);
        SimuKraft.LOGGER().debug("URL of Resource = " + url);
        System.out.println("URL of Resource = " + url);
        if (url != null) {
            URI uri = url.toURI();
            Path path;
            SimuKraft.LOGGER().debug("Scheme of Resource = " + url);
            System.out.println("Scheme of Resource = " + url);
            if ("file".equals(uri.getScheme())) {
                path = Paths.get(SimuKraft.class.getResource(stringUrl).toURI());

            } else {
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                path = fileSystem.getPath("/assets/simukraft/textures/entity/sim/female");
            }
            return Arrays.stream(Objects.requireNonNull(path.toFile().list())).map(e -> new ResourceLocation(Reference.MODID, folderLocation + e)).collect(Collectors.toList());
        }
        return null;
    }

}

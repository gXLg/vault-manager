package dev.gxlg.vaultmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("CanBeFinal")
public class Memory {

    public Map<String, Set<String>> vaults = new HashMap<>();

    private static final Path memoryPath = FabricLoader.getInstance().getConfigDir().resolve("vaultmanager.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Memory init() {
        Memory memory;
        if (Files.notExists(memoryPath)) {
            try {
                Files.createFile(memoryPath);
            } catch (IOException e) {
                throw new RuntimeException("Could not initialize config", e);
            }
            memory = new Memory();
        } else {
            try (FileReader reader = new FileReader(memoryPath.toFile())) {
                memory = GSON.fromJson(reader, Memory.class);
            } catch (IOException e) {
                throw new RuntimeException("Could not parse config", e);
            }
        }
        memory.save();
        return memory;
    }

    public void save() {
        Path dir = memoryPath.getParent();
        try {
            if (Files.notExists(dir)) {
                Files.createDirectory(dir);
            } else if (!Files.isDirectory(dir)) {
                throw new IOException("Not a directory: " + dir);
            }

            Path tempPath = memoryPath.resolveSibling(memoryPath.getFileName() + ".tmp");
            Files.createFile(tempPath);
            Files.write(tempPath, GSON.toJson(this).getBytes(), StandardOpenOption.WRITE);
            Files.move(tempPath, memoryPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Could not save config", e);
        }
    }
}
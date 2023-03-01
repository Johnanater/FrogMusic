package com.johnanater.frogmusic;

import net.minecraft.src.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FrogMusicLoader
{
    private final Path musicDir = new File("mods/frogmusic/").toPath();

    private final J_JdomParser JDOM_PARSER = new J_JdomParser();

    public List<CustomMusic> customMusicList = new ArrayList<>();
    public List<Integer> allRecords = new ArrayList<>();

    public void loadAllMusic()
    {
        checkFolder();

        try
        {
            Files.list(musicDir)
                .forEach(path ->
                {
                    try
                    {
                        loadFolder(path);
                    }
                    catch (Exception ex)
                    {
                        System.out.println("Failed to load folder " + path.getFileName() + "!");
                        ex.printStackTrace();
                    }
                });
        }
        catch (Exception ex)
        {
            System.out.println("Failed to load folders!");
            ex.printStackTrace();
        }

        customMusicList.forEach(c ->
        {
            if (c.type.equals("record"))
                loadRecord(c);
        });

        try
        {
            if (mod_FrogMusic.overrideCreepers)
                overrideCreeper();
        }
        catch (Exception ex)
        {
            System.out.println("Failed to override creeper!");
            ex.printStackTrace();
        }
    }

    public void loadRecord(CustomMusic customMusic)
    {
        try
        {
            Constructor<ItemRecord> constructor = ItemRecord.class.getDeclaredConstructor(int.class, String.class);
            constructor.setAccessible(true);
            ItemRecord newRecord = constructor.newInstance(customMusic.itemId, customMusic.itemName.toLowerCase());
            newRecord.setItemName(customMusic.itemName);
            newRecord.setIconIndex(ModLoader.addOverride("/gui/items.png", customMusic.textureFile));
            ModLoader.AddName(newRecord, customMusic.itemName);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to load record!");
            ex.printStackTrace();
        }

    }

    public void loadFolder(Path path) throws IOException, J_InvalidSyntaxException
    {
        // Read and parse the JSON file
        String json = new String(Files.readAllBytes(Paths.get(path.toAbsolutePath() + "/music.json")), StandardCharsets.UTF_8);

        // func_27367_a = parse
        J_JsonNode jsonNode = JDOM_PARSER.func_27367_a(json);
        // func_27213_a = getStringValue
        String type = jsonNode.func_27213_a("type");
        String itemName = jsonNode.func_27213_a("itemName");
        int itemId = Integer.parseInt(jsonNode.func_27213_a("itemId"));
        String soundFile = jsonNode.func_27213_a("soundFile");
        String soundPath = jsonNode.func_27213_a("soundPath");
        String texturePath = jsonNode.func_27213_a("texturePath");

        CustomMusic customMusic = new CustomMusic(type, itemName, itemId, soundFile, soundPath, texturePath);

        String loc = musicDir + "/" + path.getFileName() + soundPath + soundFile;
        File file = new File(loc);

        // Load the ogg/mus file
        ModLoader.getMinecraftInstance().sndManager.addStreaming(soundFile, file.getCanonicalFile());

        // Load the texture file
        customMusic.textureFile = "/" + path.getFileName() + texturePath;

        customMusicList.add(customMusic);
    }

    public void checkFolder()
    {
        if (!Files.exists(musicDir))
        {
            try
            {
                Files.createDirectory(musicDir);
            }
            catch (IOException ex)
            {
                System.out.println("Failed to check Music Directory!");
                ex.printStackTrace();
            }
        }
    }

    // Override EntityCreeper with EntityCreeperOverride
    public void overrideCreeper() throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        // Get all the fields with reflection
        HashMap<String, Class> stringToClassMapping = (HashMap<String, Class>) getPrivateValue(EntityList.class, null, "a", "stringToClassMapping");
        HashMap<Class, String> classToStringMapping = (HashMap<Class, String>) getPrivateValue(EntityList.class, null, "b", "classToStringMapping");
        HashMap<Integer, Class> IDtoClassMapping = (HashMap<Integer, Class>) getPrivateValue(EntityList.class, null, "c", "IDtoClassMapping");
        HashMap<Class, Integer> classToIDMapping = (HashMap<Class, Integer>) getPrivateValue(EntityList.class, null, "d", "classToIDMapping");

        // Remove vanilla Creepers from EntityList
        stringToClassMapping.remove("Creeper");
        classToStringMapping.remove(EntityCreeper.class);
        IDtoClassMapping.remove(50);
        classToIDMapping.remove(EntityCreeper.class);

        // Get the private method to re-add the Creeper override
        Method addMapping;

        try
        {
            addMapping = EntityList.class.getDeclaredMethod("a", Class.class, String.class, int.class);
        }
        catch (Exception ex)
        {
            try
            {
                addMapping = EntityList.class.getDeclaredMethod("addMapping", Class.class, String.class, int.class);
            }
            catch(Exception ex1)
            {
                throw new NoSuchMethodException("Failed to find method 'a' ('addMapping'!");
            }
        }

        // Re-add Creepers to EntityList
        addMapping.setAccessible(true);
        addMapping.invoke(null, EntityCreeperOverride.class, "Creeper", 50);

        // Remove Creeper from all biomes
        removeCreeper(BiomeGenBase.desert);
        removeCreeper(BiomeGenBase.forest);
        removeCreeper(BiomeGenBase.plains);
        removeCreeper(BiomeGenBase.rainforest);
        removeCreeper(BiomeGenBase.seasonalForest);
        removeCreeper(BiomeGenBase.taiga);
        removeCreeper(BiomeGenBase.iceDesert);
        removeCreeper(BiomeGenBase.tundra);
        removeCreeper(BiomeGenBase.hell);
        removeCreeper(BiomeGenBase.savanna);
        removeCreeper(BiomeGenBase.shrubland);
        removeCreeper(BiomeGenBase.swampland);
        removeCreeper(BiomeGenBase.sky);

        // Re-add the new Creeper to all biomes
        addCreeper(BiomeGenBase.desert);
        addCreeper(BiomeGenBase.forest);
        addCreeper(BiomeGenBase.plains);
        addCreeper(BiomeGenBase.rainforest);
        addCreeper(BiomeGenBase.seasonalForest);
        addCreeper(BiomeGenBase.taiga);
        addCreeper(BiomeGenBase.iceDesert);
        addCreeper(BiomeGenBase.tundra);
        addCreeper(BiomeGenBase.hell);
        addCreeper(BiomeGenBase.savanna);
        addCreeper(BiomeGenBase.shrubland);
        addCreeper(BiomeGenBase.swampland);
        addCreeper(BiomeGenBase.sky);

        // Add original records
        allRecords.add(Item.record13.shiftedIndex);
        allRecords.add(Item.recordCat.shiftedIndex);

        // Add custom records
        customMusicList.forEach(c -> {
            allRecords.add((c.itemId + 256));
        });
    }

    public void removeCreeper(BiomeGenBase biome) throws NoSuchFieldException
    {
        ArrayList<SpawnListEntry> spawnableMonsterList = (ArrayList<SpawnListEntry>) getPrivateValue(BiomeGenBase.class, biome, "s", "spawnableMonsterList");

        spawnableMonsterList.removeIf(s -> s.entityClass.equals(EntityCreeper.class));
    }

    public void addCreeper(BiomeGenBase biome) throws NoSuchFieldException
    {
        ArrayList<SpawnListEntry> spawnableMonsterList = (ArrayList<SpawnListEntry>) getPrivateValue(BiomeGenBase.class, biome, "s", "spawnableMonsterList");

        spawnableMonsterList.add(new SpawnListEntry(EntityCreeperOverride.class, 10));
    }

    public Object getPrivateValue(Class theClass, Object instance, String obfField, String deobfField) throws NoSuchFieldException
    {
        try
        {
            return ModLoader.getPrivateValue(theClass, instance, obfField);
        }
        catch (Exception ex)
        {
            try
            {
                return ModLoader.getPrivateValue(theClass, instance, deobfField);
            }
            catch (Exception ex1)
            {
                throw new NoSuchFieldException("Failed to find field " + obfField + " (" + deobfField + ")!");
            }
        }
    }
}

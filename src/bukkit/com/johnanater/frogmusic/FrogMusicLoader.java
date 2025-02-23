package com.johnanater.frogmusic;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.saj.InvalidSyntaxException;
import net.minecraft.server.*;

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

    private final JdomParser JDOM_PARSER = new JdomParser();

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
            newRecord.a(customMusic.itemName); // a = setItemName
        }
        catch (Exception ex)
        {
            System.out.println("Failed to load record!");
            ex.printStackTrace();
        }

    }

    public void loadFolder(Path path) throws IOException, InvalidSyntaxException
    {
        // Read and parse the JSON file
        String json = new String(Files.readAllBytes(Paths.get(path.toAbsolutePath() + "/music.json")), StandardCharsets.UTF_8);

        JsonNode jsonNode = JDOM_PARSER.parse(json);
        String type = jsonNode.getStringValue("type");
        String itemName = jsonNode.getStringValue("itemName");
        int itemId = Integer.parseInt(jsonNode.getStringValue("itemId"));
        String soundFile = jsonNode.getStringValue("soundFile");
        String soundPath = jsonNode.getStringValue("soundPath");
        String texturePath = jsonNode.getStringValue("texturePath");

        CustomMusic customMusic = new CustomMusic(type, itemName, itemId, soundFile, soundPath, texturePath);

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
        HashMap<String, Class> stringToClassMapping = (HashMap<String, Class>) getPrivateValue(EntityTypes.class, null, "a", "stringToClassMapping");
        HashMap<Class, String> classToStringMapping = (HashMap<Class, String>) getPrivateValue(EntityTypes.class, null, "b", "classToStringMapping");
        HashMap<Integer, Class> IDtoClassMapping = (HashMap<Integer, Class>) getPrivateValue(EntityTypes.class, null, "c", "IDtoClassMapping");
        HashMap<Class, Integer> classToIDMapping = (HashMap<Class, Integer>) getPrivateValue(EntityTypes.class, null, "d", "classToIDMapping");

        // Remove vanilla Creepers from EntityList
        stringToClassMapping.remove("Creeper");
        classToStringMapping.remove(EntityCreeper.class);
        IDtoClassMapping.remove(50);
        classToIDMapping.remove(EntityCreeper.class);

        // Get the private method to re-add the Creeper override
        Method addMapping;

        try
        {
            addMapping = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
        }
        catch (Exception ex)
        {
            try
            {
                addMapping = EntityTypes.class.getDeclaredMethod("addMapping", Class.class, String.class, int.class);
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
        removeCreeper(BiomeBase.DESERT);
        removeCreeper(BiomeBase.FOREST);
        removeCreeper(BiomeBase.PLAINS);
        removeCreeper(BiomeBase.RAINFOREST);
        removeCreeper(BiomeBase.SEASONAL_FOREST);
        removeCreeper(BiomeBase.TAIGA);
        removeCreeper(BiomeBase.ICE_DESERT);
        removeCreeper(BiomeBase.TUNDRA);
        removeCreeper(BiomeBase.SAVANNA);
        removeCreeper(BiomeBase.SHRUBLAND);
        removeCreeper(BiomeBase.SWAMPLAND);

        // Re-add the new Creeper to all biomes
        addCreeper(BiomeBase.DESERT);
        addCreeper(BiomeBase.FOREST);
        addCreeper(BiomeBase.PLAINS);
        addCreeper(BiomeBase.RAINFOREST);
        addCreeper(BiomeBase.SEASONAL_FOREST);
        addCreeper(BiomeBase.TAIGA);
        addCreeper(BiomeBase.ICE_DESERT);
        addCreeper(BiomeBase.TUNDRA);
        addCreeper(BiomeBase.SAVANNA);
        addCreeper(BiomeBase.SHRUBLAND);
        addCreeper(BiomeBase.SWAMPLAND);

        // Add original records
        allRecords.add(Item.GOLD_RECORD.id);
        allRecords.add(Item.GREEN_RECORD.id);

        // Add custom records
        customMusicList.forEach(c -> {
            allRecords.add((c.itemId + 256));
        });
    }

    public void removeCreeper(BiomeBase biome) throws NoSuchFieldException
    {
        ArrayList<BiomeMeta> spawnableMonsterList = (ArrayList<BiomeMeta>) getPrivateValue(BiomeBase.class, biome, "s", "spawnableMonsterList");

        spawnableMonsterList.removeIf(s -> s.a.equals(EntityCreeper.class)); // a = entityClass
    }

    public void addCreeper(BiomeBase biome) throws NoSuchFieldException
    {
        ArrayList<BiomeMeta> spawnableMonsterList = (ArrayList<BiomeMeta>) getPrivateValue(BiomeBase.class, biome, "s", "spawnableMonsterList");

        spawnableMonsterList.add(new BiomeMeta(EntityCreeperOverride.class, 10));
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

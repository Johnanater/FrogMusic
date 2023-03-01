package com.johnanater.frogmusic;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.saj.InvalidSyntaxException;
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
            newRecord.setItemName(customMusic.itemName);
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
        removeCreeper(BiomeGenBase.field_28054_m); // field_28054_m = sky

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
        addCreeper(BiomeGenBase.field_28054_m); // field_28054_m = sky

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

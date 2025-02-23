package net.minecraft.src;

import com.johnanater.frogmusic.FrogMusicLoader;

public class mod_FrogMusic extends BaseModMp
{
    public static FrogMusicLoader frogMusicLoader;

    public mod_FrogMusic()
    {
        frogMusicLoader = new FrogMusicLoader();
        frogMusicLoader.loadAllMusic();
    }

    public String Name()
    {
        return "FrogMusic";
    }

    public String Description()
    {
        return "Add your own custom records!";
    }

    @Override
    public String Version()
    {
        return "1.0.2";
    }

    @MLProp(name = "Override Creepers",
            info = "Override creepers so that they can drop custom records.")
    public static boolean overrideCreepers = true;
}

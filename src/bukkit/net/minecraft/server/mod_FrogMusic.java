package net.minecraft.server;

import com.johnanater.frogmusic.FrogMusicLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

public class mod_FrogMusic extends BaseModMp
{
    public static FrogMusicLoader frogMusicLoader;

    public mod_FrogMusic() throws IOException
    {
        loadArgo();

        frogMusicLoader = new FrogMusicLoader();
        frogMusicLoader.loadAllMusic();
    }

    private void loadArgo() throws IOException
    {
        File file = new File("lib/argo-small-6.0.jar");

        if (!file.exists())
        {
            File dir = new File("lib");

            if (!dir.exists())
                dir.mkdir();

            System.out.println("Downloading Argo 6.0...");
            URL url = new URL("https://sourceforge.net/projects/argo/files/argo/6.0/argo-small-6.0.jar");
            try (InputStream in = url.openStream())
            {
                Files.copy(in, file.toPath());
            }
            catch (Exception ex)
            {
                System.out.println("Failed to download Argo 6.0!");
                ex.printStackTrace();
            }
        }

        addClassPath(file.toURI().toURL());
    }

    // from: https://bukkit.org/threads/tutorial-use-external-library-s-with-your-plugin.103781/
    private void addClassPath(final URL url) throws IOException
    {
        final URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        try
        {
            final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);
        }
        catch (final Throwable t)
        {
            t.printStackTrace();
            throw new IOException("Error adding " + url + " to system classloader!");
        }
    }

    @Override
    public String Version()
    {
        return "1.0.0";
    }

    @MLProp(name = "Override Creepers",
            info = "Override creepers so that they can drop custom records.")
    public static boolean overrideCreepers = true;
}

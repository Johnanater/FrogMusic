package com.johnanater.frogmusic;

import net.minecraft.src.*;

public class EntityCreeperOverride extends EntityCreeper
{
    public EntityCreeperOverride(World world)
    {
        super(world);
    }

    @Override
    public void onDeath(Entity entity)
    {
        if(entity instanceof EntitySkeleton)
        {
            int randomRecord = mod_FrogMusic.frogMusicLoader.allRecords.get(rand.nextInt(mod_FrogMusic.frogMusicLoader.allRecords.size()));
            this.dropItem(randomRecord, 1);
        }

        // Copy-pasted from EntityLiving's onDeath
        // Avoids calling EntityCreeper's onDeath
        if(this.scoreValue >= 0 && entity != null)
        {
            entity.addToPlayerScore(this, this.scoreValue);
        }

        if(entity != null)
        {
            entity.onKillEntity(this);
        }

        this.unused_flag = true;
        if(!this.worldObj.multiplayerWorld)
        {
            this.dropFewItems();
        }

        this.worldObj.func_9425_a(this, (byte) 3);
        // End copy-paste
    }
}

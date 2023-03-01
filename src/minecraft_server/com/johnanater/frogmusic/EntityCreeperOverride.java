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
            entity.func_27010_a(this); // func_27010_a = onKillEntity
        }

        this.unused_flag = true;
        if(!this.worldObj.singleplayerWorld)
        {
            this.dropFewItems();
        }

        this.worldObj.sendTrackedEntityStatusUpdatePacket(this, (byte) 3); // sendTrackedEntityStatusUpdatePacket = func_9425_a (client)
        // End copy-paste
    }
}

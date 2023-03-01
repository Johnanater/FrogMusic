package com.johnanater.frogmusic;

import net.minecraft.server.*;

public class EntityCreeperOverride extends EntityCreeper
{
    public EntityCreeperOverride(World world)
    {
        super(world);
    }

    @Override
    public void die(Entity entity) // die = onDeath
    {
        // CraftBukkit's patch passes the arrow as the entity
        // parameter instead of the shooter like Vanilla does
        if(entity instanceof EntityArrow)
        {
            EntityArrow entityArrow = (EntityArrow) entity;

            if (entityArrow.shooter instanceof EntitySkeleton)
            {
                int randomRecord = mod_FrogMusic.frogMusicLoader.allRecords.get(random.nextInt(mod_FrogMusic.frogMusicLoader.allRecords.size()));
                this.b(randomRecord, 1); // b = dropItem
            }
        }

        // Copy-pasted from EntityLiving's onDeath
        // Avoids calling EntityCreeper's onDeath
        if (this.W >= 0 && entity != null) // W = scoreValue
        {
            entity.c(this, this.W); // c = addToPlayerScore, W = scoreValue
        }

        if (entity != null)
        {
            entity.a(this); // a = onKillEntity = func_27010_a
        }

        this.ak = true; // ak = unused
        if (!this.world.isStatic)
        {
            this.q(); // q = dropFewItems
        }

        this.world.a(this, (byte)3); // a = sendTrackedEntityStatusUpdatePacket = func_9425_a
        // End copy-paste
    }
}

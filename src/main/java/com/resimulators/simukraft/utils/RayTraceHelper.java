package com.resimulators.simukraft.utils;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

import java.util.List;

public class RayTraceHelper {
    public static final RayTraceHelper INSTANCE = new RayTraceHelper();
    private RayTraceResult target = null;
    private Minecraft minecraft = Minecraft.getInstance();

    private RayTraceHelper() {}

    public void ray() {
        if (minecraft.objectMouseOver != null && minecraft.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
            this.target = minecraft.objectMouseOver;
            return;
        } else
            this.target = null;

        Entity entity = minecraft.getRenderViewEntity();
        if (entity == null)
            return;
    }

    public RayTraceResult getTarget() {
        return target;
    }

    public Entity getTargetEntity() {
        return target.getType() == RayTraceResult.Type.ENTITY ? getIdentifierEntity() : null;
    }

    public Entity getIdentifierEntity() {
        if (this.target == null || this.target.getType() != RayTraceResult.Type.ENTITY)
            return null;

        List<Entity> entities = Lists.newArrayList();

        Entity entity = ((EntityRayTraceResult) target).getEntity();

        return entities.size() > 0 ? entities.get(0) : entity;
    }

    public void reset() {
        this.target = null;
    }
}

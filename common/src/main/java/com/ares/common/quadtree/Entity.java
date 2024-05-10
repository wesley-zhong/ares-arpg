package com.ares.common.quadtree;


import com.ares.common.math.AABB;
import com.ares.common.math.Circle;

import java.util.Random;
import java.util.logging.Logger;


public class Entity<T> implements Value {
    private static final Logger logger = Logger.getLogger(Entity.class.getName());
    private AABB aabb;
    private long uid;
    private int x, y;
    private T obj;
    private static int index = 0;

    public Entity(int x, int z, int width, int height) {
        aabb = new AABB(x, z, width, height);
        uid = System.currentTimeMillis() + new Random().nextInt();
        this.x = x;
        this.y = z;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }
    public T getObj(){
        return this.obj;
    }

    @Override
    public AABB getAABB() {
        return aabb;
    }

    @Override
    public long getFlag() {
        return uid / 2;
    }

    @Override
    public long getUniqueId() {
        return uid;
    }

    @Override
    public boolean intersect(Circle circle) {
        Circle selfCircle = new Circle(x, y, 20);
        boolean intersect = selfCircle.intersect(circle);
        index++;
        //  logger.info("-----entity id = " +uid +"  "+intersect +"  count = " +index);
        return intersect;
    }

    @Override
    public boolean getDeleteFlag() {
        return false;
    }


    @Override
    public boolean isContain(float x, float y) {
        return aabb.isContain(x, y);
    }

    @Override
    public void setDeleteFlag(boolean flag) {

    }
}

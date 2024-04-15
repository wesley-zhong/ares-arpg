package com.ares.common.quadtree;

interface Value {

    AABB getAABB();

    long getFlag();

    long getUniqueId();

    boolean intersect(Circle circle);

    boolean getDeleteFlag();

    boolean isContain(float x, float y);

    void setDeleteFlag(boolean flag);
}

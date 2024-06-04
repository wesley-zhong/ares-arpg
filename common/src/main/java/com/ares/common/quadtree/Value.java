package com.ares.common.quadtree;

import com.ares.common.math.AABB;
import com.ares.common.math.Circle;

interface Value {

    AABB getAABB();

    long getFlag();

    long getUniqueId();

    boolean intersect(Circle circle);

    boolean getDeleteFlag();

    boolean isContain(float x, float y);

    void setDeleteFlag(boolean flag);
}

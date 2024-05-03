package com.ares.common.math;

public class AABB {
    private float x = 0;
    private float y = 0;
    private float width = 0;
    private float height = 0;

    public AABB() {
    }

    public AABB(float x, float y, float width, float height) {
        init(x, y, width, height);
    }

    public void init(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void init(AABB other) {
        x = other.x;
        y = other.y;
        width = other.width;
        height = other.height;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setXY(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public float getLeft() {
        return x;
    }

    public float getRight() {
        return x + width;
    }

    public float getBottom() {
        return y;
    }

    public float getTop() {
        return y + height;
    }

    public float getCenterX() {
        return x + width * 0.5f;
    }

    public float getCenterY() {
        return y + height * 0.5f;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean intersect(AABB other) {
        return !(getRight() < other.getLeft() ||
                getLeft() > other.getRight() ||
                getTop() < other.getBottom() ||
                getBottom() > other.getTop());
    }

    public boolean isContain(AABB other) {
        return !(getLeft() > other.getLeft() ||
                getRight() < other.getRight() ||
                getBottom() > other.getBottom() ||
                getTop() < other.getTop());
    }

    public boolean isContain(float x, float y) {
        return x >= getLeft() && x <= getRight() &&
                y >= getBottom() && y <= getTop();
    }
}

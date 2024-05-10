package com.ares.common.math;

public class Circle {
    private float x = 0;
    private float y = 0;
    private float radius = 0;

    public Circle() {
    }

    public Circle(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setXY(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float distanceSqr(float x, float y) {
        float xDiff = this.x - x;
        float yDiff = this.y - y;
        return xDiff * xDiff + yDiff * yDiff;
    }

    public boolean isContain(float x, float y) {
        float distSqr = distanceSqr(x, y);
        float radiusSqr = radius * radius;
        return distSqr <= radiusSqr;
    }

    public boolean intersect(Circle other) {
        float distSqr = distanceSqr(other.x, other.y);
        float len = radius + other.radius;
        float lenSqr = len * len;
        return distSqr <= lenSqr;
    }
}

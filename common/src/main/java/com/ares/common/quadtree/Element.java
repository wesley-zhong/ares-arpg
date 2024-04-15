package com.ares.common.quadtree;

public class Element<T extends Value> {
    private Node<T> node = null;
    private T value;

    public Element(T value) {
        this.value = value;
    }

    public Node<T> getNode() {
        return node;
    }

    public void setNode(Node<T> node) {
        this.node = node;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public AABB getAABB() {
        if (value == null)
            return null;
        return value.getAABB();
    }

    public boolean isContain(float x, float y) {
        if (value == null)
            return false;
        return value.isContain(x, y);
    }

    public boolean intersect(Circle circle) {
        if (value == null)
            return false;
        return value.intersect(circle);
    }
}

package com.ares.common.quadtree;

import com.ares.common.math.AABB;

import java.util.ArrayList;
import java.util.List;

public class Node<T extends Value> {
    private int deep;
    private int routeElemCount;
    private AABB aabb;
    private Node<T> parent;
    private List<Node<T>> children = new ArrayList<>();
    private List<Element<T>> elems = new ArrayList<>();

    public Node(int deep, AABB aabb, Node<T> parent) {
        this.deep = deep;
        routeElemCount = 0;
        this.aabb = aabb;
        this.parent = parent;
    }

    public int getDeep() {
        return deep;
    }

    public boolean hasChild() {
        return !children.isEmpty();
    }

    public float getWidth() {
        return aabb.getWidth();
    }

    public float getHeight() {
        return aabb.getHeight();
    }

    public float getChildWidth() {
        if (children.isEmpty())
            return 0;
        return children.get(0).aabb.getWidth();
    }

    public float getChildHeight() {
        if (children.isEmpty())
            return 0;
        return children.get(0).aabb.getHeight();
    }

    public int getElemCount() {
        return elems.size();
    }

    public int getRouteElemCount() {
        return routeElemCount;
    }

    private void setRouteElemCount(boolean add) {
        Node<T> node = this;
        while (node != null) {
            if (add)
                ++node.routeElemCount;
            else
                --node.routeElemCount;
            node = node.parent;
        }
    }

    public AABB getAABB() {
        return aabb;
    }

    public Node<T> getParent() {
        return parent;
    }

    public void initChildren() {
        if (hasChild())
            return;
        float x = aabb.getLeft();
        float y = aabb.getBottom();
        float width = aabb.getWidth();
        float height = aabb.getHeight();
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;
        float midX = x + halfWidth;
        float midY = y + halfHeight;
        int deep = this.deep + 1;

        // TreeDefine.RIGHT_TOP
        AABB aabb = new AABB(midX, midY, halfWidth, halfHeight);
        children.add(new Node<T>(deep, aabb, this));

        // TreeDefine.LEFT_TOP
        aabb = new AABB(x, midY, halfWidth, halfHeight);
        children.add(new Node<T>(deep, aabb, this));

        // TreeDefine.LEFT_BOTTOM
        aabb = new AABB(x, y, halfWidth, halfHeight);
        children.add(new Node<T>(deep, aabb, this));

        // TreeDefine.RIGHT_BOTTOM
        aabb = new AABB(midX, y, halfWidth, halfHeight);
        children.add(new Node<T>(deep, aabb, this));
    }

    public Node<T> getChild(int pos) {
        if (pos < 0 || pos >= children.size())
            return null;
        return children.get(pos);
    }

    public boolean isContain(AABB aabb) {
        return this.aabb.isContain(aabb);
    }

    public boolean isContain(float x, float y) {
        return aabb.isContain(x, y);
    }

    public boolean intersect(AABB aabb) {
        return this.aabb.intersect(aabb);
    }

    public Element<T> getElem(int pos) {
        if (pos < 0 || pos >= elems.size())
            return null;
        return elems.get(pos);
    }

    public void addElem(Element<T> elem) {
        Node<T> node = elem.getNode();
        if (node == this)
            return;
        if (node != null)
            node.removeElem(elem);
        elem.setNode(this);
        elems.add(elem);
        setRouteElemCount(true);
    }

    public void removeElem(Element<T> elem) {
        Node<T> node = elem.getNode();
        if (node != this)
            return;
        for (int i = 0; i < elems.size(); ++i) {
            if (elems.get(i) == elem) {
                setRouteElemCount(false);
                elem.setNode(null);
                elems.remove(i);
                return;
            }
        }
        elem.setNode(null);
    }
}

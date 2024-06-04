package com.ares.common.quadtree;

import com.ares.common.math.AABB;
import com.ares.common.math.Circle;

import java.util.List;

public class Tree<T extends Value> {
    private int maxDeep = 0;
    private Node<T> root = null;

    public Tree() {
        this.maxDeep = 6;
    }

    public Tree(int maxDeep) {
        this.maxDeep = maxDeep;
    }

    public Tree(float x, float y, float width, float height) {
        this(x, y, width, height, 6);
    }

    public Tree(float x, float y, float width, float height, int maxDeep) {
        this.maxDeep = maxDeep;
        init(x, y, width, height);
    }

    public boolean isInited() {
        return root != null;
    }

    public void init(float x, float y, float width, float height) {
        clear();
        AABB aabb = new AABB(x, y, width, height);
        root = new Node<T>(1, aabb, null);
    }

    public void clear() {
        root = null;
    }

    boolean add(Node<T> node, Element<T> elem, int atDeep) {
        int curDeep = node.getDeep();
        AABB aabb = elem.getAABB();
        if (aabb == null || (!node.isContain(aabb) && curDeep > 1))
            return false;
        if (atDeep > 0 && atDeep == curDeep) {
            node.addElem(elem);
            return true;
        }
        float width = node.getWidth();
        float height = node.getHeight();
        float childWidth = width * 0.5f;
        float childHeight = height * 0.5f;
        if (childWidth < aabb.getWidth() ||
                childHeight < aabb.getHeight()) {
            node.addElem(elem);
            return true;
        }
        if (node.hasChild()) {
            for (int i = 0; i < TreeDefine.QT_NODE_COUNT; ++i) {
                Node<T> child = node.getChild(i);
                if (add(child, elem, atDeep))
                    return true;
            }
            node.addElem(elem);
            return true;
        }
        if (atDeep == 0) {
            if (curDeep >= maxDeep || node.getElemCount() < TreeDefine.QT_ELEM_MIN_SIZE) {
                node.addElem(elem);
                return true;
            }
        }

        node.initChildren();
        if (atDeep == 0) {
            for (int i = 0; i < TreeDefine.QT_NODE_COUNT; ++i) {
                Node<T> child = node.getChild(i);
                int elemSize = node.getElemCount();
                for (int j = elemSize - 1; j >= 0; --j) {
                    Element<T> e = node.getElem(j);
                    add(child, e, atDeep);
                }
            }
        }
        for (int i = 0; i < TreeDefine.QT_NODE_COUNT; ++i) {
            Node<T> child = node.getChild(i);
            if (add(child, elem, atDeep))
                return true;
        }

        node.addElem(elem);
        return true;
    }

    public boolean add(Element<T> elem, int atDeep) {
        if (root == null)
            return false;
        return add(root, elem, atDeep);
    }

    public Element<T> add(T value, int atDeep) {
        if (root == null)
            return null;
        Element<T> elem = new Element<T>(value);
        if (!add(root, elem, atDeep))
            return null;
        return elem;
    }

    public void remove(Element<T> elem) {
        if (elem == null)
            return;
        Node<T> node = (Node<T>) elem.getNode();
        if (node == null)
            return;
        node.removeElem(elem);
    }

    public void refresh(Element<T> elem) {
        Node<T> node = (Node<T>) elem.getNode();
        if (node == null || !node.isContain(elem.getAABB())) {
            add(elem, 0);
            return;
        }
        add(node, elem, 0);
    }

    public boolean intersect(float x, float y, List<T> output) {
        return intersect(x, y, output, false);
    }

    public boolean intersect(float x, float y, List<T> output, boolean getOne) {
        if (root == null)
            return false;
        return intersect(root, x, y, output, getOne);
    }

    public boolean intersect(Node<T> node, float x, float y, List<T> output) {
        return intersect(node, x, y, output, false);
    }

    public boolean intersect(Node<T> node, float x, float y, List<T> output, boolean getOne) {
        if (node.getRouteElemCount() == 0)
            return false;
        if (!node.isContain(x, y))
            return false;
        int elemCount = node.getElemCount();
        boolean hasValue = false;
        for (int i = 0; i < elemCount; ++i) {
            Element<T> elem = node.getElem(i);
            T value = elem.getValue();
            if (!value.isContain(x, y))
                continue;
            output.add(value);
            if (getOne)
                return true;
            hasValue = true;
        }
        if (!node.hasChild())
            return hasValue;
        for (int i = 0; i < TreeDefine.QT_NODE_COUNT; ++i) {
            Node<T> child = node.getChild(i);
            hasValue = intersect(child, x, y, output, getOne) || hasValue;
            if (getOne && hasValue)
                return true;
        }
        return hasValue;
    }

    public boolean intersect(Circle circle, List<T> output) {
        return intersect(circle, output, false);
    }

    public boolean intersect(Circle circle, List<T> output, boolean getOne) {
        if (root == null)
            return false;
        float radius = circle.getRadius();
        float len = radius * 2;
        AABB aabb = new AABB(circle.getX() - radius, circle.getY() - radius, len, len);
        return intersect(root, aabb, circle, output, getOne);
    }

    public boolean intersect(Node<T> node, AABB aabb, Circle circle, List<T> output) {
        return intersect(node, aabb, circle, output, false);
    }

    public boolean intersect(Node<T> node, AABB aabb, Circle circle, List<T> output, boolean getOne) {
        if (node.getRouteElemCount() == 0)
            return false;
        if (!node.intersect(aabb))
            return false;
        int elemCount = node.getElemCount();
        boolean hasValue = false;
        for (int i = 0; i < elemCount; ++i) {
            Element<T> elem = node.getElem(i);
            T value = elem.getValue();
            if (!value.intersect(circle))
                continue;
            output.add(value);
            if (getOne)
                return true;
            hasValue = true;
        }
        if (!node.hasChild())
            return hasValue;
        for (int i = 0; i < TreeDefine.QT_NODE_COUNT; ++i) {
            Node<T> child = node.getChild(i);
            hasValue = intersect(child, aabb, circle, output, getOne) || hasValue;
            if (getOne && hasValue)
                return true;
        }
        return hasValue;
    }
}

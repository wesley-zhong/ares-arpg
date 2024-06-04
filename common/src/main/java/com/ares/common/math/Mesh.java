package com.ares.common.math;

public class Mesh<T> {
    private final Class<T> gridClazz;
    private final int length;       // 长度
    private final int width;       // 宽度
    private final T[] gridArray;

    private Mesh(final Class<T> gridClazz, final int length, final int width) {
        this.gridClazz = gridClazz;
        this.length = length;
        this.width = width;
        this.gridArray = (T[]) new Object[length * width];
    }

    public static <T2> Mesh<T2> createMesh(Class<T2> gridClazz, int length, int width)
    {
        if (length <= 0 || width <= 0)
        {
            throw new IllegalArgumentException("Length and width must be greater than 0");
        }
        Mesh<T2> mesh = new Mesh<T2>(gridClazz, length, width);
        mesh.init();
        return mesh;
    }

    public T getGrid(int x, int y)
    {
        if (x >= length || x < 0 || y >= width || y < 0)
            return null;
        T val = gridArray[y * width + x];
        if (val == null)
        {
            try {
                val = gridClazz.newInstance();
                gridArray[y * width + x] = val;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return val;
    }

    public T findGrid(int x, int y)
    {
        if (x >= length || x < 0 || y >= width || y < 0)
            return null;
        return gridArray[y * width + x];
    }

    private void init()
    {
    }
}

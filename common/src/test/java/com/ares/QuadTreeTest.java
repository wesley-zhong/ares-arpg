package com.ares;

import com.ares.common.quadtree.Entity;
import com.ares.common.quadtree.Node;
import com.ares.common.quadtree.QuadTree;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;


import java.util.Random;
import java.util.logging.Logger;

@Slf4j
public class QuadTreeTest {
    private final QuadTree<Entity> quadTree = new QuadTree<>(7);
    private static final Logger logger = Logger.getLogger(QuadTreeTest.class.getName());
    private static final   int MAP_SIZE = 307200;


    @Before
    public void init() {
        quadTree.init(0, 0, MAP_SIZE, MAP_SIZE);
    }

    @Test
    public void testAdd() {
        Entity entity = new Entity(10, 10, 20, 20);
        quadTree.add(entity, 0);

        Entity entity1 = new Entity(50, 50, 20, 20);
        quadTree.add(entity1, 0);

        long[] intersect = quadTree.intersect(50, 50, 9, 0L);
        log.info("interSect ={}", intersect);

    }

    @Test
    public void testMultiAdd() {
        Random random = new Random();
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < 300000; i++) {
            int x = random.nextInt(MAP_SIZE);
            int y = random.nextInt(MAP_SIZE);
            Entity entity = new Entity(x, y, 256, 256);
            quadTree.add(entity, 0);
        }
        long end1 = System.currentTimeMillis();
        System.out.println("cost = " + (end1 - start1));

        Entity entity1 = new Entity(500, 500, 1000, 1000);
        quadTree.add(entity1, 0);
        //printNode(quadTree.getRoot());

        int count = 0;
        long start = System.currentTimeMillis();
        for(int i = 0 ; i < 10000 ; ++i) {
            long[] intersect = quadTree.intersect(600, 700, 1000, 0L);
            count = intersect.length;
         //   logger.info("-------- size = " + intersect.length);
        }

        long end = System.currentTimeMillis();
        logger.info("---  cost = " + (end -start) +" count = " + count);
    }

    private void printNode(Node<Entity> node){
        if(node == null){
            return;
        }
        int count = node.getElemCount();
        logger.info("================  level = " +node.getDeep() + " count = " + count);
        for(int i = 0 ; i < 4; i ++){
            Node<Entity> entityNode = node.getChild(i);
            printNode(entityNode);
        }
    }
}

package com.ares.game.service;

import com.ares.common.math.Circle;
import com.ares.common.quadtree.Entity;
import com.ares.common.quadtree.QuadTree;
import com.ares.game.bean.MonsterTestBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class QuadTreeTestService implements InitializingBean {
    private QuadTree<Entity<MonsterTestBean>> quadTree = new QuadTree<>(0, 0, 3007200, 3007200, 7);

    public List<Entity<MonsterTestBean>> intersect(int x, int y, int r) {
        Circle circle = new Circle(x, y, r);
        List<Entity<MonsterTestBean>> entities = new ArrayList<>();
        quadTree.intersect(circle, entities, 0);
        return entities;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Random random = new Random();
        for (int i = 0; i < 200000; ++i) {
            int x = random.nextInt(3007200);
            int z = random.nextInt(3007200);
            Entity<MonsterTestBean> entity = new Entity<>(x, z, 128, 128);
            MonsterTestBean monsterTestBean = new MonsterTestBean(i);
            entity.setObj(monsterTestBean);
            quadTree.add(entity, 0);
        }
    }
}

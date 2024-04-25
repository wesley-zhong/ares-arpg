package com.ares.dal.mongo;

import com.ares.dal.DO.CASDO;
import com.ares.dal.DO.CommDO;
import com.ares.dal.mongo.annotation.CollectionName;
import com.ares.dal.mongo.annotation.MdbName;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

@Slf4j
public class CommonMongoDAO<T extends CommDO> implements InitializingBean {
    private final static String _ID = "_id";
    private final static String _VER = "ver";
    private final Class<T> doClass;
    @Autowired
    private MongoClient mongoClient;
    protected MongoCollection<T> collection;

    private final static ReplaceOptions UP_INSERT_OPTIONS = new ReplaceOptions().upsert(true);

    public CommonMongoDAO(Class<T> doClass) {
        this.doClass = doClass;
    }

    public boolean insert(T obj) {
        InsertOneResult insertOneResult = collection.insertOne(obj);
        return insertOneResult.wasAcknowledged();
    }


    public boolean upInsert(T obj) {
        try {
            if (obj instanceof CASDO casObj) {
                long verEQ = casObj.getVer();
                casObj.setVer(casObj.getVer() + 1);
                UpdateResult updateResult = collection.replaceOne(and(eq(_ID, obj.getId()), eq(_VER, verEQ)), obj, UP_INSERT_OPTIONS);
                if (updateResult.getModifiedCount() != 1) {
                    log.error("####### replace id = {}, ver = {}, modify count = {}  ", obj.getId(), verEQ, updateResult.getModifiedCount());
                }
                return updateResult.wasAcknowledged();
            }

            UpdateResult updateResult = collection.replaceOne(eq(_ID, obj.getId()), obj, UP_INSERT_OPTIONS);
            return updateResult.wasAcknowledged();
        } catch (Exception e) {
            log.error("===== mongdb error", e);
        }
        return false;
    }

    public boolean bathInsert(List<T> objs) {
        InsertManyResult insertManyResult = collection.insertMany(objs);
        return insertManyResult.wasAcknowledged();
    }

    public long deleteMany(Bson condition) {
        return collection.deleteMany(condition).getDeletedCount();
    }

    public T getSingle(long key) {
        return collection.find(eq(_ID, key)).first();
    }

    public T getSingle(String key) {
        return collection.find(eq(_ID, key)).first();
    }

    public T getById(long key) {
        return collection.find(eq(_ID, key)).first();
    }

    public T getSingle() {
        return collection.find().first();
    }

    public T findOne(String fieldName, Object fieldValue) {
        return collection.find(eq(fieldName, fieldValue)).first();
    }

    public FindIterable<T> findMany(String fieldName, Object fieldValue) {
        return collection.find(eq(fieldName, fieldValue));
    }

    public FindIterable<T> findMany(Bson condition) {
        return collection.find(condition);
    }

    public FindIterable<T> getList(long key) {
        return collection.find(eq(_ID, key)).limit(30);
    }


    public long replaceOne(T obj) {
        String id = obj.getId();
        if (obj instanceof CASDO casObj) {
            long verEQ = casObj.getVer();
            casObj.setVer(casObj.getVer() + 1);
            UpdateResult updateResult = collection.replaceOne(and(eq(_ID, id), eq(_VER, verEQ)), obj);
            if (updateResult.getMatchedCount() != 1) {
                log.error("####### replace id = {}, ver = {}, modify count = {}  ", id, verEQ, updateResult.getModifiedCount());
            }
            return updateResult.getMatchedCount();
        }
        UpdateResult updateResult = collection.replaceOne(eq(_ID, id), obj);
        return updateResult.getModifiedCount();
    }


    public long delete(T obj) {
        String id = obj.getId();
        if (obj instanceof CASDO casObj) {
            long verEQ = casObj.getVer();
            DeleteResult deleteResult = collection.deleteOne(and(eq(_ID, id), eq(_VER, verEQ)));
            return deleteResult.getDeletedCount();
        }
        DeleteResult deleteResult = collection.deleteOne(eq(_ID, id));
        return deleteResult.getDeletedCount();
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // mongodb force operator
    public T findOneAndUpdate(Long key, String fieldName, Object value) {
        return collection.findOneAndUpdate(eq(_ID, key), combine(set(fieldName, value)));
    }

    public T findAndDelete(Long id) {
        return collection.findOneAndDelete(eq(_ID, id));
    }

    public long findReplace(String fieldName, String fieldValue, T obj) {
        UpdateResult updateResult = collection.replaceOne(eq(fieldName, fieldValue), obj);
        return updateResult.getModifiedCount();
    }
    /////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void afterPropertiesSet() {
        MdbName mdbName = this.doClass.getAnnotation(MdbName.class);
        String dbName;
        if (mdbName == null) {
            String tmpdbName = this.doClass.getSimpleName();
            dbName = tmpdbName.substring(0, tmpdbName.length() - 2);
        } else {
            dbName = mdbName.value();
        }
        MongoDatabase database = mongoClient.getDatabase(dbName);
        CollectionName collectionName = this.doClass.getAnnotation(CollectionName.class);
        String tableName;
        if (collectionName == null) {
            tableName = this.doClass.getSimpleName();
            tableName = tableName.substring(0, tableName.length() - 2);
        } else {
            tableName = collectionName.value();
        }
        tableName = tableName.toLowerCase();
        collection = database.getCollection(tableName, this.doClass);
    }
}

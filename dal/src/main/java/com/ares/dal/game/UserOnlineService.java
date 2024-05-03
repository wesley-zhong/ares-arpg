package com.ares.dal.game;


import com.ares.core.json.transcoder.JsonObjectMapper;
import com.ares.core.utils.JsonUtil;
import com.ares.dal.redis.RedisDAO;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UserOnlineService implements InitializingBean {
    @Autowired
    private RedisDAO redisDAO;
    private final static String USER_ONLINE_STATUS_LUA = " local strDataObj = redis.call('get',KEYS[1]); \n" +
            " if(strDataObj ==  false) then   \n" +
            "   local existObj = cjson.decode(ARGV[1]);  \n" +
            "   existObj.ver = 100  \n" +
            "   strDataObj=  cjson.encode(existObj)\n" +
            "   redis.call('set',KEYS[1],strDataObj)   \n" +
            " return strDataObj   \n" +
            " end  \n" +
            " local  existObj = cjson.decode(strDataObj);  \n" +
            " local  argObj = cjson.decode(ARGV[1])\n" +
            " \n" +
            " if(existObj.gmSrId == false) then \n" +
            "   existObj.gmSrId= argObj.gmSrId\n" +
            "  end\n" +
            " if(existObj.ver == argObj.ver) then\n" +
            "   existObj.gmSrId = argObj.gmSrId\n" +
            " end\n" +
            "  existObj.ver = existObj.ver + 1\n" +
            "  strDataObj = cjson.encode(existObj)\n" +
            "  redis.call('set',KEYS[1],strDataObj)  \n" +
            " return strDataObj\n" +
            " ";
    private String USER_ONLINE_STATUS_LUA_SHA;
    private String SET_USER_TEAM_LUA = "   \n" +
            " local strDataObj = redis.call('get',KEYS[1]); \n" +
            " if(strDataObj ==  false) then   \n" +
            " return  nil   \n" +
            " end  \n" +
            " local existObj = cjson.decode(strDataObj);  \n" +
            " if ARGV[1] == 1  then\n" +
            " existObj.tmSrId=ARGV[1]\n" +
            " end \n" +
            " if ARGV[1] ==2 then \n" +
            "  existObj.targetId=ARGV[1]\n" +
            " end\n" +
            " strDataObj = cjson.encode(existObj)\n" +
            " redis.call('set',KEYS[1],strDataObj)  \n" +
            " return strDataObj\n" +
            " ";
    private String SET_USER_TEAM_LUA_SHA;
    private static final String OLS = "ol:";


    /**
     * GTId : 直接替换
     * GsId  :需要判断 ：1. 如果版本号能对应，直接更新  2.  如果版本号不对应，GsId 有值的情况下不更新， 没值的情况下需要更新
     *
     * @param userOnlineStateDO
     * @return 修改后的 value
     */

    public UserOnlineStateDO setUserOnlineStatus(long uid, UserOnlineStateDO userOnlineStateDO) {
        String[] keys = new String[1];
        keys[0] = OLS + uid;
        String body = JsonUtil.toJsonString(userOnlineStateDO);
        String[] args = new String[1];
        args[0] = body;
        Object changedResult = redisDAO.evalsha(USER_ONLINE_STATUS_LUA_SHA, keys, args);
        String strUserOnlineStateDo = (String) ((List) changedResult).get(0);
        try {
            return JsonObjectMapper.getInstance().readValue(strUserOnlineStateDo, UserOnlineStateDO.class);
        } catch (JsonProcessingException e) {
            log.error("error", e);
        }
        return null;
    }


    public String resetTeamServId(long uid, String teamId) {
        String[] keys = new String[1];
        keys[0] = OLS + uid;
        String[] args = new String[2];
        args[0] = "1"; //1: change teamId  2:change  game serverId
        args[1] = teamId;
        Object changedResult = redisDAO.evalsha(SET_USER_TEAM_LUA_SHA, keys, args);
        return (String) ((List) changedResult).get(0);
    }

    public String forceChangeGamSrvId(long uid, long gmSrvId) {
        String[] keys = new String[1];
        keys[0] = OLS + uid;
        String[] args = new String[2];
        args[0] = "2";//1: change teamId  2:change targetId
        args[1] = gmSrvId + "";
        Object changedResult = redisDAO.evalsha(SET_USER_TEAM_LUA_SHA, keys, args);
        return (String) ((List) changedResult).get(0);
    }

    public UserOnlineStateDO getUserOnlineStateDO(long userId) {
        String key = OLS + userId;
        return redisDAO.get(key, UserOnlineStateDO.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        USER_ONLINE_STATUS_LUA_SHA = redisDAO.scriptLoad(USER_ONLINE_STATUS_LUA);
        SET_USER_TEAM_LUA_SHA = redisDAO.scriptLoad(SET_USER_TEAM_LUA);
        //  test();
    }

    private void test() {
        UserOnlineStateDO userOnlineStateDO = new UserOnlineStateDO();

        userOnlineStateDO.setGmSrId("gs_1");
        UserOnlineStateDO newUserOnline = setUserOnlineStatus(1002, userOnlineStateDO);

        UserOnlineStateDO userOnlineStateD1 = new UserOnlineStateDO();
        userOnlineStateDO.setGmSrId("gs_2");
        UserOnlineStateDO newUserOnline2 = setUserOnlineStatus(1002, userOnlineStateDO);

        newUserOnline2.setGmSrId("gs_3");

        UserOnlineStateDO newUserOnline3 = setUserOnlineStatus(1002, newUserOnline2);
        log.info("new ={}", newUserOnline3);

        resetTeamServId(1002, "uuuuu");


    }
}

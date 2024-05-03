
cd ../build
nohup java -jar --add-exports java.base/jdk.internal.vm=ALL-UNNAMED -Xms6g -Xmx6g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -XX:+UseZGC -XX:+ZGenerational -Xlog:gc*:./logs/gc_game.log:time   game/game.jar >/dev/null  2>&1 &
nohup java -jar --add-exports java.base/jdk.internal.vm=ALL-UNNAMED -Xms6g -Xmx6g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -XX:+UseZGC -XX:+ZGenerational -Xlog:gc*:./logs/gc_gate.log:time  gateway/gateway.jar >/dev/null 2>&1&
nohup java -jar --add-exports java.base/jdk.internal.vm=ALL-UNNAMED -Xms3g -Xmx4g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -XX:+UseZGC -XX:+ZGenerational -Xlog:gc*:./logs/gc_login.log:time login/login.jar >/dev/null 2>&1&
nohup java -jar --add-exports java.base/jdk.internal.vm=ALL-UNNAMED -Xms3g -Xmx3g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -XX:+UseZGC -XX:+ZGenerational -Xlog:gc*:./logs/gc_router.log:time  router/router.jar > /dev/null 2>&1&
nohup java -jar --add-exports java.base/jdk.internal.vm=ALL-UNNAMED -Xms3g -Xmx3g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -XX:+UseZGC -XX:+ZGenerational -Xlog:gc*:./logs/gc_team.log:time  team/team.jar > /dev/null 2>&1&

ps aux | grep java 



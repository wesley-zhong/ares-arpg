
cd ../build
nohup java -jar game/game.jar > logs/game.log&
nohup java -jar gateway/gateway.jar > logs/gateway.log&
nohup java -jar login/login.jar > logs/login.log&
nohup java -jar router/router.jar > logs/router.log&
nohup java -jar team/team.jar > logs/team.log&

ps aux | grep java 



ps -ef|grep gateway|grep -v grep|awk '{print $2}' | xargs kill -9
ps -ef|grep game|grep -v grep|awk '{print $2}' | xargs kill -9
ps -ef|grep router|grep -v grep|awk '{print $2}' | xargs kill -9
ps -ef|grep team|grep -v grep|awk '{print $2}' | xargs kill -9
ps -ef|grep login|grep -v grep|awk '{print $2}' | xargs kill -9

ps -aux | grep java 



# 基于openjdk镜像
FROM openjdk:21

#定义环境变量
ARG MS_NAME=gateway
ENV JAVA_OPTS="-Xms128m -Xmx128m"

#声明需要暴露的端口
EXPOSE 2001

#设置工作目录
WORKDIR /opt/data/${MS_NAME}

# 复制文件到容器目录
ADD ./target/${MS_NAME}*.jar .

#定义匿名数据卷
VOLUME ["/opt/data/${MS_NAME}/logs"]

#配置容器启动后执行的命令
ENTRYPOINT java ${JAVA_OPTS} -Duser.timezone=GMT+8 -Dlog4j2.formatMsgNoLookups=true -XX:MetaspaceSize=1286m   -XX:MaxMetaspaceSize=128m  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=dump/${MS_NAME}.dump -jar  ${MS_NAME}*.jar
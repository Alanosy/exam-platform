# 考试服务镜像：防作弊服务（ruoyi-exam-proctor，端口 9219）
#
# 打镜像前要有 fat jar（本脚本之外先跑一次打包）：
#   mvn -DskipTests -pl ruoyi-modules/ruoyi-exam-proctor -am package
#
# 构建（构建上下文是模块目录，jar 从 target 里取）：
#   docker build -t ruoyi-exam-proctor:latest ruoyi-modules/ruoyi-exam-proctor
#
# 也可以直接用 script/docker/exam/build-images.sh 批量打。

# 贝尔实验室 Spring 官方推荐镜像 JDK下载地址 https://bell-sw.com/pages/downloads/
FROM bellsoft/liberica-openjdk-rocky:17.0.16-cds
#FROM bellsoft/liberica-openjdk-rocky:21.0.8-cds
#FROM findepi/graalvm:java17-native

LABEL maintainer="exam-platform"

RUN mkdir -p /ruoyi/ruoyi-exam-proctor/logs \
    /ruoyi/ruoyi-exam-proctor/temp \
    /ruoyi/skywalking/agent

WORKDIR /ruoyi/ruoyi-exam-proctor

ENV SERVER_PORT=9219 LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS=""

EXPOSE ${SERVER_PORT}

ADD ./target/ruoyi-exam-proctor.jar ./app.jar

# 用 exec 形式而不是 shell 形式：让 java 自己当容器主进程，
# docker stop 才能把 SIGTERM 送到 JVM（否则就是 10 秒后被 SIGKILL，优雅关闭形同虚设）。
# 端口与 JVM 参数仍走环境变量，运行时可覆盖。
ENTRYPOINT ["sh", "-c", "exec java \
    -Djava.security.egd=file:/dev/./urandom \
    -Dserver.port=${SERVER_PORT} \
    -XX:+HeapDumpOnOutOfMemoryError -XX:+UseZGC \
    ${JAVA_OPTS} -jar app.jar"]

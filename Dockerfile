# 拉取jdk8作为基础镜像
FROM openjdk:17
# 作者
MAINTAINER Alan <alanos@aliyun.com>
# 添加jar到镜像并命名为user.jar
ADD target/exam-1.0-SNAPSHOT.jar exam.jar
# 镜像启动后暴露的端口
EXPOSE 8080
# jar运行命令，参数使用逗号隔开
ENTRYPOINT ["java","-jar","exam.jar"]


## network

docker network create 
--driver bridge 
--subnet=172.20.0.0/24 
exam-network-custom-subnet

## mysql

docker run  -itd -dp 9601:3306 
--name mysql-master 
-e MYSQL_ROOT_PASSWORD=root 
-v /Users/alan/docker/mysql/mysql-master/data:/var/lib/mysql 
-v /Users/alan/docker/mysql/mysql-master/log:/var/log/mysql 
-v /Users/alan/docker/mysql/mysql-master/conf:/etc/mysql/conf.d 
--net=examnet 
--ip=172.20.0.4 
mysql:5.7.31

docker run  -itd -dp 9602:3306 
--name mysql-slave1 
-e MYSQL_ROOT_PASSWORD=root 
-v /Users/alan/docker/mysql/mysql-slave1/data:/var/lib/mysql 
-v /Users/alan/docker/mysql/mysql-slave1/log:/var/log/mysql 
-v /Users/alan/docker/mysql/mysql-slave1/conf:/etc/mysql/conf.d 
--ip=172.20.0.3 
--net=examnet 
mysql:5.7.31

docker run  -itd -dp 9603:3306
--name mysql-slave2
-e MYSQL_ROOT_PASSWORD=root
-v /Users/alan/docker/mysql/mysql-slave2/data:/var/lib/mysql
-v /Users/alan/docker/mysql/mysql-slave2/log:/var/log/mysql
-v /Users/alan/docker/mysql/mysql-slave2/conf:/etc/mysql/conf.d
--ip=172.20.0.2
--net=examnet
mysql:5.7.31

## redis

for port in `seq 6381 6386`; do
mkdir -p ${port}/conf &&
mkdir -p ${port}/data &&
PORT=${port} envsubst < ./redis-conf.tmpl > ./${port}/conf/redis.conf
done

for port in `seq 6381 6386`; do
docker stop redis-${port} && docker rm redis-${port}
done

counter=9
for port in `seq 6381 6386`; do
ip_last_digit=$((counter++))
docker run -d -p ${port}:${port} -p 1${port}:1${port}  --net=examnet --ip=172.20.0.${ip_last_digit} 
-v /Users/alan/docker/redis/redis-cluster/${port}/conf/redis.conf:/etc/redis/redis.conf 
-v /Users/alan/docker/redis/redis-cluster/${port}/data:/data 
--restart always 
--name=redis-${port} 
redis redis-server /etc/redis/redis.conf
done

docker exec -it redis-6381 /bin/bash
// 创建集群
redis-cli -a 123456 --cluster create 172.20.0.9:6381 172.20.0.10:6382 172.20.0.11:6383 172.20.0.12:6384 172.20.0.13:6385 172.20.0.14:6386 --cluster-replicas 1
redis-cli -a 123456 --cluster create redis-6381:6381 redis-6382:6382 redis-6383:6383 redis-6384:6384 redis-6385:6385 redis-6386:6386 --cluster-replicas 1

## exam

examnames=1
ipl=21
for port in `seq 8081 8083`; do
examname=$((examnames++))
ip_last_digit=$((ipl++))
docker run -itd --name exam-${examname} --net=examnet --ip=172.20.0.${ip_last_digit} -p:${port}:8080 exam
done

## nginx

docker run -d -p 80:80  --name nginx 
-v /Users/alan/docker/nginx/conf.d:/etc/nginx/conf.d  
-v /Users/alan/docker/nginx/www:/usr/share/nginx/html  
-v /Users/alan/docker/nginx/conf/nginx.conf:/etc/nginx/nginx.conf  
-v /Users/alan/docker/nginx/logs:/var/log/nginx 
--network examnet 
--ip=172.20.0.31 
nginx

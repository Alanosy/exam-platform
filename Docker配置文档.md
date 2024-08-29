## nginx

``` base
docker run -d -p 80:80  --name nginx \
-v /Users/alan/docker/nginx/conf.d:/etc/nginx/conf.d  \
-v /Users/alan/docker/nginx/www:/usr/share/nginx/html  \
-v /Users/alan/docker/nginx/conf/nginx.conf:/etc/nginx/nginx.conf  \
-v /Users/alan/docker/nginx/logs:/var/log/nginx \
--network exam-network \
nginx
```

## redis

```base

docker run -p 6380:6379 --name redis -v /Users/alan/redis/redis.conf:/etc/redis/redis.conf -v /Users/alan/redis/data:/data -d redis redis-server /etc/redis/redis.conf 

```

## mysql

```base
docker run -itd -p 9507:3306 --name slaver -v /Users/alan/docker/mysql/slave/cnf:/etc/mysql/conf.d -v /Users/alan/docker/mysql/slave/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=PvjFma5RLxEcZqSydIEmCpbpRqO6nZLNe5R0 mysql:5.7.31
```

```base
docker run -itd -p 9506:3306 --name master -v /Users/alan/docker/mysql/master/cnf:/etc/mysql/conf.d -v /Users/alan/docker/mysql/master/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=PvjFma5RLxEcZqSydIEmCpbpRqO6nZLNe5R0 mysql:5.7.31
```

redis集群

```
for port in `seq 6381 6386`; do  
    docker run -d -p ${port}:${port} --net=exam-network -v /Users/alan/docker/redis/redis-cluster/${port}/conf/redis.conf:/etc/redis/redis.conf  -v /Users/alan/docker/redis/redis-cluster/${port}/data:/data  --restart always  --name=redis-${port} redis redis-server /etc/redis/redis.conf  
done
```

```
for port in `seq 6381 6386`; do  
    docker stop redis-${port} && docker rm redis-${port}
done
```

```
// 创建集群
redis-cli -a 123456 --cluster create 172.20.0.9:6381 172.20.0.10:6382 172.20.0.11:6383 172.20.0.12:6384 172.20.0.13:6385 172.20.0.14:6386 --cluster-replicas 1
```

```
redis-cli -a 123456 --cluster create redis-6381:6381 redis-6382:6382 redis-6383:6383 redis-6384:6384 redis-6385:6385 redis-6386:6386 --cluster-replicas 1
```

```
for port in `seq 6381 6386`; do  
     docker inspect --format='{{json .NetworkSettings.Networks}}' redis-${port} 
done
```

```

for port in `seq 6381 6386`; do  
     docker restart  redis-${port} 
done
```

清空redis存放数据区域

```
for port in `seq 6381 6386`; do  
    rm -rf /Users/alan/docker/redis/redis-cluster/${port}/data && mkdir /Users/alan/docker/redis/redis-cluster/${port}/data
done
```

```

docker run -d --name prometheus -p 9090:9090 --restart=always \
-v /Users/alan/docker/prometheus/server/prometheus.yml:/etc/prometheus/prometheus.yml \
-v /Users/alan/docker/prometheus/server/rules.yml:/etc/prometheus/rules.yml \
--net=exam-network  \
prom/prometheus:v2.39.1 \
--config.file=/etc/prometheus/prometheus.yml \
--web.enable-lifecycle

————————————————

                            版权声明：本文为博主原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接和本声明。
                        
原文链接：https://blog.csdn.net/summer_fish/article/details/127736056
```

1. 
   docker cp grafana:/etc/grafana/grafana.ini /Users/alan/docker/prometheus/grafana/



```
docker run -d -p 3000:3000 --name grafana  --restart=always \
-v /Users/alan/docker/prometheus/grafana/grafana.ini:/etc/grafana/grafana.ini \
-v /Users/alan/docker/prometheus/grafana/data:/var/lib/grafana \
-e "GF_SECURITY_ADMIN_PASSWORD=admin" \
--net=exam-network \
grafana/grafana:9.1.8

————————————————

                            版权声明：本文为博主原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接和本声明。
                        
原文链接：https://blog.csdn.net/summer_fish/article/details/127736056
```

```
docker run --cap-add=NET_ADMIN --cap-add=NET_BROADCAST --cap-add=NET_RAW --net=host -d --name keepalived \
    -v /etc/localtime:/etc/localtime \
    -e KEEPALIVED_INTERFACE='ens160' \
    -e KEEPALIVED_PASSWORD='d0cker' \
    -e KEEPALIVED_STATE='BACKUP' \
    -e KEEPALIVED_ROUTER_ID='51' \
    -e KEEPALIVED_UNICAST_PEERS="#PYTHON2BASH:['10.100.1.123','10.100.1.125']" \
    -e KEEPALIVED_VIRTUAL_IPS='10.100.1.155' \
    osixia/keepalived:2.0.20
```

mysql双主双从

```
#conf文件夹用于存放配置文件
mkdir -p /Users/alan/docker/mysql/mysql-master/conf
#data文件夹用于存放mysql数据文件
mkdir -p /Users/alan/docker/mysql/mysql-master/data
#log文件夹用于存放mysql日志文件
mkdir -p /Users/alan/docker/mysql/mysql-master/log

mkdir -p /Users/alan/docker/mysql/mysql-master1/conf
mkdir -p /Users/alan/docker/mysql/mysql-master1/data
mkdir -p /Users/alan/docker/mysql/mysql-master1/log

mkdir -p /Users/alan/docker/mysql/mysql-slave/conf
mkdir -p /Users/alan/docker/mysql/mysql-slave/data
mkdir -p /Users/alan/docker/mysql/mysql-slave/log

mkdir -p /Users/alan/docker/mysql/mysql-slave1/conf
mkdir -p /Users/alan/docker/mysql/mysql-slave1/data
mkdir -p /Users/alan/docker/mysql/mysql-slave1/log

```

```
docker run --name mysql-master \
-e MYSQL_ROOT_PASSWORD=root \
-v /Users/alan/docker/mysql_master/data:/var/lib/mysql \
-v /Users/alan/docker/mysql_master/log:/var/log/mysql \
-v /Users/alan/docker/mysql_master/conf:/etc/mysql/conf.d \
-dp 9508:3306 mysql:5.7.31

```

```
docker run --name mysql-master1 \
-e MYSQL_ROOT_PASSWORD=root \
-v /Users/alan/docker/mysql_master1/data:/var/lib/mysql \
-v /Users/alan/docker/mysql_master1/log:/var/log/mysql \
-v /Users/alan/docker/mysql_master1/conf:/etc/mysql/conf.d \
-dp 9509:3306 mysql:5.7.31

```

```
docker run --name mysql_slave \
-e MYSQL_ROOT_PASSWORD=root \
-v /Users/alan/docker/mysql_slave/data:/var/lib/mysql \
-v /Users/alan/docker/mysql_slave/log:/var/log/mysql \
-v /Users/alan/docker/mysql_slave/conf:/etc/mysql/conf.d \
-dp 9510:3306 mysql:5.7.31

```

```
docker run --name mysql_slave1 \
-e MYSQL_ROOT_PASSWORD=root \
-v /Users/alan/docker/mysql_slave1/data:/var/lib/mysql \
-v /Users/alan/docker/mysql_slave1/log:/var/log/mysql \
-v /Users/alan/docker/mysql_slave1/conf:/etc/mysql/conf.d \
-dp 9511:3306 mysql:5.7.31


```


# 도커 기반으로 서비스하기
## 네트워크 만들기
```
docker network create --gateway 172.18.0.1 --subnet 172.18.0.0/16 mynetwork
```
컨테이너에 직접 IP를 지정할 때 gateway와 subnet이 있어야 오류가 안 난다

```
docker network inspect mynetwork
```

## RabbitMQ
```
docker run -d --name rabbitmq --network mynetwork
  -p 15672:15672 -p 15671:15671 -p 5672:5672 -p 4369:4369
  -e RABBITMQ_DEFAULT_USER=admin
  -e RABBITMQ_DEFAULT_PASS=admin
  rabbitmq:management
```

## Config Server
### Dockerfile
```
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY apiEncryptionKey.jks apiEncryptionKey.jks #설정정보를 암호화한 것을 복호화 하는데 필요
COPY target/config-service-1.0.jar ConfigServer.jar
ENTRYPOINT ["java", "-jar", "ConfigServer.jar"]
```
`docker build --tag config-service:1.0 .`
### Docker run
```
docker run -d -p 8888:8888 --network mynetwork 
  -e "spring.rabbitmq.host=rabbitmq"
  -e "spriong.profiles.active=default"
  --name config-service config-service:1.0
```

## Eureka Discovery
### Dockerfile
```
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/discoveryservice-1.0.jar DiscoveryService.jar
ENTRYPOINT ["java", "-jar", "DiscoveryService.jar"]
```
### Docker run
```
docker run -d -p 8761:8761 --network mynetwork 
  -e "spring.cloud.config.uri=http://config-service:8888"
  --name discovery-service discovery-service:1.0
```

## API Gateway Server
### Dockerfile
```
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/apigateway-service-1.0.jar ApigatewayService.jar
ENTRYPOINT ["java", "-jar", "ApigatewayService.jar"]
```

### Docker run
```
docker run -d -p 8000:8000 --network mynetwork \
  -e "spring.cloud.config.uri=http://config-service:8888" \
  -e "spriong.rabbitmq.host=rabbitmq" \
  -e "eureka.client.service-url.defaultZone=http://discovery-service:8761/eureka" \
  --name apigateway-service discovery-service:1.0 
```

## MySQL
1. 기존에 있던 데이터베이스를 그대로 가져온다고 하자
2. db의 정보들을 ./mysql_data/mysql로 옮겼다고 하자
### Dockerfile
```
FROM mysql
ENV MYSQL_ROOT_PASSWORD password
ENV MYSQL_DATABASE mydb
COPY ./mysql_data/mysql /var/lib/mysql
EXPOSE 3306
ENTRYPOINT ["mysqld", "--user=root"]
```

### Docker run
```
docker -d -p 3306:3006 --network mynetwork --name mysql mysql:1.0
```

## Kafka
1. Zookeeper, Kafka Broker를 실행
2. wurstmeister/kafka-docker를 사용하면 편하다
3. 여기서는 `docker-compose-single-broker.yml`를 적절히 수정해서 사용하자

### docker-compose-single-broker.yml
```
version: '2'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"
    networks:
      our-network:
        ipv4_addresss: 172.18.0.100
  kafka:
    image: wurstmeister/kafka
    ports:
      - "9002:9002"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: 172.18.0.101
      KAFKA_CREATE_TOPICS: "topicname:1:1"
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    depends_on:
      - zookeeper
    networks:
      our-network:
      ipv4_address: 172.18.0.101

networks:
  our-networks:
    name: mynetwork
```
4. `docker-compose -f docker-compose-single-broker.yml up -d`

## zipkin
```
docker run -d -p 9411:9411 \
  --network mynetwork \
  --name zipkin \
  openzipkin/zipkin
```

## Prometheus, Grafana
### Prometheus
```
docker run -d -p 9090:9090 \
  --network mynetwork \
  --name prometheus \
  -v /path/to/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```

### Grafana
```
docker run -d -p 3000:3000 \
  --network mynetwork \
  --name grafana \
  grafana/grafana
```

## User Service
### Dockerfile
```
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/user-service-1.0.jar UserService.jar
ENTRYPOINT ["java", "-jar", "UserService.jar"]
```

### Docker run
```
docker run -d --network mynetwork \
  --name user-service
  -e "spring.cloud.config.uri=http://config-service:8888" \
  -e "spring.rabbitmq.host=rabbitmq" \
  -e "spring.zipkin.base-url=http://zipkin:9411" \
  -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka" \
  -e "logging.file=/api-logs/users-ws.log"
  user-service:1.0
```


## Order Service
### Dockerfile
```
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/order-service-1.0.jar OrderService.jar
ENTRYPOINT ["java", "-jar", "OrderService.jar"]
```

### Docker run
```
docker run -d --network mynetwork \
  --name order-service
  -e "spring.cloud.config.uri=http://config-service:8888" \
  -e "spring.rabbitmq.host=rabbitmq" \
  -e "spring.zipkin.base-url=http://zipkin:9411" \
  -e "spring.datasource.url=jdbc:mysql://mysql/mydb"
  -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka" \
  -e "logging.file=/api-logs/orders-ws.log"
  order-service:1.0
```


## Catalog Service
### Dockerfile
```
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/catalog-service-1.0.jar CatalogService.jar
ENTRYPOINT ["java", "-jar", "CatalogService.jar"]
```
### Docker run
```
docker run -d --network mynetwork \
  --name catalog-service
  -e "spring.cloud.config.uri=http://config-service:8888" \
  -e "spring.rabbitmq.host=rabbitmq" \
  -e "spring.zipkin.base-url=http://zipkin:9411" \
  -e "spring.datasource.url=jdbc:mysql://mysql/mydb"
  -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka" \
  -e "logging.file=/api-logs/catalog-ws.log"
  catalog-service:1.0
```

# 패턴
## Event Driven Architecture
1. 각 MSA간에 서로 다른 DB에 대해 트랜잭션이 필요한 경우가 있다
2. 예를 들어 주문 처리에 대한 재고에 따라 성공 OR 실패가 될 수 있다
3. 카프카를 이용하여 이를 제어하자

### Event Sourcing
- 데이터의 마지막 상태만 저장하는 것이 아닌, 해당 데이터에 수행된 전체 이력을 기록
- 데이터 구조 단순
- 데이터 일관성과 트랜잭션 처리 가능
- 데이터 저장소의 개체를 직접 업데이트 하지 않기 때문에, 동시성에 대한 충돌 문제 해결 

## CQRS
- 명령과 조회의 책임 분리
- Command And Query

## Saga Pattern
- Application에서 Transaction 처리
- 각각의 MSA에서는 Local Transaction을 처리하자
- 데이터의 원자성을 보장하지는 않지만, 일관성을 보장한다

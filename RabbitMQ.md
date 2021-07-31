
**MQ**  
1.解决模块之间的耦合度过高,导致一个模块宕机后,全部功能都不能使用;  
2.解决同步通讯的成本问题.
MQ:ActiveMQ  RocketMQ  Kafka  RabbitMQ  
3.语言支持:ActiveMQ  RocketMQ只支持Java语言  
Kafka支持多门语言  
RabbitMQ支持多种语言  
4.效率方面:RabbitMQ是微秒级别,ActiveMQ  RocketMQ  Kafka是毫秒级别;  
5.消息丢失/消息重复问题:RabbitMQ针对消息的持久化和重复问题有比较成熟的解决方案;  

RabbitMQ严格遵循AMQP协议,高级消息队列协议,帮助我们在进程之间传递异步消息;  

架构:publisher(生产者)->Exchange(交换机)->通过Routes(路由)->Queue(队列)->Consumer(消费者)  
publisher(生产者):发布消息到RabbitMQ的Exchange中  
Consumer(消费者):监听RabbitMQ中的Queue中的消息  
Exchange(交换机):和生产者建立连接并接收生产者的消息  
Queue(队列):获取Exchange会将消息分布到制定的queue中,queue和消费者进行交互  
Routes(路由):交换机以什么样的策略将消息发布到Queue中  

一个队列中的消息,只会被一个消费者消费一次.(即:队列中只有一个消息,但是有多个消费者时,这一个消息
不会被重复消费)  

图形化界面:  
connections:本地与rabbitmq建立连接的地方   virtual hosts
channels:管道,用于提供者与exchange/消费者与queue通讯
exchange:服务提供者将消息发布到此
admin:创建用户

先创建用户->创建virtual hosts


RabbitMQ通讯方式:7种  
1. Hello-world方式 : 一个生产者,一个默认的交换机,一个消费者  
2. work方式 : 一个生产者,一个默认的交换机,两个消费者  
3. publish方式: 一个生产者,一个交换机,两个队列,两个消费者  
4. routing方式: 一个生产者,一个交换机,两个队列,两个消费者,但是交换机和队列间通过routingKey绑定

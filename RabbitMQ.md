
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
1. Hello-world方式(simple) : 一个生产者,一个默认的交换机,一个消费者   
2. work方式(work) : 一个生产者,一个默认的交换机,两个消费者    
3. publish方式(fanout) : 一个生产者,一个交换机,两个队列,两个消费者  
4. routing方式(direct) : 一个生产者,一个交换机,两个队列,两个消费者,但是交换机和队列间通过routingKey绑定  
5. topic方式(topic) : 一个生产者,一个交换机,两个队列,两个消费者,交换机与队列通过routingKey绑定,
一个队列可以绑定多个routingKey,通过*(占位符)或者#(通配符)对routingKey命名


**注意点**  
1.因为队列queue有持久化机制,因此即使发送消息到RabbitMQ后,RabbitMQ宕机,也不会影响消息的传递  
2.如果消费者消费到一半,也宕机了,怎么办?通过手动ACK,如果RabbitMQ没接收到消费成功时,可以传给其他消费者,或者间隔多长时间再让
消费者消费  
3.生产者在发送消息时,没有发送到RabbitMQ(例如由于网络问题),但是生产者还以为发送成功了,怎么办?
RabbitMQ提供了事务操作和confirm操作.  

**RabbitMQ事务**  
事务可以保证消息100%传递,可以通过事务的回滚取记录日志,后面定时再次发送当前消息.但事务操作,效率太低,家里事务操作
后,效率慢100倍.  

RabbitMQ的confirm的确认操作,效率比事务高很多  
1.普通confirm操作,用`channel.waitForConfirms()`确认是否成功  
 `//3.1开启confirm
channel.confirmSelect();
//发送消息
//确认发送成功
if(channel.waitForConfirms()){
    System.out.println("消息发送成功");
}else{
    System.out.println("消息发送失败");
}`

2.批量confirm操作`waitForConfirmsOrDie()`
当发送的消息,有一个失败时,就直接全部失败,并抛出异常    

3.异步confirm操作(最常用):`addConfirmListener`添加监听  
`//异步confirm方式,开启异步回调,确认消息发送成功,因为时异步,所以不会影响后边的逻辑的正常运行
         channel.addConfirmListener(new ConfirmListener() {
             public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                 System.out.println("消息发送成功!标识:"+deliveryTag+",是否批量:"+multiple);
             }
             public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                 System.out.println("消息发送失败!标识:"+deliveryTag+",是否批量:"+multiple);
             }
         });`  


**RabbitMQ的return机制**  
RabbitMQ的事务和confirm机制,是用于保证生产者将消息发送到exchange,
但是若exchange和queue之间存在问题,是无法解决的,而且exchange是不能持久化消息的,
queue可以持久化消息,因此就需要用到RabbitMQ的return机制,来监听消息是否从exchange送到了指定的queue中  

`//3.2开启return机制
     channel.addReturnListener(new ReturnListener() {
         public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
             //当消息没有送达到queue时,才会执行
             //没有送达的消息会在body中
             System.out.println(new String(body,"UTF-8")+"没有送达到队列中");
         }
     });`
同时发送消息时,指定mandatory的参数为true    
`channel.basicPublish("","HelloWorld_Test01",true,null,msg.getBytes());
`

**避免重复消费消息**    
一个消息,通过queue分给消费者1号后,然后1号进行业务处理,消费完后,没有手动给RabbitMQ返回消费完的ACK,
然后RabbitMQ以为没有消费,就会把这个消息给到消费者2号,然后2号又把这个消息消费了一遍,导致重复消费的问题.  
1.对与幂等性的操作,例如修改  删除,重复消费可能没有问题,
但是对于非幂等性的操作,例如新增,而且数据库主键时自增的,就会存在数据重复的问题,因此就需要保证消息不会被重复消费.  

**问题:**归根到底,都是消费者没有给RabbitMQ一个ACK.  
为解决消息重复消费的问题,可以采用Redis,在消费者1号消费消息之前,先将消息的id放到Redis中,并设置该id的状态
(例如:0:正在执行业务,1:业务执行完成),如果ack失败,在RabbitMQ将消息交给消费者2号时,限制性redis的setnx命令(不存在时才设置)
如果id存在,则获取他的值,如果是0,则当前消费者2号什么都不做,如果是1,则给RabbitMQ直接返回ack    
极端情况:消费者1号在执行业务时,出现了死锁,在setnx的基础上,再给id设置一个生存时间,到期后会被自动删除,然后其他消费者就可以
消费者就可以消费了.

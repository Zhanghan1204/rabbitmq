package com.mq.routing;

import com.mq.config.RabbitMQ;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

//1.pubsub方式
public class Publisher {
    //1.创建生产者,创建一个channel,
    //2.创建一个DIRECT类型的exchange和两个队列,并将exchange与队列绑定,指定路由规则routingKey
    //3.创建两个消费者,各创建一个channel,分别与队列1 2绑定,消费对应的队列
    // basicConsume中的autoACK设置成false,并且basicQos指定每次的消费能力,同时在回调函数中配置basicAck,消费完后手动告诉RabbitMQ已消费完成

    //1.生产者创建一个DIRECT类型的exchange,和一个或者多个队列通过routingKey绑定到一起,在发送消息时
    //指定消息的具体RoutingKey即可找到对应的队列
    //2.消费者指定好队列,如:消费者1监听error队列,消费者2监听info队列

    @Test
    public void publish() throws IOException, TimeoutException {

        //1.获取connection
        Connection connection = RabbitMQ.getConnection();

        //2.创建Channel
        Channel channel = connection.createChannel();

        //3.创建exchange - 绑定一个队列
        //参数1:exchange的名称,自定义
        //参数2:指顶exchenge类型,可以直接写类型的名字,也可以点出来 类型有:FANOUT DIRECT HEADERS   TOPIC
        channel.exchangeDeclare("routing-exchange", BuiltinExchangeType.DIRECT);
        //绑定队列
        channel.queueBind("routing-queue-error","routing-exchange","ERROR");
        channel.queueBind("routing-queue-info","routing-exchange","INFO");


        //3.发布消息到exchange,同时制定路由的规则
        //参数1:指定exchange,使用"",表示使用默认的方式
        //参数2:指定路由的规则,在运行时,交换机会根据routingKey去找对应的queue队列
        //参数3:指定传递的消息所携带的propeities
        //参数4:指定发布的具体消息,byte[]类型
        channel.basicPublish("routing-exchange","ERROR",null,"error".getBytes());
        channel.basicPublish("routing-exchange","INFO",null,"msg1".getBytes());
        channel.basicPublish("routing-exchange","INFO",null,"msg2".getBytes());
        channel.basicPublish("routing-exchange","INFO",null,"msg3".getBytes());
        //Ps:exchange不会帮我们将消息持久化到本地,Queue才能帮我们持久化消息(但也要看具体配置)

        System.out.println("生产者发布消息成功");
        //4.释放资源
        channel.close();
        connection.close();


    }




}

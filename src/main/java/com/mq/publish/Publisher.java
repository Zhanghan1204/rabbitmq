package com.mq.publish;

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
    //2.创建一个FANOUT类型的exchange和两个队列,并将exchange与队列绑定,指定路由规则
    //3.创建两个消费者,各创建一个channel,分别与队列1 2 绑定,消费当前队列
    // basicConsume中的autoACK设置成false,并且basicQos指定每次的消费能力,同时在回调函数中配置basicAck,消费完后手动告诉RabbitMQ已消费完成

    //1.生产者创建一个exchange并且指定类型,和一个或者多个队列绑定到一起
    //2.消费者依然正常的监听某一个队列即可

    @Test
    public void publish() throws IOException, TimeoutException {

        //1.获取connection
        Connection connection = RabbitMQ.getConnection();

        //2.创建Channel
        Channel channel = connection.createChannel();

        //3.创建exchange - 绑定一个队列
        //参数1:exchange的名称,自定义
        //参数2:指顶exchenge类型,可以直接写类型的名字,也可以点出来 类型有:FANOUT DIRECT HEADERS   TOPIC
        channel.exchangeDeclare("pubsub-exchange", BuiltinExchangeType.FANOUT);
        //绑定队列
        channel.queueBind("pubsub-queue1","pubsub-exchange","");
        channel.queueBind("pubsub-queue2","pubsub-exchange","");


        //3.发布消息到exchange,同时制定路由的规则
        //参数1:指定exchange,使用"",表示使用默认的方式
        //参数2:指定路由的规则,使用具体的队列名称
        //参数3:指定传递的消息所携带的propeities
        //参数4:指定发布的具体消息,byte[]类型
        for(int i = 0;i<10;i++){
            String msg = "Hello-World!"+i;
            channel.basicPublish("pubsub-exchange","Publish_Test01",null,msg.getBytes());
        }
        //Ps:exchange不会帮我们将消息持久化到本地,Queue才能帮我们持久化消息(但也要看具体配置)

        System.out.println("生产者发布消息成功");
        //4.释放资源
        channel.close();
        connection.close();


    }




}

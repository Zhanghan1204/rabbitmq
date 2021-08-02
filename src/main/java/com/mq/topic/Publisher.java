package com.mq.topic;

import com.mq.config.RabbitMQ;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

//1.topic方式
//生产者创建Topic的exchange并且绑定到队列中,这次绑定可以通过*和#关键字,对指定Routing内容
//注意格式为:xxxx.xxxx.xxxx   其中一个*代表一个xxxx,而一个#可以代表多个xxxx.xxxx
//在发送消息时,指定具体的RoutingKey到底是什么
//消费者只用监听具体的队列即可

public class Publisher {
    //1.创建生产者,创建一个channel,
    //2.创建一个TOPIC类型的exchange和两个队列,并将exchange与队列绑定,指定路由规则
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
        channel.exchangeDeclare("topic-exchange", BuiltinExchangeType.TOPIC);
        //绑定队列  一个exchange可以绑定多个队列,同时一个队列可以被多次绑定
        //发布者发布动物信息  速度  颜色 类型
        //*.red.*           *是占位符  1个*代表一个位置
        //fast.#            #是通配符  1个#可以代表多个位置
        //#.rabbit 或者 *.*.rabbit
        channel.queueBind("topic-queue1","topic-exchange","*.red.*");
        channel.queueBind("topic-queue2","topic-exchange","fast.#");
        channel.queueBind("topic-queue2","topic-exchange","#.rabbit");


        //3.发布消息到exchange,同时制定路由的规则
        //参数1:指定exchange,使用"",表示使用默认的方式
        //参数2:指定路由的规则,使用具体的队列名称
        //参数3:指定传递的消息所携带的propeities
        //参数4:指定发布的具体消息,byte[]类型
        for(int i = 0;i<10;i++){
            String msg = "Hello-World!"+i;
            //消息1可以被队列1 2同时监听到
            //队列1 2 都监听不到消息2
            //消息3只有队列2可以监听到
            //消息4只有队列2可以监听到
            //消息5只有队列1可以监听到
            channel.basicPublish("topic-exchange","fast.red.monkey",null,"红快猴子".getBytes());
            channel.basicPublish("topic-exchange","slow.black.dog",null,"黑漫狗".getBytes());
            channel.basicPublish("topic-exchange","fast.white.cat.one",null,"快白猫".getBytes());
            channel.basicPublish("topic-exchange","one.two.rabbit",null,"兔子".getBytes());
            channel.basicPublish("topic-exchange","abc.red.monkey",null,"abc".getBytes());


        }
        //Ps:exchange不会帮我们将消息持久化到本地,Queue才能帮我们持久化消息(但也要看具体配置)

        System.out.println("生产者发布消息成功");
        //4.释放资源
        channel.close();
        connection.close();


    }




}

package com.mq.helloworld;

import com.mq.config.RabbitMQ;
import com.rabbitmq.client.*;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer {


    @Test
    public void consume() throws IOException, TimeoutException {

        //1.获取连接对象
        Connection connection = RabbitMQ.getConnection();


        //2.创建channel,连接队列
        Channel channel = connection.createChannel();

        //3.声明队列-helloworld
        //参数1:queue - 指定消费队列名称
        //参数2:durable - 当前队列是否需要持久化(当RabbitMQ宕机后,再重启后队列是否还在,true:在,false:没有了)
        //参数3:exclusive - 是否排外(connection.close() - 当前队列会被自动删除,当前队列只能被一个消费者消费,若后边还有其他消费者,则其他消费者会直接报错)
        //参数4:autoDelete - 若队列无消费者消费,则队列会被自动删除
        //参数5:arguments - 指定当前队列的其他消费
        channel.queueDeclare("HelloWorld_Test01",true,false,false,null);

        //4.开启监听Queue

        //创建回调函数,把channel传进去
        DefaultConsumer consumer = new DefaultConsumer(channel){
            //右键 -> Generate -> Override Methods -> handleDelivery()

            //重写handleDelivery方法
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("接收到消息:"+new String(body,"UTF-8"));
            }
        };


        //参数1:queue - 指定消费队列名称
        //参数2:deliverCallback - 指定是否自动ACK(true:接收消费后,会立即告诉RabbitMQ,这个消息已经被消费了;false:接收消息后,需要手动告诉RabbitMQ)
        //参数3:cancelCallback - 指定消费回调函数consumer
        channel.basicConsume("HelloWorld_Test01",true,consumer);

        System.out.println("消费者开始监听队列");
        System.in.read();//保证监听不会停
        //5.释放资源
        channel.close();
        connection.close();

    }

}
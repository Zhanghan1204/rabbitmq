package com.mq.helloworld;

import com.mq.config.RabbitMQ;
import com.rabbitmq.client.*;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

//1.Hello-world方式
public class Publisher2 {
    //1.创建生产者,创建一个channel,发布消息到exchange,指定路由规则
    //2.创建消费者,创建一个channel,创建一个队列,并且去消费当前队列
    // basicConsume中的autoACK设置成true,完成消费后,会立即告诉RabbitMQ,这个消息已经被消费了


    @Test
    public void publish() throws IOException, TimeoutException, InterruptedException {


        //1.获取connection
        Connection connection = RabbitMQ.getConnection();

        //2.创建Channel
        Channel channel = connection.createChannel();

        //3.发布消息到exchange,同时制定路由的规则
        //3.1开启confirm
        channel.confirmSelect();

        //3.2开启return机制
        channel.addReturnListener(new ReturnListener() {
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //当消息没有送达到queue时,才会执行
                //没有送达的消息会在body中
                System.out.println(new String(body,"UTF-8")+"没有送达到队列中");
            }
        });

        for (int i = 0; i < 1000; i++) {

            //参数1 exchange:指定exchange,使用"",表示使用默认的方式
            //参数2 routingKey:指定路由的规则,可使用具体的队列名称
            //参数3 mandatory:该字段设置成true,才能保证exchange没有将消息发送到queue时,正常的回调,相当于是让RabbitMQ的return机制生效
            //参数4 :指定传递的消息所携带的properties
            //参数5 :指定发布的具体消息,byte[]类型
            String msg = "Hello-World!"+i;

            //为了测试RabbitMQ的return机制时,可以对RoutingKey设置一个不存在的queue,例如设置成xxxx
            channel.basicPublish("","HelloWorld_Test01",true,null,msg.getBytes());
            //channel.basicPublish("","xxxx",true,null,msg.getBytes());
            //Ps:exchange不会帮我们将消息持久化到本地,Queue才能帮我们持久化消息(但也要看具体配置)
        }

        //异步confirm方式,开启异步回调,确认消息发送成功,因为时异步,所以不会影响后边的逻辑的正常运行
        channel.addConfirmListener(new ConfirmListener() {
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("消息发送成功!标识:"+deliveryTag+",是否批量:"+multiple);
            }

            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("消息发送失败!标识:"+deliveryTag+",是否批量:"+multiple);
            }
        });


        System.in.read();
        System.out.println("生产者发布消息成功");
        //4.释放资源
        channel.close();
        connection.close();


    }




}

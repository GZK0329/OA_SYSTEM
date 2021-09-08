package com.example.emos.wx.task;

import com.example.emos.wx.db.pojo.MessageEntity;
import com.example.emos.wx.db.pojo.MessageRefEntity;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.MessageService;
import com.rabbitmq.client.*;

import com.rabbitmq.client.impl.AMQBasicProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @Classname MessageTask
 * @Description TODO
 * @Date 2021/8/13 10:40
 * @Created by GZK0329
 */
@Slf4j
@Component
public class MessageTask {
    @Autowired
    private ConnectionFactory factory;

    @Autowired
    private MessageService messageService;

    /**
     * 同步发送消息
     */
    public void send(String topic, MessageEntity entity) {
        //向mongodb数据库发送消息 返回消息的id
        String id = messageService.insertMessage(entity);
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            /**
             * 声明一个队列 channel.QueueDeclare(name队列名, durable是否持久化, exclusive, autoDelete自动删, HashMap args)
             * exclusive - true if we are declaring an exclusive queue (restricted to this connection)
             * arguments - other properties (construction arguments) for the queue
             */
            channel.queueDeclare(topic, true, false, false, null);

            HashMap header = new HashMap();
            header.put("messageId", id);
            /**
             * 创建AMQP协议参数对象，添加附加属性
             */
            AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().headers(header).build();
            /**
             * 发布消息
             * exchange队列交换机 绑定队列 - the exchange to publish the message to
             * routingKey - the routing key
             * props - other properties for the message - routing headers etc
             * body - the message body
             */
            channel.basicPublish("", topic, properties, entity.getMsg().getBytes());
            log.debug("消息发送成功");
        } catch (Exception e) {
            log.error("执行异常", e);
            throw new EmosException("向MQ发送消息失败");
        }
    }

    @Async
    public void sendAsync(String topic, MessageEntity entity) {
        send(topic, entity);
    }

    /**
     * 同步接收消息
     * 返回接收消息数量
     */
    public int receive(String topic) {
        int i = 0;
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            //获取队列
            channel.queueDeclare(topic, true, false, false, null);
            //从队列中取消息 死循环直到队列为空
            while (true) {
                //basicGet(String queue, boolean autoAck)从队列中取一个消息
                //String queue:队列名
                //boolean autoAck:自动确认
                GetResponse response = channel.basicGet(topic, false);
                if (response != null) {
                    AMQP.BasicProperties props = response.getProps();
                    Map<String, Object> header = props.getHeaders();
                    String messageId = header.get("messageId").toString();

                    byte[] body = response.getBody();
                    String message = new String(body);
                    log.debug("从mq中接收到消息:" + message);

                    MessageRefEntity refEntity = new MessageRefEntity();
                    refEntity.setMessageId(messageId);
                    refEntity.setReceiverId(Integer.parseInt(topic));
                    refEntity.setReadFlag(false);
                    refEntity.setLastFlag(true);
                    messageService.insert(refEntity);
                    long deliveryTag = response.getEnvelope().getDeliveryTag();
                    /**确认接收了一条或多条消息
                     * basicAck(long deliveryTag, boolean multiple)
                     * deliveryTag - the tag from the received AMQP.Basic.GetOk or AMQP.Basic.Deliver
                     * multiple - true to acknowledge all messages up to and including the supplied delivery tag;
                     * false to acknowledge just the supplied delivery tag.
                     */
                    channel.basicAck(deliveryTag, false);
                    i++;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("执行异常", e);
            throw new EmosException("从MQ中接收消息失败");
        }
        return i;
    }

    /**
     * 异步接收消息
     */
    @Async
    public int receiveAsync(String topic) {
        return receive(topic);
    }


    /**
     * 删除队列
     */
    public void deleteQueue(String topic) {
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDelete(topic);
            log.debug("删除队列成功");
        } catch (Exception e) {
            log.error("删除队列失败", e);
            throw new EmosException("删除队列失败");
        }
    }

    @Async
    public void deleteQueueAsync(String topic) {
        deleteQueue(topic);
    }

}

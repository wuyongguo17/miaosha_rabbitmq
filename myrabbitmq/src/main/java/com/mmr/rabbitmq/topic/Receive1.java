package com.mmr.rabbitmq.topic;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.mmr.rabbitmq.util.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

public class Receive1 {
	private static final String QUEUE_NAME = "test_queque_topic_1";
	private static final String EXCHANGE_NAME = "test_exchange_topic";
	
	public static void main(String[] args) throws IOException, TimeoutException {
		Connection connection = ConnectionUtil.getConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "goods.add");
		
		DefaultConsumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				String msg = new String(body, "utf-8");
				System.out.println("recieve[1]：" + msg);
			}

		};
		channel.basicConsume(QUEUE_NAME, true,consumer);
	}

}

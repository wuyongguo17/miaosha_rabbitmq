package com.mmr.rabbitmq.work;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.mmr.rabbitmq.util.ConnectionUtil;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class Recieve2 {
	private static final String QUEUE_NAME = "test_work_queue";
	
	public static void main(String[] args) throws IOException, TimeoutException {
		Connection connection = ConnectionUtil.getConnection();
		Channel channel = connection.createChannel();
		//声明队列
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		
		//定义一个消费者
		DefaultConsumer consumer = new DefaultConsumer(channel) {
			//消息到达出发这个方法
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, 
					BasicProperties properties, byte[] body)
					throws IOException {
				String msg = new String(body,"utf-8");
				System.out.println("[2] recieve msg" + msg);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}finally {
					System.out.println("[2] done");
				}
			}
			
		};
		
		boolean autoAck = true;
		channel.basicConsume(QUEUE_NAME, autoAck,consumer);
	}

}

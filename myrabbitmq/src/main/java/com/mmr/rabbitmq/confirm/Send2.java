package com.mmr.rabbitmq.confirm;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.mmr.rabbitmq.util.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * 普通模式：批量发送
 * 
 * @author 吴永国
 *
 */
public class Send2 {
	private static final String QUEUE_NAME = "test_queue_confirm1";

	public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
		Connection connection = ConnectionUtil.getConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);

		// 生产者调用confirmSelect 将channel设置为confirm模式，注意：如果队列已经设置了其他的模式，不能更改模式。
		channel.confirmSelect();

		String msg = "hello confirm message";
		//批量发送
		for (int i = 0; i < 10; i++) {
			channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
		}
		
		//确认
		if (!channel.waitForConfirms()) {
			System.out.println("发送失败");
		}else {
			System.out.println("发送成功");
		}
		
		channel.close();
		connection.close();
	}

}

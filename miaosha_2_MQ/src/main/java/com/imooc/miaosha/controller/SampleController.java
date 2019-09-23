package com.imooc.miaosha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.miaosha.domain.User;
import com.imooc.miaosha.rabbitmq.MQSender;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.redis.UserKey;
//import com.imooc.miaosha.redis.RedisService;
//import com.imooc.miaosha.redis.UserKey;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.UserService;

@Controller
@RequestMapping("/demo")
public class SampleController {

	@Autowired
	UserService userService;

	@Autowired
	RedisService redisService;
	
	@Autowired
	MQSender sender;
	
	@RequestMapping("/mq")
	@ResponseBody
	public Result<String> mq() {
		sender.send("hello MQ");
		return Result.success("Hello，world");
	}
	
	@RequestMapping("/mq/topic")
	@ResponseBody
	public Result<String> topic() {
		sender.sendTopic("hello MQ");
		return Result.success("Hello，world");
	}
	
	@RequestMapping("/mq/fanout")
	@ResponseBody
	public Result<String> fanout() {
		sender.sendFanout("hello MQ");
		return Result.success("Hello，world");
	}
	
	@RequestMapping("/mq/header")
	@ResponseBody
	public Result<String> header() {
		sender.sendHeader("hello MQ");
		return Result.success("Hello，world");
	}
	
	@RequestMapping("/hello")
	@ResponseBody
	public Result<String> home() {
		return Result.success("Hello，world");
	}

	/*
	 * @RequestMapping("/error")
	 * 
	 * @ResponseBody public Result<String> error() { return
	 * Result.error(CodeMsg.SESSION_ERROR); }
	 */

	@RequestMapping("/hello/themaleaf")
	public String themaleaf(Model model) {
		model.addAttribute("name", "Joshua");
		return "hello";
	}

	@RequestMapping("/db/get")
	@ResponseBody
	public Result<User> dbGet() {
		User user = userService.getById(1);
		return Result.success(user);
	}

	@RequestMapping("/db/tx")
	@ResponseBody
	public Result<Boolean> dbTx() {
		userService.tx();
		return Result.success(true);
	}

	@RequestMapping("/redis/get")
	@ResponseBody
	public Result<User> redisGet() {
		User result = redisService.get(UserKey.getById,"1", User.class);
		return Result.success(result);
	}

	@RequestMapping("/redis/set")
	@ResponseBody
	public Result<Boolean> redisSet(){
		User user = new User();
		user.setId(1);
		user.setName("张三");
		boolean bool = redisService.set(UserKey.getById, "1", user);
		User result = redisService.get(UserKey.getById, "1", User.class);
		System.out.println(result);
		return Result.success(bool);
		
	}
	
	@RequestMapping("/redis/exists")
	@ResponseBody
	public Result<Boolean> existsKey() {
		boolean exists = redisService.exists(UserKey.getById, "2");
		return Result.success(exists);
	}
    
	@RequestMapping("/redis/incr")
	@ResponseBody
	public Result<Long> incr() {
		Long incr = redisService.incr(null, "key1");
		return Result.success(incr);
	}
}

package com.imooc.miaosha.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.miaosha.access.AccessLimit;
import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.rabbitmq.MQSender;
import com.imooc.miaosha.rabbitmq.MiaoshaMessage;
import com.imooc.miaosha.redis.AccessKey;
import com.imooc.miaosha.redis.GoodsKey;
import com.imooc.miaosha.redis.MiaoshaKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import com.imooc.miaosha.service.GoodsService;
import com.imooc.miaosha.service.MiaoshaService;
import com.imooc.miaosha.service.MiaoshaUserService;
import com.imooc.miaosha.service.OrderService;
import com.imooc.miaosha.util.MD5Util;
import com.imooc.miaosha.util.UUIDUtil;
import com.imooc.miaosha.vo.GoodsVo;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean{

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	@Autowired
	MQSender sender;
	
	private HashMap<Long, Boolean> localOverMap =  new HashMap<Long, Boolean>();
	
	/*
	 * @RequestMapping("/do_miaosha") public String list(Model model,MiaoshaUser
	 * user,
	 * 
	 * @RequestParam("goodsId")long goodsId) { if(user == null) { return "login"; }
	 * //判断库存 GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId); int stock =
	 * goods.getStockCount(); if(stock <= 0) { model.addAttribute("errmsg",
	 * CodeMsg.MIAO_SHA_OVER.getMsg()); return "miaosha_fail"; } //判断是否已经秒杀到了
	 * MiaoshaOrder order =
	 * orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId); if(order
	 * != null) { model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
	 * return "miaosha_fail"; } //减库存 下订单 写入秒杀订单 OrderInfo orderInfo =
	 * miaoshaService.miaosha(user, goods); model.addAttribute("user", user);
	 * model.addAttribute("orderInfo", orderInfo); model.addAttribute("goods",
	 * goods); return "order_detail"; }
	 */
    
//	@RequestMapping(value="/do_miaosha", method=RequestMethod.POST)
//    @ResponseBody
//    public Result<OrderInfo> miaosha(Model model,MiaoshaUser user,
//    		@RequestParam("goodsId")long goodsId) {
//    	model.addAttribute("user", user);
//    	if(user == null) {
//    		return Result.error(CodeMsg.SESSION_ERROR);
//    	}
//    	//判断库存
//    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//10个商品，req1 req2
//    	int stock = goods.getStockCount();
//    	if(stock <= 0) {
//    		return Result.error(CodeMsg.MIAO_SHA_OVER);
//    	}
//    	//判断是否已经秒杀到了
//    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
//    	if(order != null) {
//    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
//    	}
//    	//减库存 下订单 写入秒杀订单
//    	OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
//        return Result.success(orderInfo);
//    }
	
    //使用MQ
    @RequestMapping("/{path}/do_miaosha")
    @ResponseBody
    public Result<Integer> miaosha_mq(Model model,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId,
    		@PathVariable("path") String path) {
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	
    	//验证path
    	boolean check = miaoshaService.checkPath(user, goodsId, path);
    	if(!check){
    		return Result.error(CodeMsg.REQUEST_ILLEGAL);
    	}
    	
    	//内存标记，减少redis访问
    	boolean over = localOverMap.get(goodsId);
    	if(over) {
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	
    	//预减库存，返回的是减少之后的库存
    	Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);
    	if (stock < 0) {
    		localOverMap.put(goodsId, true);
    		model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) {
    		model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	
    	//入队
    	MiaoshaMessage mm = new MiaoshaMessage();
    	mm.setGoodsId(goodsId);
    	mm.setUser(user);
    	sender.sendMiaoshaMessage(mm);
    	
        return Result.success(0);//排队中
    }
    
    @AccessLimit(maxCount = 5, seconds = 5,needLogin = true)
    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(Model model,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId,
    		@RequestParam(value="verifyCode", defaultValue="0")int verifyCode,
    		HttpServletRequest request) {
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	
    	//查询访问次数（使用上面的注解，下面的代码就不需要了）
//    	String uri = request.getRequestURI();
//    	String key = uri + "_" + user.getId();
//    	Integer count = redisService.get(AccessKey.withExpire(5), key, Integer.class);
//    	if (count == null) {
//    		redisService.set(AccessKey.withExpire(5), key, 1);
//    	}else if (count < 5){
//    		//限制5秒钟
//    		redisService.incr(AccessKey.withExpire(5), key);
//    	}else {
//    		return Result.error(CodeMsg.ACCESS_LIMIT_REACHED);
//    	}
    	
    	boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
    	if(!check) {
    		return Result.error(CodeMsg.REQUEST_ILLEGAL);
    	}
    	String path  =miaoshaService.createMiaoshaPath(user, goodsId);
        return Result.success(path);//排队中
    }
    
    /**
     * 系统初始化，把商品库存加到缓存
     */
	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		if (goodsList == null) {
			return;
		}
		for (GoodsVo goods:goodsList) {
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), goods.getStockCount());
			localOverMap.put(goods.getId(), false);
		}
	}
	
	/**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     * */
    @RequestMapping(value="/result", method=RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	long result  =miaoshaService.getMiaoshaResult(user.getId(), goodsId);
    	return Result.success(result);
    }
    
    /**
     *  生成验证码
     * @param response
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCod(HttpServletResponse response,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId) {
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	try {
    		BufferedImage image  = miaoshaService.createVerifyCode(user, goodsId);
    		OutputStream out = response.getOutputStream();
    		ImageIO.write(image, "JPEG", out);
    		out.flush();
    		out.close();
    		return null;
    	}catch(Exception e) {
    		e.printStackTrace();
    		return Result.error(CodeMsg.MIAOSHA_FAIL);
    	}
    }
}

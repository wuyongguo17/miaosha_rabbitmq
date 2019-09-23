package com.imooc.miaosha.test;

public class Son extends AbstractFather {
	private Son(int age,String name) {
		super(age,name);
	}
	
	public static Son son = new Son(23,"张三");
	
	public static void main(String[] args) {
		test(son);
	}
	
	public static void test(Son son) {
		String name = son.getName();
		System.out.println(name);
	}
}

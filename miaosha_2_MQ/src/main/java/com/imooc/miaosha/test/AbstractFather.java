package com.imooc.miaosha.test;

public abstract class AbstractFather implements Father {
	private int age;
	protected String name;
	
	public AbstractFather(int age,String name) {
		this.age = age;
		this.name = name;
	}
	
	@Override
	public int getAge() {
		return 0;
	}

	@Override
	public String getName() {
		return name + "(中国人)";
	}

}

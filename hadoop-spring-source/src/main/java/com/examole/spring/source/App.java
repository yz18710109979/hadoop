package com.examole.spring.source;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.examole.spring.source.bean.Student;

public class App {

	public static void main(String[] args) {
		ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
		Student student = ac.getBean(Student.class);
		System.out.println(student.toString());
	}
}

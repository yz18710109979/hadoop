package com.examole.spring.source;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.examole.spring.source.bean.Student;
import com.examole.spring.source.config.AppConfig;

public class App {

	public static void main(String[] args) {
		ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
		Student student = ac.getBean(Student.class);
		System.out.println(student.toString());
//		ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
//		Object stu = ac.getBean("student");
//		System.out.println(stu.toString());
	}
}

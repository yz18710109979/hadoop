package com.example.spring.boot.source;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.spring.boot.source.bean.Student;
import com.example.spring.boot.source.config.AppConfig;

@SpringBootApplication
public class App {
	public static void main(String[] args) {
//		SpringApplication.run(App.class, args);
		ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
		Student student = ac.getBean(Student.class);
		System.out.println(student.toString());
	}
}

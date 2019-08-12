package com.examole.spring.source.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.examole.spring.source.bean.Student;

@Configuration
public class AppConfig {

	@Bean
	public Student student() {
		Student stu = new Student();
		stu.setName("zs");
		stu.setAdress("bj");
		return stu;
	}
}

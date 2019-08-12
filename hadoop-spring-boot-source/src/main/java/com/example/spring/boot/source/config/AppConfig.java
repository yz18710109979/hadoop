package com.example.spring.boot.source.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.spring.boot.source.bean.Student;

@Configuration
public class AppConfig {

	@Bean
	public Student student() {
		Student stu = new Student();
		stu.setName("zs");
		return stu;
	}
}

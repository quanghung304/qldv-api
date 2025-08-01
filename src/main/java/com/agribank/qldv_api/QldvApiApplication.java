package com.agribank.qldv_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.agribank.qldvutils", "com.agribank.qldv_api"})
@EnableFeignClients
@EnableScheduling
public class QldvApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(QldvApiApplication.class, args);
	}

}

package space.learn.co.la;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class KafkaCdcConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaCdcConsumerApplication.class, args);
	}

}

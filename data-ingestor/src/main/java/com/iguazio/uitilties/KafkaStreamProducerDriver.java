package com.iguazio.drivers;

import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaStreamProducerDriver {

	private static Properties props = new Properties();
	private static KafkaProducer<String, String> kafkaProducer;

	public static void main(String[] args) {
		KafkaStreamProducerDriver driver = new KafkaStreamProducerDriver();
		driver.init();
		driver.run();

	}

	public void init() {
		props.put("bootstrap.servers", "0.0.0.0:32773");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 1500);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		kafkaProducer = new KafkaProducer<String, String>(props);

	}

	public void run() {

		double[] downtown = { -0.1195, 51.5033 };
		double[] westminster = { -0.1357, 51.4975 };
		double[] oxfordSt = { -0.1410, 51.5154 };
		double[] heathrow = { -0.4543, 51.4700 };
		double[] heathrowParking = { -0.4473, 51.4599 };
		double[] gatwick = { 0.1821, 51.1537 };
		double[] canaryWharf = { -0.0235, 51.5054 };
		double[][] locations = { downtown, westminster, oxfordSt, heathrow, heathrowParking, gatwick, canaryWharf };
		String[] driverStatus = { "busy", "available", "passenger" };

		/**
		 * producing random batches of data every 5 second.
		 */
		Random random = new Random();

		for (int x = 0; x <= 1000; x++) {
			int location = random.nextInt(7);
			int radius = random.nextInt(6);
			for (int i = 0; i <= 300; i++) {
				int driver = random.nextInt(5001);

				double longitude = locations[location][0] + random.nextDouble() * (-radius - radius) + -radius;
				double latitude = locations[location][1] + random.nextDouble() * (-radius - radius) + -radius;
				kafkaProducer.send(new ProducerRecord<String, String>("cars", Integer.toString(driver),
						driver + "," + System.currentTimeMillis() + "," + longitude + "," + latitude + ","
								+ driverStatus[random.nextInt(3)]));
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				kafkaProducer.close();
			}
			System.out.println("batch write success");
		}

	}

}

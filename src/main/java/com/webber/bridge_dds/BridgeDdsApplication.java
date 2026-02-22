package com.webber.bridge_dds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sun.jna.Library;
import com.sun.jna.Native;


@SpringBootApplication
public class BridgeDdsApplication {



	public static void main(String[] args) {

		SpringApplication.run(BridgeDdsApplication.class, args);
	}

}

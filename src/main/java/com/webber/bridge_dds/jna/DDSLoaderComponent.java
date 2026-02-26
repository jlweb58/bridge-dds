package com.webber.bridge_dds.jna;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
class DDSLoaderComponent {


    @PostConstruct
    public void init() {
        System.out.println("DDS loaded: " + DDS.INSTANCE);
        }
}


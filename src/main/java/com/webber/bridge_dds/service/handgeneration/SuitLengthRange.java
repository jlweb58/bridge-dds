package com.webber.bridge_dds.service.handgeneration;

public record SuitLengthRange(int min, int max) {
   public SuitLengthRange {
       assert min <= max;
   }
}

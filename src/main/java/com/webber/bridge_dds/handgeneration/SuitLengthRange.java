package com.webber.bridge_dds.handgeneration;

public record SuitLengthRange(int min, int max) {
   public SuitLengthRange {
       assert min <= max;
   }
}

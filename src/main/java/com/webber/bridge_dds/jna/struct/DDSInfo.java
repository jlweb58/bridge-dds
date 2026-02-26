package com.webber.bridge_dds.jna.struct;

import com.sun.jna.Structure;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class DDSInfo extends Structure {
    // Version 2.8.0 has 2, 8, 0 and a string of 2.8.0
    public int major;
    public int minor;
    public int patch;
    public byte[] versionString = new byte[10];

    // Currently 0 = unknown, 1 = Windows, 2 = Cygwin, 3 = Linux, 4 = Apple
    public int system;

    // We know 32 and 64-bit systems.
    public int numBits;

    // Currently 0 = unknown, 1 = Microsoft Visual C++, 2 = mingw, 3 = GNU g++, 4 = clang
    public int compiler;

    // Currently 0 = none, 1 = DllMain, 2 = Unix-style
    public int constructor;

    public int numCores;

    // 0 = none, 1 = Windows (native), 2 = OpenMP, 3 = GCD, 4 = Boost, 5 = STL, 6 = TBB,
    // 7 = STLIMPL (for_each), experimental only, 8 = PPLIMPL (for_each), experimental only
    public int threading;

    // The actual number of threads configured
    public int noOfThreads;

    // The string is of the form LLLSSS meaning 3 large TT memories and 3 small ones.
    public byte[] threadSizes = new byte[128];

    public byte[] systemString = new byte[1024];

    @Override
    protected List<String> getFieldOrder() {
        return List.of(
                "major", "minor", "patch", "versionString",
                "system",
                "numBits",
                "compiler",
                "constructor",
                "numCores",
                "threading",
                "noOfThreads",
                "threadSizes",
                "systemString"
        );
    }

    /** Optional convenience for JNA call patterns (pointer vs inline struct). */
    public static class ByReference extends DDSInfo implements Structure.ByReference {}
    public static class ByValue extends DDSInfo implements Structure.ByValue {}

    /** Reads a C char[] (NUL-terminated) stored in a byte[] as a Java String. */
    public static String cString(byte[] buf) {
        int len = 0;
        while (len < buf.length && buf[len] != 0) len++;
        return new String(buf, 0, len, StandardCharsets.US_ASCII);
    }

    public String getVersionString() {
        return cString(versionString);
    }

    public String getThreadSizesString() {
        return cString(threadSizes);
    }

    public String getSystemString() {
        return cString(systemString);
    }

    private static String decodeSystem(int v) {
        return switch (v) {
            case 1 -> "Windows";
            case 2 -> "Cygwin";
            case 3 -> "Linux";
            case 4 -> "Apple";
            default -> "Unknown(" + v + ")";
        };
    }

    private static String decodeCompiler(int v) {
        return switch (v) {
            case 1 -> "MSVC";
            case 2 -> "MinGW";
            case 3 -> "GNU g++";
            case 4 -> "clang";
            default -> "Unknown(" + v + ")";
        };
    }

    private static String decodeConstructor(int v) {
        return switch (v) {
            case 1 -> "DllMain";
            case 2 -> "Unix-style";
            case 0 -> "none";
            default -> "Unknown(" + v + ")";
        };
    }

    private static String decodeThreading(int v) {
        return switch (v) {
            case 0 -> "none";
            case 1 -> "Windows (native)";
            case 2 -> "OpenMP";
            case 3 -> "GCD";
            case 4 -> "Boost";
            case 5 -> "STL";
            case 6 -> "TBB";
            case 7 -> "STLIMPL (for_each) [experimental]";
            case 8 -> "PPLIMPL (for_each) [experimental]";
            default -> "Unknown(" + v + ")";
        };
    }

    public String toPrettyString() {
        String vs = getVersionString();
        if (vs.isBlank()) vs = major + "." + minor + "." + patch;

        return """
                DDSInfo {
                  version: %s (%d.%d.%d)
                  system:  %s, %d-bit
                  compiler:%s
                  ctor:    %s
                  cores:   %d
                  threading:%s (%d threads)
                  threadSizes: %s
                  systemString: %s
                }""".formatted(
                vs, major, minor, patch,
                decodeSystem(system), numBits,
                decodeCompiler(compiler),
                decodeConstructor(constructor),
                numCores,
                decodeThreading(threading), noOfThreads,
                getThreadSizesString(),
                getSystemString()
        );
    }
}

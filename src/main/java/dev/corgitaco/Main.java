package dev.corgitaco;

import sun.misc.Unsafe;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    private static final Unsafe unsafe;

    static {

        try {
            loadLibrary(Files.createTempFile(null, null));
//            System.load("F:\\development\\personal\\modding\\LandMod\\src\\main\\resources\\natives\\x86_64\\windows\\libLandMod.dll");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        try {
            var unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("Failed to get instance of sun.misc.Unsafe", e);
        }
    }

    private static void loadLibrary(Path tempFile) throws IOException {
        String path = "/natives/%s".formatted(Architecture.current().prefix + "/" + OperatingSystem.current().suffix);
        InputStream resourceAsStream = Main.class.getResourceAsStream(path);
        Files.write(tempFile, resourceAsStream.readAllBytes());
        System.load(tempFile.toAbsolutePath().toString());
    }



    public static void main(String[] args) {
        long startTimeMs = System.currentTimeMillis();
        double totalNoise = 0;


        int chunkX = 0;
        int minChunkY = 0;
        int maxChunkY = 2500;
        int chunkZ = 5;
        int size = 16;
        float freq = 0.5F;
        int seed = 900;


        long[] uniformGrid3DPointers = NoiseGenerator.getUniformGrid3DPointer(chunkX, minChunkY, maxChunkY, chunkZ, size, freq, seed);


        for (int j = 0, uniformGrid3DPointersLength = uniformGrid3DPointers.length; j < uniformGrid3DPointersLength; j += 2) {
            long uniformGrid3DPointer = uniformGrid3DPointers[j];

            long grid3DAddress = uniformGrid3DPointer;
            long grid3DSize = uniformGrid3DPointers[j + 1];

            for (long gridSize = 0; gridSize < grid3DSize; gridSize++) {
                float noiseResult = unsafe.getFloat(grid3DAddress + (Float.BYTES * gridSize));
                totalNoise += noiseResult;
            }
        }

        NoiseGenerator.freeUniformGrid3DPointer(uniformGrid3DPointers);


        System.out.printf("Time taken: %d, Total: %f\n", System.currentTimeMillis() - startTimeMs, totalNoise);
    }

    /**
     * Get unsafe
     *
     * @return unsafe
     */
    public static Unsafe getUnsafe() {
        return unsafe;
    }

    enum Architecture {
        X64("x86_64"),
        ARM64("aarch64"),
        I386("i686");
        private final String prefix;

        Architecture(String prefix) {
            this.prefix = prefix;
        }

        static Architecture current() {
            final String arch = System.getProperty("os.arch");

            if ("aarch64".equals(arch) || arch.startsWith("armv8")) {
                return ARM64;
            } else if (arch.contains("64")) {
                return X64;
            }

            return I386; // Default
        }
    }

    enum OperatingSystem {
        MAC_OS("macos/libLandMod.dylib"),
        LINUX("linux/libLandMod.so"),
        WINDOWS("windows/libLandMod.dll");
        private final String suffix;


        OperatingSystem(String suffix) {
            this.suffix = suffix;
        }

        static OperatingSystem current() {
            final String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("win")) {
                return WINDOWS;
            } else if (osName.contains("mac")) {
                return MAC_OS;
            } else {
                return LINUX; // Also default
            }
        }
    }
}
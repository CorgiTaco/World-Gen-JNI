package dev.corgitaco;

public class NoiseGenerator {

    public static native long[] getUniformGrid3DPointer(int chunkX, int minChunkY, int maxChunkY, int chunkZ, int size, float freq, int seed);

    public static native void freeUniformGrid3DPointer(long[] sectionPointers);

}

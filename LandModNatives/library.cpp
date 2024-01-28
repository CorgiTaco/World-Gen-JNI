#include <iostream>
#include <vector>
#include <string>
#include <format>
#include <chrono>
#include <FastNoise/FastNoise.h>
#include "JNIBinding.h"


FastNoise::SmartNode<FastNoise::FractalFBm> getFastNoise() {
    auto noise = FastNoise::New<FastNoise::OpenSimplex2S>();
    auto fnFractal = FastNoise::New<FastNoise::FractalFBm>();
    fnFractal->SetOctaveCount(5);
    fnFractal->SetSource(noise);

    return fnFractal;
}

const auto noise = getFastNoise();

int main() {
    int chunkX = 0;
    int minChunkY = 0;
    volatile int maxChunkY = 50000;
    int chunkZ = 5;

    const int size = 16;
    float frequency = 0.5;
    int seed = 900;

    int length = maxChunkY - minChunkY;

    float noiseOutput[size * size * size] = {};

    double val = 0;
    for (int sectionY = 0; sectionY < length; sectionY++) {
        noise->GenUniformGrid3D(noiseOutput, chunkX << 4, (minChunkY + sectionY) << 4, chunkZ << 4, size, size, size,
                                frequency, seed);

        for (float values: noiseOutput) {
            val += values;
        }
    }
    std::cout << val;
}


/*
 * Class:     dev_corgitaco_NoiseGenerator
 * Method:    getUniformGrid3DPointer
 * Signature: (IIIIIFI)[J
 */
JNIEXPORT jlongArray JNICALL Java_dev_corgitaco_NoiseGenerator_getUniformGrid3DPointer
(JNIEnv *env, jclass Main, jint chunkX, jint minChunkY, jint maxChunkY, jint chunkZ, jint size, jfloat frequency,
 jint seed) {
    int length = maxChunkY - minChunkY;

    auto pointers = env->NewLongArray(2 * length);

    for (size_t sectionY = 0; sectionY < length; sectionY++) {
        long long noiseOutputSizeRaw = size * size * size;
        float *noiseOutput = new float[noiseOutputSizeRaw];

        noise->GenUniformGrid3D(noiseOutput, chunkX << 4, (minChunkY + sectionY) << 4, chunkZ << 4, size, size, size,
                                frequency, seed);

        // long long* noiseOutputSize = new long long[1] {(long long)noiseOutputSizeRaw};

        env->SetLongArrayRegion(pointers, (sectionY * 2), 1, reinterpret_cast<jlong *>(&noiseOutput));
        env->SetLongArrayRegion(pointers, (sectionY * 2) + 1, 1, &noiseOutputSizeRaw);
    }
    return pointers;
}

/*
 * Class:     dev_corgitaco_NoiseGenerator
 * Method:    freeUniformGrid3DPointer
 * Signature: ([J)V
 */
JNIEXPORT void JNICALL Java_dev_corgitaco_NoiseGenerator_freeUniformGrid3DPointer
(JNIEnv *env, jclass Main, jlongArray sectionPointers) {
    auto boolean = false;
    auto elements = env->GetLongArrayElements(sectionPointers, reinterpret_cast<jboolean *>(&boolean));
    auto arrayLength = env->GetArrayLength(sectionPointers);

    for (size_t i = 0; i < arrayLength; i += 2) {
        delete reinterpret_cast<float *>(elements[i]);
    }
}

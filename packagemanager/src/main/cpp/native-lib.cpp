#include <jni.h>
#include <string>


extern "C" {
    extern int main(int argc, const char *argv[]);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_bsdiff_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT int JNICALL
Java_lib_bsdiff_PatchUtils_bsPatch(JNIEnv *env, jobject instance, jstring oldApk_,
                                          jstring patch_, jstring output_) {
    const char *oldApk = env->GetStringUTFChars(oldApk_, 0);
    const char *patch = env->GetStringUTFChars(patch_, 0);
    const char *output = env->GetStringUTFChars(output_, 0);

    //TODO
    const char *argv[] = {"", oldApk, output, patch};
    main(4, argv);

    env->ReleaseStringUTFChars(oldApk_, oldApk);
    env->ReleaseStringUTFChars(patch_, patch);
    env->ReleaseStringUTFChars(output_, output);

    return 0;
}
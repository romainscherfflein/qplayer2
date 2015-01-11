/**
 * openSLES_Wrap.c
 *
 * Author:         Claus Chierici (claus@antamauna.net)
 *                 Code take from http://stackoverflow.com/users/1340329/vipul-purohit
 *                 Thank you very much
 * Copyright 2012: Claus Chierici, all rights reserved
 * License:        GPL V2
 */

#include <jni.h>
#include <android/log.h>

#define LOG_TAG "QPLAYER-NATIVE : "
#define LOG_INF(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOG_DBG(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOG_ERR(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

#include <assert.h>
#include <sys/types.h>

// engine interfaces
static SLObjectItf       engineObject = NULL;
static SLEngineItf       engineEngine;

// URI player interfaces
static SLObjectItf       uriPlayerObject = NULL;
static SLPlayItf         uriPlayerPlay;
static SLSeekItf         uriPlayerSeek;
static SLPlaybackRateItf uriPlaybackRate;

// output mix interfaces
static SLObjectItf outputMixObject = NULL;

// playback rate (def: 1x:1000)
static SLpermille playbackMinRate = 500;
static SLpermille playbackMaxRate = 2000;
static SLpermille playbackRateStepSize;

// pitch
static SLPitchItf uriPlaybackPitch;
static SLpermille playbackMinPitch = 500;
static SLpermille playbackMaxPitch = 2000;

JNIEXPORT jboolean
Java_net_antamauna_android_djplayer_mediaplayer_QMediaPlayer_createEngine(
		JNIEnv* env, jclass clazz) {
	LOG_INF("createEngine(): called");

	SLresult res;

	// create engine
	LOG_DBG("1. creating engine ...");
	res = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
	if (SL_RESULT_SUCCESS != res) return JNI_FALSE;

	// realize engine
	LOG_DBG("2. realizing engine ...");
	res = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
	if (SL_RESULT_SUCCESS != res) return JNI_FALSE;

	// get the engine interface, which is needed in order to create other objects
	LOG_DBG("3. getting engine interface");
	res = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
	if (SL_RESULT_SUCCESS != res) return JNI_FALSE;

    // create output mix, with environmental reverb specified as a non-required interface
    LOG_DBG("4. create output mix");
    const SLInterfaceID ids[1] = {SL_IID_PLAYBACKRATE};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    res = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, ids, req);
    if (SL_RESULT_SUCCESS != res) return JNI_FALSE;

    // realize the output mix
    LOG_DBG("5. realize the output mix");
    res = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != res) return JNI_FALSE;
    else return JNI_TRUE;
}

JNIEXPORT jboolean
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_releaseEngine(
		JNIEnv* env, jclass clazz) {
	LOG_INF("releaseEngine(): called");

	// destroy URI audio player object, and invalidate all associated interfaces
	if (uriPlayerObject != NULL) {
		(*uriPlayerObject)->Destroy(uriPlayerObject);
		uriPlayerObject = NULL;
		uriPlayerPlay = NULL;
		uriPlayerSeek = NULL;
	}

	// destroy output mix object, and invalidate all associated interfaces
	if (outputMixObject != NULL) {
		(*outputMixObject)->Destroy(outputMixObject);
		outputMixObject = NULL;
	}

	// destroy engine object, and invalidate all associated interfaces
	if (engineObject != NULL) {
		(*engineObject)->Destroy(engineObject);
		engineObject = NULL;
		engineEngine = NULL;
	}
	return JNI_TRUE;
}

void playStatusCallback(SLPlayItf play, void* context, SLuint32 event) {
    LOG_DBG("playStatusCallback(): event: %i", event);
}

JNIEXPORT jboolean
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_createAudioPlayer(
        JNIEnv* env, jclass clazz, jstring uri) {
	LOG_INF("createAudioPlayer(): called");

	SLresult result;

    // convert Java string to UTF-8
    const jbyte *utf8 = (*env)->GetStringUTFChars(env, uri, NULL);
    assert(NULL != utf8);

    // configure audio source
    // (requires the INTERNET permission depending on the uri parameter)
    SLDataLocator_URI loc_uri          = { SL_DATALOCATOR_URI,
    		                               (SLchar *) utf8 };
    SLDataFormat_MIME format_mime      = { SL_DATAFORMAT_MIME,
                                           NULL,
                                           SL_CONTAINERTYPE_UNSPECIFIED };
    SLDataSource audioSrc              = { &loc_uri, &format_mime };

    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = { SL_DATALOCATOR_OUTPUTMIX,
                                           outputMixObject };
    SLDataSink audioSnk                = { &loc_outmix, NULL };

    // create audio player
    const SLInterfaceID ids[2]         = { SL_IID_SEEK, SL_IID_PLAYBACKRATE };
    const SLboolean req[2]             = { SL_BOOLEAN_FALSE, SL_BOOLEAN_TRUE };

    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &uriPlayerObject,
            &audioSrc, &audioSnk, 2, ids, req);
    // note that an invalid URI is not detected here, but during prepare/prefetch on Android,
    // or possibly during Realize on other platforms
    assert(SL_RESULT_SUCCESS == result);

    // release the Java string and UTF-8
    (*env)->ReleaseStringUTFChars(env, uri, utf8);

    // realize the player
    result = (*uriPlayerObject)->Realize(uriPlayerObject, SL_BOOLEAN_FALSE);
    // this will always succeed on Android, but we check result for portability to other platforms
    if (SL_RESULT_SUCCESS != result) {
        (*uriPlayerObject)->Destroy(uriPlayerObject);
        uriPlayerObject = NULL;
        return JNI_FALSE;
    }

    // get the play interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_PLAY,
            &uriPlayerPlay);
    assert(SL_RESULT_SUCCESS == result);

    // get the seek interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_SEEK,
            &uriPlayerSeek);
    assert(SL_RESULT_SUCCESS == result);

    // get playback rate interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject,
            SL_IID_PLAYBACKRATE, &uriPlaybackRate);
    assert(SL_RESULT_SUCCESS == result);

    // get playback pitch interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_PITCH, &uriPlaybackPitch);
    assert(SL_RESULT_SUCCESS == result);

    // register callback function
    result = (*uriPlayerPlay)->RegisterCallback(uriPlayerPlay,
            playStatusCallback, 0);
    assert(SL_RESULT_SUCCESS == result);
    result = (*uriPlayerPlay)->SetCallbackEventsMask(uriPlayerPlay,
            SL_PLAYEVENT_HEADATEND); // head at end
    assert(SL_RESULT_SUCCESS == result);

    SLmillisecond msec;
    result = (*uriPlayerPlay)->GetDuration(uriPlayerPlay, &msec);
    assert(SL_RESULT_SUCCESS == result);

    // no loop
    result = (*uriPlayerSeek)->SetLoop(uriPlayerSeek, SL_BOOLEAN_TRUE, 0, msec);
    assert(SL_RESULT_SUCCESS == result);

    SLuint32 capa;
    result = (*uriPlaybackRate)->GetRateRange(uriPlaybackRate, 0,
    		&playbackMinRate, &playbackMaxRate, &playbackRateStepSize, &capa);

    assert(SL_RESULT_SUCCESS == result);

    result = (*uriPlaybackRate)->SetPropertyConstraints(uriPlaybackRate,
    		SL_RATEPROP_PITCHCORAUDIO);

    if (SL_RESULT_PARAMETER_INVALID == result) {
    	LOG_ERR("Parameter Invalid");
    }
    if (SL_RESULT_FEATURE_UNSUPPORTED == result) {
    	LOG_ERR("Feature Unsupported");
    }
    if (SL_RESULT_SUCCESS == result) {
    	assert(SL_RESULT_SUCCESS == result);
    	LOG_DBG("Success");
    }

    /*
     result = (*uriPlaybackPitch)->GetPitchCapabilities(uriPlaybackPitch, &playbackMinPitch, &playbackMaxPitch);
     assert(SL_RESULT_SUCCESS == result);*/

    /*
     SLpermille minRate, maxRate, stepSize, rate = 1000;
     SLuint32 capa;
     (*uriPlaybackRate)->GetRateRange(uriPlaybackRate, 0, &minRate, &maxRate, &stepSize, &capa);

     (*uriPlaybackRate)->SetRate(uriPlaybackRate, minRate);
     */

    return JNI_TRUE;
}

JNIEXPORT jboolean
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_releaseAudioPlayer(
        JNIEnv* env, jclass clazz) {
	LOG_INF("releaseAudioPlayer(): called");

	// destroy URI audio player object, and invalidate all associated interfaces
    if (uriPlayerObject != NULL) {
        (*uriPlayerObject)->Destroy(uriPlayerObject);
        uriPlayerObject = NULL;
        uriPlayerPlay = NULL;
        uriPlayerSeek = NULL;
        uriPlaybackRate = NULL;
    }
    return JNI_TRUE;
}

void setPlayState(SLuint32 state) {
	LOG_INF("setPlayState(): called");

    SLresult result;

    // make sure the URI audio player was created
    if (NULL != uriPlayerPlay) {

        // set the player's state
        result = (*uriPlayerPlay)->SetPlayState(uriPlayerPlay, state);
        assert(SL_RESULT_SUCCESS == result);
    }
}

SLuint32 getPlayState() {
	LOG_INF("getPlayState(): called");

	SLresult result;

    // make sure the URI audio player was created
    if (NULL != uriPlayerPlay) {

        SLuint32 state;
        result = (*uriPlayerPlay)->GetPlayState(uriPlayerPlay, &state);
        assert(SL_RESULT_SUCCESS == result);

        return state;
    }
    return 0;
}

JNIEXPORT void
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_play(JNIEnv* env,
        jclass clazz) {
	LOG_INF("JNI: play(): called");
    setPlayState(SL_PLAYSTATE_PLAYING);
}

JNIEXPORT void
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_stop(JNIEnv* env,
        jclass clazz) {
	LOG_INF("JNI: stop(): called");
    setPlayState(SL_PLAYSTATE_STOPPED);
}

JNIEXPORT void
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_pause(JNIEnv* env,
        jclass clazz) {
	LOG_INF("JNI: pause(): called");
    setPlayState(SL_PLAYSTATE_PAUSED);
}

JNIEXPORT jboolean
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_isPlaying(
        JNIEnv* env, jclass clazz) {
	LOG_INF("JNI: isPlaying(): called");
    return (getPlayState() == SL_PLAYSTATE_PLAYING);
}

JNIEXPORT jboolean
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_isPaused(
        JNIEnv* env, jclass clazz) {
	LOG_INF("JNI: isPaused(): called");
    return (getPlayState() == SL_PLAYSTATE_PAUSED);
}

JNIEXPORT jboolean
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_isStopped(
        JNIEnv* env, jclass clazz) {
	LOG_INF("JNI: isStopped(): called");
    return (getPlayState() == SL_PLAYSTATE_STOPPED);
}

JNIEXPORT void
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_seekTo(
        JNIEnv* env, jclass clazz, jint position) {
	LOG_INF("JNI: seekTo(): called");

    if (NULL != uriPlayerPlay) {

        //SLuint32 state = getPlayState();
        //setPlayState(SL_PLAYSTATE_PAUSED);

        SLresult result;

        result = (*uriPlayerSeek)->SetPosition(uriPlayerSeek, position,
                SL_SEEKMODE_FAST);
        assert(SL_RESULT_SUCCESS == result);

        //setPlayState(state);
    }
}

JNIEXPORT jint
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_getDuration(
        JNIEnv* env, jclass clazz) {
//	LOG_INF("JNI: getDuration(): called");

	if (NULL != uriPlayerPlay) {

        SLresult result;

        SLmillisecond msec;
        result = (*uriPlayerPlay)->GetDuration(uriPlayerPlay, &msec);
        assert(SL_RESULT_SUCCESS == result);

        return msec;
    }
    return 0.0f;
}

JNIEXPORT jint
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_getCurrentPosition(
        JNIEnv* env, jclass clazz) {
//	LOG_INF("JNI: getPosition(): called");

    if (NULL != uriPlayerPlay) {

        SLresult result;

        SLmillisecond msec;
        result = (*uriPlayerPlay)->GetPosition(uriPlayerPlay, &msec);
        assert(SL_RESULT_SUCCESS == result);

        return msec;
    }

    return 0.0f;
}

JNIEXPORT void
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_setPitch(
        JNIEnv* env, jclass clazz, jint rate) {
	LOG_INF("JNI: setPitch(): called. rate: %i", rate);

    if (NULL != uriPlaybackPitch) {
        SLresult result;

        result = (*uriPlaybackPitch)->SetPitch(uriPlaybackPitch, rate);
        assert(SL_RESULT_SUCCESS == result);
    }
}

JNIEXPORT void
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_setPlaybackRate(
        JNIEnv* env, jclass clazz, jint rate) {
	LOG_INF("JNI: setPlaybackRate(): called. rate: %i", rate);
    if (NULL != uriPlaybackRate) {
        SLresult result;

        result = (*uriPlaybackRate)->SetRate(uriPlaybackRate, rate);
            assert(SL_RESULT_SUCCESS == result);
    }
}

JNIEXPORT jint
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_getPlaybackRate(
        JNIEnv* env, jclass clazz) {
	LOG_INF("JNI: getPlaybackRate(): called");

	if (NULL != uriPlaybackRate) {
        SLresult result;

        SLpermille rate;
        result = (*uriPlaybackRate)->GetRate(uriPlaybackRate, &rate);
        assert(SL_RESULT_SUCCESS == result);

        return rate;
    }
    return 0;
}

JNIEXPORT jboolean
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_setLoop(
        JNIEnv* env, jclass clazz, jint startPos, jint endPos) {
	LOG_INF("JNI: setLoop(): called. startPos: %i, endPos: %i", startPos, endPos);

	SLresult result;

    result = (*uriPlayerSeek)->SetLoop(uriPlayerSeek, SL_BOOLEAN_TRUE, startPos,
            endPos);
    assert(SL_RESULT_SUCCESS == result);

    return JNI_TRUE;
}

JNIEXPORT jboolean
Java_org_qstuff_qplayer_player_QNativeMediaPlayer_setNoLoop(
        JNIEnv* env, jclass clazz) {
	LOG_INF("JNI: setNoLoop(): called");

    SLresult result;

    if (NULL != uriPlayerSeek) {
        // enable whole file looping
        result = (*uriPlayerSeek)->SetLoop(uriPlayerSeek, SL_BOOLEAN_TRUE, 0,
                SL_TIME_UNKNOWN);
        assert(SL_RESULT_SUCCESS == result);

    }
    return JNI_TRUE;
}

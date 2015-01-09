package com.tqcenglish.vlcdemo;
interface IAudioPlayerService {
    void play(String filePath);
    boolean pause();
    void stop();
    long duration();
    long position();
    int seek(int position);
    void mute();
    void unmute();
    boolean isPlaying();
    boolean isPaused();
    boolean isStopped();
    boolean isMuted();
}
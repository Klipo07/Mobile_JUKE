package com.example.myapplication.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

public class GameViewModel extends ViewModel {
    
    // Состояние игры
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private final MutableLiveData<Long> timeLeft = new MutableLiveData<>(0L);
    private final MutableLiveData<Boolean> isGameRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isGamePaused = new MutableLiveData<>(false);
    private final MutableLiveData<Double> goldRate = new MutableLiveData<>(10491.49);
    
    // Getters для LiveData
    public LiveData<Integer> getScore() {
        return score;
    }
    
    public LiveData<Long> getTimeLeft() {
        return timeLeft;
    }
    
    public LiveData<Boolean> getIsGameRunning() {
        return isGameRunning;
    }
    
    public LiveData<Boolean> getIsGamePaused() {
        return isGamePaused;
    }
    
    public LiveData<Double> getGoldRate() {
        return goldRate;
    }
    
    // Методы для обновления состояния
    public void setScore(int newScore) {
        score.setValue(newScore);
    }
    
    public void addScore(int points) {
        Integer currentScore = score.getValue();
        if (currentScore != null) {
            score.setValue(currentScore + points);
        }
    }
    
    public void setTimeLeft(long time) {
        timeLeft.setValue(time);
    }
    
    public void setGameRunning(boolean running) {
        isGameRunning.setValue(running);
    }
    
    public void setGamePaused(boolean paused) {
        isGamePaused.setValue(paused);
    }
    
    public void setGoldRate(double rate) {
        goldRate.setValue(rate);
    }
    
    // Методы для управления игрой
    public void startGame() {
        setGameRunning(true);
        setGamePaused(false);
    }
    
    public void pauseGame() {
        setGamePaused(true);
    }
    
    public void resumeGame() {
        setGamePaused(false);
    }
    
    public void stopGame() {
        setGameRunning(false);
        setGamePaused(false);
    }
    
    public void resetGame() {
        setScore(0);
        setTimeLeft(0);
        setGameRunning(false);
        setGamePaused(false);
    }
    
    // Получить текущие значения
    public int getCurrentScore() {
        Integer currentScore = score.getValue();
        return currentScore != null ? currentScore : 0;
    }
    
    public long getCurrentTimeLeft() {
        Long currentTime = timeLeft.getValue();
        return currentTime != null ? currentTime : 0;
    }
    
    public boolean isGameCurrentlyRunning() {
        Boolean running = isGameRunning.getValue();
        return running != null ? running : false;
    }
    
    public boolean isGameCurrentlyPaused() {
        Boolean paused = isGamePaused.getValue();
        return paused != null ? paused : false;
    }
    
    public double getCurrentGoldRate() {
        Double rate = goldRate.getValue();
        return rate != null ? rate : 10491.49;
    }
}

package com.shayakum.CardComposerService.utils.patterns.obs;

public interface Subject {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}

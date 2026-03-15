package lab;

@FunctionalInterface
public interface LivesListener {
    void onLivesChanged(int newLives);
}

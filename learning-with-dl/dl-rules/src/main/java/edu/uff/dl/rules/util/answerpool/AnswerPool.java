/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.util.answerpool;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Victor
 * @param <T>
 */
public class AnswerPool<T> {

    Set<T> answerPool;
    T bestAnswer;
    Comparator<T> comparator;

    public AnswerPool(Comparator<T> comparator) {
        this.answerPool = new LinkedHashSet<>();
        this.comparator = comparator;
    }

    public void addAnswer(T answer) {
        answerPool.add(answer);

        if (bestAnswer == null) {
            bestAnswer = answer;
        } else {
            bestAnswer = (comparator.compare(bestAnswer, answer) > 0 ? answer : bestAnswer);
        }

    }

    public boolean addAnswerIfNotWorse(T answer) {
        if (bestAnswer == null) {
            answerPool.add(answer);
            bestAnswer = answer;
            return true;
        } else if (! (comparator.compare(bestAnswer, answer) < 0)) {
            answerPool.add(answer);

            if (comparator.compare(bestAnswer, answer) > 0) {
                bestAnswer = answer;
            }

            return true;
        }

        return false;
    }

    public boolean isBetter(T answer) {
        return (bestAnswer == null || comparator.compare(bestAnswer, answer) > 0);
    }

    public Set<T> getAnswerPool() {
        return answerPool;
    }

    public T getBestAnswer() {
        return bestAnswer;
    }

    public Comparator<? super T> getComparator() {
        return comparator;
    }

}

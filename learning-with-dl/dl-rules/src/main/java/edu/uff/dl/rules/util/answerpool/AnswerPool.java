/*
 * UFF Project Semantic Learning
 */
package edu.uff.dl.rules.util.answerpool;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Generic class to keep a {@link Set} of answers. This class can keep any kind
 * of class as answers and compare this answers with a specific criteria
 * represented by a comparator.
 *
 * @author Victor Guimarães
 * @param <T> the answer's type.
 */
public class AnswerPool<T> {

    Set<T> answerPool;
    T bestAnswer;
    Comparator<T> comparator;

    /**
     * Constructor with all needed parameters.
     *
     * @param comparator the comparator of answers.
     */
    public AnswerPool(Comparator<T> comparator) {
        this.answerPool = new LinkedHashSet<>();
        this.comparator = comparator;
    }

    /**
     * Add a answer to the pool.
     *
     * @param answer the answer.
     */
    public void addAnswer(T answer) {
        answerPool.add(answer);

        if (bestAnswer == null) {
            bestAnswer = answer;
        } else {
            bestAnswer = (comparator.compare(bestAnswer, answer) > 0 ? answer : bestAnswer);
        }

    }

    /**
     * Add a answer to the pool only if the pool does not have a better answer
     * than this new one.
     *
     * @param answer the answer.
     * @return true if the answer has been added, false otherwise.
     */
    public boolean addAnswerIfNotWorse(T answer) {
        if (bestAnswer == null) {
            answerPool.add(answer);
            bestAnswer = answer;
            return true;
        } else if (!(comparator.compare(bestAnswer, answer) < 0)) {
            answerPool.add(answer);

            if (comparator.compare(bestAnswer, answer) > 0) {
                bestAnswer = answer;
            }

            return true;
        }

        return false;
    }

    /**
     * Check if the given answer are better than the best pool's answer.
     *
     * @param answer the answer
     * @return true if it is, false otherwise.
     */
    public boolean isBetter(T answer) {
        return (bestAnswer == null || comparator.compare(bestAnswer, answer) > 0);
    }

    /**
     * Getter for the {@link Set} of answers.
     *
     * @return the {@link Set} of answers.
     */
    public Set<T> getAnswerPool() {
        return answerPool;
    }

    /**
     * Getter for the best answer.
     *
     * @return the best answer.
     */
    public T getBestAnswer() {
        return bestAnswer;
    }

    /**
     * Getter for the {@link Comparator}.
     *
     * @return the the {@link Comparator}.
     */
    public Comparator<? super T> getComparator() {
        return comparator;
    }

}

package com.agni.asus.quiz;

import java.util.ArrayList;

/**
 * Created by ASUS on 12/27/2017.
 */

public class QuestionModel {
    String category;
    String type;
    String difficulty;
    String question;
    String correct_answer;
    ArrayList<String> incorrect_answers;

    public QuestionModel(String category, String type, String difficulty, String question, String correct_answer, ArrayList<String> incorrect_answers) {
        this.category = category;
        this.type = type;
        this.difficulty = difficulty;
        this.question = question;
        this.correct_answer = correct_answer;
        this.incorrect_answers = incorrect_answers;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public String getCorrect_answer() {
        return correct_answer;
    }

    public ArrayList<String> getIncorrect_answers() {
        return incorrect_answers;
    }
}

package com.example.schoolproject.Models;

import com.google.firebase.database.Exclude;
import java.util.ArrayList;

public class Question {

    private String examType,Course,Unit,Lecturer,University,Grade,Semester,Question,Answer;
    private Boolean mSelected = false;
    private String quizKey,item;
    private ArrayList<String> mAttributes;
    public Question() {
    }

    public Question(String quizKey,String examType, String course, String unit, String lecturer,
                     String university, String grade, String semester,String question,String Answer) {
        this.quizKey = quizKey;
        this.examType = examType;
        Course = course;
        Unit = unit;
        Lecturer = lecturer;
        University = university;
        Grade = grade;
        Semester = semester;
        Question = question;
        this.Answer = Answer;
    }

    public Question(String item, boolean selected) {
        this.item = item;
        this.mSelected = Boolean.valueOf(selected);
    }

    public String getQuizKey() {
        return quizKey;
    }

    public void setQuizKey(String quizKey) {
        this.quizKey = quizKey;
    }

    public String getAnswer() {
        return Answer;
    }

    public void setAnswer(String answer) {
        Answer = answer;
    }

    public ArrayList<String> getmAttributes() {
        return mAttributes;
    }

    public void setmAttributes(ArrayList<String> mAttributes) {
        this.mAttributes = mAttributes;
    }

    public String getQuestion() {
        return Question;
    }

    @Exclude
    public void setSelected(boolean selected) {
        this.mSelected = Boolean.valueOf(selected);
    }

    @Exclude
    public boolean isSelected() {
        return this.mSelected.booleanValue();
    }

    public void setQuestion(String question) {
        Question = question;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public String getCourse() {
        return Course;
    }

    public void setCourse(String course) {
        Course = course;
    }

    public String getUnit() {
        return Unit;
    }

    public void setUnit(String unit) {
        Unit = unit;
    }

    public String getLecturer() {
        return Lecturer;
    }

    public void setLecturer(String lecturer) {
        Lecturer = lecturer;
    }

    public String getUniversity() {
        return University;
    }

    public void setUniversity(String university) {
        University = university;
    }

    public String getGrade() {
        return Grade;
    }

    public void setGrade(String grade) {
        Grade = grade;
    }

    public String getSemester() {
        return Semester;
    }

    public void setSemester(String semester) {
        Semester = semester;
    }
}

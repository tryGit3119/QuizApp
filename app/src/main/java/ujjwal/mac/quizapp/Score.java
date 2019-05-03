package ujjwal.mac.quizapp;

public class Score {
    String date, time;
    int score;

    public Score(String date, String time, int score) {
        this.date = date;
        this.time = time;
        this.score = score;
    }

    public Score(){

    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}

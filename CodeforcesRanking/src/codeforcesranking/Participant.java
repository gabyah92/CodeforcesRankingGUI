package codeforcesranking;

class Participant {

    final private String handle;
    private int rating;
    private int rank;

    public Participant(String handle, int rating) {
        this.handle = handle;
        this.rating = rating;
        this.rank = 0;
    }

    public void setScore(int score) {
        this.rating = score;
    }

    public String getHandle() {
        return handle;
    }

    public int getRating() {
        return rating;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}

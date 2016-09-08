package jishbit.main;

public enum Subs {
    BPT("1", "blackpeopletwitter"),
    MEIRL("2", "meirl"),
    ME_IRL("3", "me_irl"),
    MEMES("4", "memes"),
    AA("5", "adviceanimals"),
    FUNNY("6", "funny");

    public String number;
    public String subreddit;

    Subs(String number, String subreddit) {
        this.number = number;
        this.subreddit = subreddit;
    }

    public static Subs getSubReddit(String number) {
        for (Subs sub : values()) {
            if (sub.number.equalsIgnoreCase(number)) {
                return sub;
            }
        }
        return null;
    }
}

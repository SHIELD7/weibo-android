package site.imcu.weibo.po;

public class UserVo extends User {
    private String bir_String;
    private Relation relation;
    private int age;
    private Mention mention;
    private Mention notice;
    private int weiboCount;
    private int followCount;
    private int fansCount;

    public String getBir_String() {
        return bir_String;
    }

    public void setBir_String(String bir_String) {
        this.bir_String = bir_String;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Mention getMention() {
        return mention;
    }

    public void setMention(Mention mention) {
        this.mention = mention;
    }

    public Mention getNotice() {
        return notice;
    }

    public void setNotice(Mention notice) {
        this.notice = notice;
    }

    public int getWeiboCount() {
        return weiboCount;
    }

    public void setWeiboCount(int weiboCount) {
        this.weiboCount = weiboCount;
    }

    public int getFollowCount() {
        return followCount;
    }

    public void setFollowCount(int followCount) {
        this.followCount = followCount;
    }

    public int getFansCount() {
        return fansCount;
    }

    public void setFansCount(int fansCount) {
        this.fansCount = fansCount;
    }

    @Override
    public String toString() {
        return "UserVo{" +
                "bir_String='" + bir_String + '\'' +
                ", relation=" + relation +
                ", age=" + age +
                ", mention=" + mention +
                ", notice=" + notice +
                ", weiboCount=" + weiboCount +
                ", followCount=" + followCount +
                ", fansCount=" + fansCount +
                '}' + super.toString();
    }
}

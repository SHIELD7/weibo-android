package site.imcu.weibo.po;

public class WeiboVo extends Weibo{
    private User user;
    private String date;
    private WeiboVo repost;
    private int likes;
    private int imgCount;
    private int collect;
    private int repostCount;
    private int commentCount;
    private int likeCount;

    public int getImgCount() {
        return imgCount;
    }

    public void setImgCount(int imgCount) {
        this.imgCount = imgCount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public WeiboVo getRepost() {
        return repost;
    }

    public void setRepost(WeiboVo repost) {
        this.repost = repost;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getCollect() {
        return collect;
    }

    public void setCollect(int collect) {
        this.collect = collect;
    }

    public int getRepostCount() {
        return repostCount;
    }

    public void setRepostCount(int repostCount) {
        this.repostCount = repostCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    @Override
    public String toString() {
        return "WeiboVo{" +
                "user=" + user +
                ", date='" + date + '\'' +
                ", repost=" + repost +
                ", likes=" + likes +
                ", collect=" + collect +
                ", repostCount=" + repostCount +
                ", commentCount=" + commentCount +
                ", likeCount=" + likeCount +
                '}' + super.toString();
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}

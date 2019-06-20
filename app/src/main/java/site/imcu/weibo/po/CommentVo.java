package site.imcu.weibo.po;

import java.util.List;

public class CommentVo extends Comment {
    // 扩展User
    private User user;

    // 被评论微博用户昵称
    private String weibo_nickname;

    private Weibo weibo;

    // json评论时间
    private String time;

    // 评论回复数
    private int countReply;

    // 回复list
    private List<ReplyVo> replyList;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getWeibo_nickname() {
        return weibo_nickname;
    }

    public void setWeibo_nickname(String weibo_nickname) {
        this.weibo_nickname = weibo_nickname;
    }

    public Weibo getWeibo() {
        return weibo;
    }

    public void setWeibo(Weibo weibo) {
        this.weibo = weibo;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCountReply() {
        return countReply;
    }

    public void setCountReply(int countReply) {
        this.countReply = countReply;
    }

    public List<ReplyVo> getReplyList() {
        return replyList;
    }

    public void setReplyList(List<ReplyVo> replyList) {
        this.replyList = replyList;
    }

    @Override
    public String toString() {
        return super.toString() + "CommentVo{" +
                "user=" + user +
                ", weibo_nickname='" + weibo_nickname + '\'' +
                ", weibo=" + weibo +
                ", time='" + time + '\'' +
                ", countReply=" + countReply +
                ", replyList=" + replyList +
                '}';
    }
}

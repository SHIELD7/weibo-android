package site.imcu.weibo.po;

public class Relation {
    private Integer relationId;
    private Integer userId;
    private Integer followId;
    private Integer state;

    public Integer getRelationId() {
        return relationId;
    }

    public void setRelationId(Integer relationId) {
        this.relationId = relationId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getFollowId() {
        return followId;
    }

    public void setFollowId(Integer followId) {
        this.followId = followId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Relation{" +
                "relationId=" + relationId +
                ", userId=" + userId +
                ", followId=" + followId +
                ", state=" + state +
                '}';
    }
}

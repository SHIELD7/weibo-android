package site.imcu.weibo.entity;

import org.litepal.crud.LitePalSupport;

import java.util.Date;

public class Collect extends LitePalSupport {
    private Integer id;

    private Integer weiboId;

    private Integer userId;

    private String weiboContent;

    private String weiboUserName;

    private String weiboUserFace;

    private Date collectTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWeiboUserName() {
        return weiboUserName;
    }

    public void setWeiboUserName(String weiboUserName) {
        this.weiboUserName = weiboUserName;
    }

    public String getWeiboUserFace() {
        return weiboUserFace;
    }

    public void setWeiboUserFace(String weiboUserFace) {
        this.weiboUserFace = weiboUserFace;
    }

    public String getWeiboContent() {
        return weiboContent;
    }

    public void setWeiboContent(String weiboContent) {
        this.weiboContent = weiboContent;
    }

    public Integer getWeiboId() {
        return weiboId;
    }

    public void setWeiboId(Integer weiboId) {
        this.weiboId = weiboId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(Date collectTime) {
        this.collectTime = collectTime;
    }

    @Override
    public String toString() {
        return "Collect{" +
                "weiboId=" + weiboId +
                ", userId=" + userId +
                ", weiboContent='" + weiboContent + '\'' +
                ", weiboUserName='" + weiboUserName + '\'' +
                ", weiboUserFace='" + weiboUserFace + '\'' +
                ", collectTime=" + collectTime +
                '}';
    }
}
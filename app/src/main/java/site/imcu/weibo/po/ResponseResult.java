package site.imcu.weibo.po;

public class ResponseResult<T> {

    //0表示正常
    public static final int SUCCESS_CODE = 0;

    public static final int ERROR_CODE = 1;

    /**
     * 成功
     */
    public static final ResponseResult SUCCESS = new ResponseResult(SUCCESS_CODE, "SUCCESS");
    /**
     * 失败
     */
    public static final ResponseResult ERROR = new ResponseResult(ERROR_CODE, "ERROR");

    //返回状态码
    private int code;

    //提示信息
    private String msg;

    //token
    private String token;

    //数据类型
    private T data;

    public ResponseResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResponseResult() {

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", token='" + token + '\'' +
                ", data=" + data +
                '}';
    }
}


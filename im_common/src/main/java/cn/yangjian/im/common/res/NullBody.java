package cn.yangjian.im.common.res;

//空对象,用在泛型中,表示没有额外的请求参数或者返回参数
public class NullBody {

    public NullBody(){}

    public static NullBody create(){
        return new NullBody();
    }
}

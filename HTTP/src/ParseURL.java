import java.util.*;


public class ParseURL {
    public static void main(String[] args) {
        int index;
        String ur1="https://www.baidu.com/s?ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&tn=baidu&wd=c%2B%2B&rsv_pq=a4fc69f200076536&rsv_t=2b96NbhzR2Ir%2BdW1z0tx0yYZJG0xZgadx7dwH0Qkvn6IH793SOANRIZNaj0&rqlang=cn&rsv_enter=1&rsv_dl=tb&rsv_sug3=3&rsv_sug1=3&rsv_sug7=100&rsv_sug2=0&prefixsug=c%252B%252B&rsp=2&inputT=1160&rsv_sug4=1161\n";
        index=ur1.indexOf("://");
        String schema=ur1.substring(0,index);
        ur1=ur1.substring(index+3);
        System.out.println(schema);


        index=ur1.indexOf("/");
        String s4=ur1.substring(0,index);
        ur1=ur1.substring(index+1);
        System.out.println(s4);

        index=ur1.indexOf("?");
        String s2=ur1.substring(0,index);
        ur1=ur1.substring(index+1);
        System.out.println(s2);


        index=ur1.indexOf("#");
        String s3=ur1.substring(0,index);
        ur1=ur1.substring(index+1);
        System.out.println(s3);
    }

}


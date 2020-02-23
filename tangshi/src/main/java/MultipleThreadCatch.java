import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MultipleThreadCatch {
    private static AtomicInteger sucessCount=new AtomicInteger(0);
    private static AtomicInteger failureCount=new AtomicInteger(0);
    private static class Job implements Runnable {
        //private WebClient client;
        private String url;
        // private MessageDigest messageDigest;
        private DataSource dataSource;
        // private CountDownLatch countDownLatch;


        public Job (String url,DataSource dataSource){
            this.url=url;
            //this.messageDigest=messageDigest;
            this.dataSource=dataSource;
            //this.countDownLatch=countDownLatch;
        }

        @Override
        public void run() {



            WebClient client=new WebClient(BrowserVersion.CHROME);
            client.getOptions().setCssEnabled(false);
            client.getOptions().setJavaScriptEnabled(false);

            try{
                MessageDigest messageDigest=MessageDigest.getInstance("SHA-256");

                HtmlPage page=client.getPage(url);
                String xpath;
                DomText domText;
                xpath= "//div[@class='cont']/h1/text()";
                domText=(DomText)page.getBody().getByXPath(xpath).get(0);
                String title=domText.asText();


                xpath="//div[@class='cont']/p[@class='source']/a[1]/text()";
                domText=(DomText)page.getBody().getByXPath(xpath).get(0);
                String dynasty=domText.asText();

                xpath="//div[@class='cont']/p[@class='source']/a[2]/text()";
                domText=(DomText)page.getBody().getByXPath(xpath).get(0);
                String author=domText.asText();

                xpath = "//div[@class='cont']/div[@class='contson']";
                HtmlElement element=(HtmlElement) page.getBody().getByXPath(xpath).get(0);
                String content=element.getTextContent().trim();


                //1，计算sha256
                String s=title+content;
                messageDigest.update(s.getBytes("UTF-8"));
                byte[] result=messageDigest.digest();
                StringBuilder sha256=new StringBuilder();
                for (byte b:result){
                    sha256.append(String.format("%02x",b));

                }
                //2，计算分词
                List<Term> termList=new ArrayList<>();
                termList.addAll(NlpAnalysis.parse(title).getTerms());
                termList.addAll(NlpAnalysis.parse(content).getTerms());
                List<String> words=new ArrayList<>();
                for (Term term:termList){
                    if(term.getNatureStr().equals("w")){
                        continue;
                    }
                    if(term.getNatureStr().equals("null")){
                        continue;
                    }
                    if(term.getRealName().length()<2){
                        continue;
                    }
                    words.add(term.getRealName());

                }
                String insertWords=String.join(",",words);

                try (Connection connection=dataSource.getConnection()) {
                    String sql = "INSERT INTO tangshi " +
                            "(sha256, dynasty, title, author, " +
                            "content, words) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement statement = (PreparedStatement) connection.prepareStatement(sql)) {
                        statement.setString(1, sha256.toString());
                        statement.setString(2, dynasty);
                        statement.setString(3, title);
                        statement.setString(4, author);
                        statement.setString(5, content);
                        statement.setString(6, insertWords);

                        com.mysql.jdbc.PreparedStatement mysqlStatement = (com.mysql.jdbc.PreparedStatement) statement;
                        System.out.println(mysqlStatement.asSql());
                        statement.executeUpdate();

                        sucessCount.getAndIncrement();

                        // System.out.println(title+"插入成功！");
                    }
                } catch (SQLException e) {
                    if (!e.getMessage().contains("Duplicate entry")) {
                        e.printStackTrace();
                        failureCount.getAndIncrement();
                    }else{
                        sucessCount.getAndIncrement();
                    }

                }
            } catch (IOException e) {
//                if (!e.getMessage().contains("Duplicate entry")) {
//                    e.printStackTrace();
//                }
                failureCount.getAndIncrement();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                failureCount.getAndIncrement();
            } finally {
                failureCount.getAndIncrement();
                //  countDownLatch.countDown();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(30);//一批处理30个，最多处理11批就够了
        WebClient client=new WebClient(BrowserVersion.CHROME);
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setCssEnabled(false);

        String baseUrl="https://so.gushiwen.org/";
        String pathUrl="gushi/tangshi.aspx";

        List<String> detailUrlList=new ArrayList<>();
        //列表页的解析
        {
            String url=baseUrl+pathUrl;
            HtmlPage page=client.getPage(url);
            List <HtmlElement> divs=page.getBody().getElementsByAttribute("div","class","typecont");
            for (HtmlElement div:divs){
                List<HtmlElement> as=div.getElementsByTagName("a");
                for (HtmlElement a:as){
                    String detailurl=a.getAttribute("href");
                    detailUrlList.add(baseUrl+detailurl);

                }
            }
        }


        MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setServerName("127.0.0.1");
        dataSource.setPort(3306);
        dataSource.setUser("root");
        dataSource.setPassword("1");
        dataSource.setDatabaseName("tangshi");
        dataSource.setUseSSL(false);
        dataSource.setCharacterEncoding("UTF8");

        // MessageDigest messageDigest=MessageDigest.getInstance("SHA-256");

        System.out.println("一共有"+detailUrlList.size()+"首诗");
        CountDownLatch countDownLatch=new CountDownLatch(detailUrlList.size());

        //详情页的请求和解析（从执行改成了启动线程去执行）
        for (String url:detailUrlList){
            pool.execute(new Job(url,dataSource));
//            Thread thread =new Thread(new Job(url,messageDigest,dataSource));
//            thread.start();
        }
        //等待所有的诗都下载完成,结束线程池
        //countDownLatch.await();
        while (sucessCount.get()+failureCount.get()<detailUrlList.size()){
            System.out.printf("一共 %d首诗,成功 %d ,失败 %d\r",detailUrlList.size(),sucessCount.get(),failureCount.get());
            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println();
        System.out.println("全部下载成功");
        pool.shutdown();
    }
}

package crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author christangga
 */
public class Crawler {

    public static final int TWEETS_COUNT = 10000;
    
    static class OAuth {

        public String name;
        public String consumerKey;
        public String consumerSecret;
        public String accessToken;
        public String accessTokenSecret;

        public OAuth(String name, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
            this.name = name;
            this.consumerKey = consumerKey;
            this.consumerSecret = consumerSecret;
            this.accessToken = accessToken;
            this.accessTokenSecret = accessTokenSecret;
        }
    }

    enum OAuthUser {

        christ, ivana, gilang, teo
    }

    static ConfigurationBuilder cb = new ConfigurationBuilder();
    static TwitterFactory tf = new TwitterFactory(cb.build());
    static Twitter twitter = tf.getInstance();

    public static List<Status> getTimeline(String username, int pageNo) throws TwitterException {
        Paging paging = new Paging(pageNo, 100);
        List<Status> statuses = twitter.getUserTimeline(username, paging);

        return statuses;
    }

    public static List<Status> search(String queryString, long maxId) throws TwitterException {
        Query query = new Query(queryString + " +exclude:retweets");
        query.setCount(100);
        query.setMaxId(maxId);

        QueryResult result = twitter.search(query);
        return result.getTweets();
    }

    public static void setOAuth(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey(consumerKey);
        cb.setOAuthConsumerSecret(consumerSecret);
        cb.setOAuthAccessToken(accessToken);
        cb.setOAuthAccessTokenSecret(accessTokenSecret);

        tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
    }

    public static void setProxy(String username, String password) {
        cb.setHttpProxyHost("cache.itb.ac.id");
        cb.setHttpProxyPort(8080);
        cb.setHttpProxyUser(username);
        cb.setHttpProxyPassword(password);
    }

    public static void setOAuthUser(List<OAuth> oAuthList, String username) {
        for (OAuth oAuth : oAuthList) {
            if (oAuth.name.equals(username)) {
                setOAuth(oAuth.consumerKey, oAuth.consumerSecret, oAuth.accessToken, oAuth.accessTokenSecret);
                break;
            }
        }
    }

    public static List<OAuth> getOAuthList(String file) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader("in/oauth.txt"));
        String line = null;
        List<OAuth> oAuthList = new ArrayList<>();
        
        while ((line = br.readLine()) != null) {
            String[] splittedLine = line.split(",");
            OAuth oAuth = new OAuth(splittedLine[0], splittedLine[1], splittedLine[2], splittedLine[3], splittedLine[4]);
            oAuthList.add(oAuth);
        }

        return oAuthList;
    }

    public static String getDateString(Date date) {
        String dateString = "";

        dateString += String.format("%02d", date.getYear() + 1900) + "-";
        dateString += String.format("%02d", date.getMonth() + 1) + "-";
        dateString += String.format("%02d", date.getDate()) + " ";
        dateString += String.format("%02d", date.getHours()) + ":";
        dateString += String.format("%02d", date.getMinutes()) + ":";
        dateString += String.format("%02d", date.getSeconds());

        return dateString;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter("res/" + "diskon" + ".csv"));
            HashSet<String> tweetSet = new HashSet<>();

            String proxyUsername = "";
            String proxyPassword = "";
            String queryString = "";

            if (args[0].equals("-p")) {
                proxyUsername = args[1];
                proxyPassword = args[2];
                for (int i = 3; i < args.length; ++i) {
                    queryString += args[i] + " ";
                }
            } else {
                for (int i = 0; i < args.length; ++i) {
                    if (args[i].equals("-p")) {
                        proxyUsername = args[i + 1];
                        proxyPassword = args[i + 2];
                        break;
                    } else {
                        queryString += args[i] + " ";
                    }
                }
            }
            queryString = queryString.trim();

            List<OAuth> oAuthList = getOAuthList("in/oauth.txt");
            // setProxy(proxyUsername, proxyPassword);
            setOAuthUser(oAuthList, "teo");

            int userId = 0;
            long maxId = Long.MAX_VALUE;
            int pageNo = 1;

            int limitRemaining = twitter.getRateLimitStatus().get("/search/tweets").getRemaining();
            System.out.println("===== search remaining " + limitRemaining + " =====");
            List<Status> statuses = search(queryString, maxId);
//            List<Status> statuses = getTimeline(queryString, pageNo);

            writer.write("\"content\",\"original_id\",\"from\",\"date_created\",\"sentiment\",\"original_sentiment\"\n");
            while (tweetSet.size() < TWEETS_COUNT && statuses.size() > 0) {
                limitRemaining--;

                for (Status status : statuses) {
                    String tweet = status.getText();
                    if (tweet.startsWith("\"")) {
                        tweet = tweet.substring(1, tweet.length());
                    }
                    if (tweet.endsWith("\"")) {
                        tweet = tweet.substring(0, tweet.length() - 1);
                    }
                    List<String> splittedTweet = new LinkedList<>(Arrays.asList(tweet.split("[\\s\\r\\n]+")));
                    for (int j = splittedTweet.size() - 1; j >= 0; --j) {
                        if (splittedTweet.get(j).contains("http") || splittedTweet.get(j).endsWith("..")) {
                            splittedTweet.remove(j);
                        }
                    }

                    System.out.println(status.getText());
                    if (tweetSet.add(String.join(" ", splittedTweet))) {
                        System.out.println("\"" + String.join(" ", splittedTweet) + "\",\"" + status.getId() + "\",\"" + status.getUser().getScreenName() + "\",\"" + getDateString(status.getCreatedAt()) + "\",\"\",\"\"");
                        writer.write("\"" + String.join(" ", splittedTweet) + "\",\"" + status.getId() + "\",\"" + status.getUser().getScreenName() + "\",\"" + getDateString(status.getCreatedAt()) + "\",\"\",\"\"\n");
                    }

                    maxId = status.getId();
                }

                if (limitRemaining <= 3) {
                    switch (userId) {
                        case 0:
                            setOAuthUser(oAuthList, "christ");
                            userId++;
                            break;
                        case 1:
                            setOAuthUser(oAuthList, "ivana");
                            userId++;
                            break;
                        case 2:
                            setOAuthUser(oAuthList, "gilang");
                            userId++;
                            break;
                        case 3:
                            setOAuthUser(oAuthList, "teo");
                            userId = 0;
                            break;
                    }

                    limitRemaining = twitter.getRateLimitStatus().get("/search/tweets").getRemaining();
                }

                System.out.println("===== search remaining " + limitRemaining + " =====");

                statuses = search(queryString, maxId);
                pageNo++;
//                statuses = getTimeline(queryString, pageNo);
            }
        } catch (IOException | TwitterException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                }
            } catch (IOException ex) {
                Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}

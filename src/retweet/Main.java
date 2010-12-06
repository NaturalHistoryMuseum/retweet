/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package retweet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.List;
import java.util.Iterator;
import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;
/**
 *
 * @author simon
 */
public class Main {

    private static Integer numSearchResultsToParse = 28;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        if(args.length == 0){
            System.out.println("Usage: java -jar reTweet.jar username [token secret-token]");
        }
        // Recent reTweets
        BigInteger[] hundredRecent = new BigInteger[100];
        OAuthSignpostClient oauthClient;
        if(args.length<3){
            String pin = null;
            oauthClient = new OAuthSignpostClient("6JazZzBkKYOyow6c8dNJWQ", "HkNtxnkyCQ01tmDqFrvTdIpmEG9Gi5cFAe69JMHDQ", "oob");
            // Open the authorisation page in the user's browser
            // On Android, you'd direct the user to URI url = client.authorizeUrl();
            // On a desktop, we can do that like this:
            System.out.println("Please visit: " + oauthClient.authorizeUrl());
            // get the pin
            System.out.print("Enter the pin: ");
            //  open up standard input
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            try {
                pin = br.readLine();
            } catch (java.io.IOException ioe) {
                System.out.println("IO error trying to read pin");
                System.exit(1);
            }
            oauthClient.setAuthorizationCode(pin);
            // Store the authorisation token details for future use
            String[] accessToken = oauthClient.getAccessToken();
            System.out.println("Access tokens (Use these next time you call this client so you don't have to authorise the app again):");
            System.out.println(accessToken[0]);
            System.out.println(accessToken[1]);
        } else {
            oauthClient = new OAuthSignpostClient("6JazZzBkKYOyow6c8dNJWQ", "HkNtxnkyCQ01tmDqFrvTdIpmEG9Gi5cFAe69JMHDQ", args[2], args[3]);
        }

	// Make a Twitter object
	Twitter twitter = new Twitter(args[0], oauthClient);

	// Go into a loop, sleeping for five minutes each time.
	List searchResults;
	Iterator searchResultsIterator;
	Status resultStatus;
	while(true){
            System.out.println("Searching for: "+args[1]);
	    searchResults = twitter.search(args[1]);
	    searchResultsIterator = searchResults.iterator();
            Integer positionInResults = 0;
	    while(searchResultsIterator.hasNext() && positionInResults < numSearchResultsToParse){
		resultStatus = (Status)searchResultsIterator.next();
		BigInteger idToRetweet = resultStatus.getId();
                System.out.println("\t"+resultStatus.getText());
                Boolean alreadyTweeted = false;
                Boolean insertedIntoHundred = false;
                // Move elements down before trying to add, this means they'll
                // always get added!
                if(hundredRecent[hundredRecent.length-1]!=null){
                    System.out.println("100 Tweets stored, removing earliest");
                    for(int i = 0; i<hundredRecent.length-1; i++){
                        hundredRecent[i] = hundredRecent[i+1];
                    }
                    hundredRecent[hundredRecent.length-1] = null;
                }
                for(int i=0; i<hundredRecent.length; i++){
                    if(hundredRecent[i] == null){
                        hundredRecent[i] = idToRetweet;
                        try{
                            twitter.follow(resultStatus.getUser());
                            if(twitter.follow(resultStatus.getUser().getScreenName()) != null){
                                System.out.println("Now following: "+resultStatus.getUser().getScreenName());
                            } else {
                                System.out.println("Attempt to follow '"+resultStatus.getUser().getScreenName()+"' failed");
                            }
                            System.out.println("Retweeting: "+resultStatus.getText()+"...");
                            twitter.retweet(resultStatus);
                        }
                        catch(Exception e){
                            ;// Do nothing - usually a twitter error or similar
                            //System.out.println(e.getMessage());
                        }
                        i=hundredRecent.length;
                    }
                    else if(idToRetweet.compareTo(hundredRecent[i]) == 0){
                        i = hundredRecent.length;
                    }
                }
                positionInResults ++;
	    }
            try{
                Thread.currentThread().sleep(300000);
            }
            catch(java.lang.InterruptedException e){
                ;// Do nothing
            }
	}
    }
}
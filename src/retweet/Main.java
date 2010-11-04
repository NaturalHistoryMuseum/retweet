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
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        // Recent reTweets
        BigInteger[] hundredRecent = new BigInteger[100];
        OAuthSignpostClient oauthClient;
        if(args.length<2){
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
            oauthClient = new OAuthSignpostClient("6JazZzBkKYOyow6c8dNJWQ", "HkNtxnkyCQ01tmDqFrvTdIpmEG9Gi5cFAe69JMHDQ", args[0], args[1]);
        }

	// Make a Twitter object
	Twitter twitter = new Twitter("my-name", oauthClient);

	// Go into a loop, sleeping for five minutes each time.
	List searchResults;
	Iterator searchResultsIterator;
	Status resultStatus;
	while(true){
	    searchResults = twitter.search("@vbrant");
	    searchResultsIterator = searchResults.iterator();
	    while(searchResultsIterator.hasNext()){
		resultStatus = (Status)searchResultsIterator.next();
		BigInteger idToRetweet = resultStatus.getId();
                Boolean alreadyTweeted = false;
                Boolean insertedIntoHundred = false;
                for(int i=0; i<hundredRecent.length; i++){
                    if(hundredRecent[i] == null){
                        hundredRecent[i] = idToRetweet;
                        insertedIntoHundred = true;
                        i=hundredRecent.length;
                        alreadyTweeted = false;
                    }
                    else if(idToRetweet.compareTo(hundredRecent[i]) == 0){
                        alreadyTweeted = true;
                        insertedIntoHundred = true;
                        i = hundredRecent.length;
                    }
                }
                if(!insertedIntoHundred){
                    System.out.println("More than 100 recent tweets, dropping oldest tweet");
                    // Move everything down one
                    for(int i = 0; i<99; i++){
                        hundredRecent[i] = hundredRecent[i+1];
                    }
                    hundredRecent[99] = idToRetweet;
                }
                if(!alreadyTweeted){
                    try{
                        System.out.println("Retweeting: "+resultStatus.getText().substring(0, 50)+"...");
                        twitter.retweet(resultStatus);
                    }
                    catch(Exception e){
                        ;// Do nothing.
                    }
                }
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
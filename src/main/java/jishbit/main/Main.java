package jishbit.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.github.jreddit.entity.Submission;
import com.github.jreddit.retrieval.Submissions;
import com.github.jreddit.retrieval.params.SubmissionSort;
import com.github.jreddit.utils.restclient.HttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;

public class Main {
	
	public static Main bot;
	
	public static IDiscordClient client;
	public static IGuild guild;

	/**
	 * List of submitted memes, do not add directly, use addMeme to retain max size
	 */
	private List<String> submittedMemes = new ArrayList<>();

	private RestClient restClient;

	String userAgent = "JishBit";
	String clientID = "XAdloE1QjMnMyyjZjMtrhqao_1c";
	String redirectURI = "www.google.ca";
	
	public static void main(String[] args) throws Exception {
		bot = new Main();
	}
	
	public Main() {
		restClient = new HttpRestClient();
		restClient.setUserAgent("JishBit");
		connect();
		client.getDispatcher().registerListener(this);
	}

	public void connect() {
		Optional<String> token = Util.getBotToken();
		if(!token.isPresent()){
			System.out.println("Add your token to token.txt");
			System.out.println("Shutting down...");
			System.exit(0);
			return;
		}
		ClientBuilder cB = new ClientBuilder();
		cB.withToken(token.get());
		cB.setMaxReconnectAttempts(50);
		try {
			client = cB.login();
		} catch(DiscordException e) {
			e.printStackTrace();
		}
	}
	
	@EventSubscriber
	public void onReadyEvent(ReadyEvent e) {
		System.out.println("Connected.");
		client.changeStatus(Status.game("Memes"));
	}
	
	@EventSubscriber
    	public void onDisconnectEvent(DiscordDisconnectedEvent event) {
        	System.out.println("BOT DISCONNECTED");
		System.out.println("Reason: " + event.getReason());
    	}
	
	@EventSubscriber
	public void onMessageEvent(MessageReceivedEvent event) {
		IMessage msg = event.getMessage();
		String text = msg.getContent();

		if(text.startsWith("`")) {
			String cmd = text.substring(1).split(" ")[0].toLowerCase();
			
			if(cmd.equalsIgnoreCase("status")) {
				deleteMessage(msg);
				if(msg.getAuthor().getID().equals("73463573900173312")) {
					try {
						String status = msg.getContent().split(" ", 2)[1];
						client.changeStatus(Status.game(status));
						sendMessage(msg.getChannel(), "Status changed to: " + status);
					} catch(Exception e) {
						e.printStackTrace();
					}		
				}
			}
			
			if(cmd.equalsIgnoreCase("meme")) {

				Submission submission = findMeme(1);
				//return if meme finding failed
				if(submission == null){
					sendMessage(event.getMessage().getChannel(), "Failed to find meme.");
					return;
				}

				deleteMessage(msg);
				String linkolio = submission.getUrl();
				if(linkolio.contains("imgur.com") && !linkolio.matches(".+\\.[A-Za-z]{1,5}$") && !linkolio.contains("/a/") && !linkolio.contains("/gallery/")) {
					linkolio += ".jpg";
				}
				sendMessage(msg.getChannel(), "*From /r/" + submission.getSubreddit() + ":* " + submission.getTitle() + "\n" + linkolio.replaceAll("&amp;", "&"));
			}
			
			if(cmd.equalsIgnoreCase("list")) {
				sendMessage(msg.getChannel(), "**Commands** \n `` `status <message>`` - Changes the status of the bot \n `` `meme`` - Posts a dank meme");
			}
		}
		
		if(text.equalsIgnoreCase("<@222446374271057920>")) {
			sendMessage(msg.getChannel(), "JishBit, the ultimate meme bot! \n Created by *Impervious* \n For a list of commands use `` `list``");
		}
	}

	/**
	 * Recursive function to find a random meme
	 */
	public Submission findMeme(int functionAttempt){
		Random rand = new Random();
		int n = rand.nextInt(5) + 1;
		String subCode = Integer.toString(n);
		Subs sub = Subs.getSubReddit(subCode);
		String subToUse = sub.subreddit;

		Submissions subms = new Submissions(restClient);

		List<Submission> submissionsSubreddit = subms.ofSubreddit(subToUse, SubmissionSort.TOP, -1, 100, null, null, true);

		Submission submissionToUse = null;
		int attempts = 0;
		while(submissionToUse == null || submittedMemes.contains(submissionToUse.getIdentifier())) {
			int index = rand.nextInt(submissionsSubreddit.size());
			submissionToUse = submissionsSubreddit.get(index);

			//if after 50 attempts no unused meme is found in this subreddit, retry with new submission request and subreddit
			attempts++;
			if(attempts >= 50){
				functionAttempt++;
				//if after 10 recursive calls no meme is found, accept failure and return null
				if(functionAttempt > 10) {
					System.out.println("FAILED TO FIND MEME. MEMERGENCY.");
					return null;
				}else{
					return findMeme(functionAttempt);
				}
			}
		}
		System.out.println("Found meme after " + functionAttempt + " subreddit searches and " + attempts + " posts.");
		addMeme(submissionToUse.getUrl());
		return submissionToUse;
	}

	public void addMeme(String Url){
		//remove first element (earliest added meme) when list becomes too big
		if(submittedMemes.size() >= 100) submittedMemes.remove(0);
		submittedMemes.add(Url);
		System.out.println(Url);
	}

	public static void sendMessage(IChannel channel, String message){
		try {
			channel.sendMessage(message);
		} catch(Exception e){}
	}
	
	public static void deleteMessage(IMessage message) {
		try {
			message.delete();
		} catch(Exception e) {}
	}
}

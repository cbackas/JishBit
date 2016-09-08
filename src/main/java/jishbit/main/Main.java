package jishbit.main;

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
	
	String userAgent = "JishBit";
	String clientID = "XAdloE1QjMnMyyjZjMtrhqao_1c";
	String redirectURI = "www.google.ca";
	
	public static void main(String[] args) throws Exception {
		bot = new Main();
	}
	
	public Main() {
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
		clientBuilder.setMaxReconnectAttempts(50);
		try {
			client = cB.login();
		} catch(DiscordException e) {
			e.printStackTrace();
		}
	}
	
	@EventSubscriber
	public void onReadyEvent(ReadyEvent e) {
		System.out.println("Connected.");
	}
	
	@EventSubscriber
	public void onDisconnectEvent(DiscordDisconnectedEvent event) {
		System.out.println("Reconnecting in 10 seconds...");
		try {
			Thread.sleep(1000 * 10);
			System.out.println("Attempting to reconnect...");
			connect();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@EventSubscriber
	public void onMessageEvent(MessageReceivedEvent event) {
		IMessage msg = event.getMessage();
		String text = msg.getContent();
		
		RestClient restClient = new HttpRestClient();
		restClient.setUserAgent("JishBit 1.0");
		
		Submissions subms = new Submissions(restClient);
		List<Submission> submissionsSubreddit = subms.ofSubreddit("blackpeopletwitter", SubmissionSort.TOP, -1, 100, null, null, true);
		
		int index = new Random().nextInt(submissionsSubreddit.size());
		Submission submission = submissionsSubreddit.get(index);
		
		if(text.startsWith("`")) {
			String cmd = text.substring(1).split(" ")[0].toLowerCase();
			
			if(cmd.equalsIgnoreCase("status")) {
				deleteMessage(msg);
				if(msg.getAuthor().getID().equals("73463573900173312")) {
					try {
						String status = msg.getContent().split(" ", 2)[1];
						client.changeStatus(Status.game(status));
					} catch (Exception e) {
						e.printStackTrace();
					}		
				}
			}
			
			if(cmd.equalsIgnoreCase("meme")) {
				deleteMessage(msg);
				String linkolio = submission.getUrl();
				if(linkolio.contains("imgur.com") && !linkolio.matches(".+\\.[A-Za-z]{1,5}$") && !linkolio.contains("/a/") && !linkolio.contains("/gallery/")) {
					linkolio += ".jpg";
				}
				sendMessage(msg.getChannel(), submission.getTitle() + " " + linkolio.replaceAll("&amp;", "&"));
			}
		}
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

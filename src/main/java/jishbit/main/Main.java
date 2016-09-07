package jishbit.main;

import java.net.URISyntaxException;
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
		try {
			Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch(URISyntaxException e) {
			e.printStackTrace();
		}
		bot = new Main();
	}
	
	public Main() {
		connect();
		client.getDispatcher().registerListener(this);
	}

	public void connect() {
		Optional<String> token = Util.getBotToken();
		ClientBuilder cB = new ClientBuilder();
		cB.withToken(token.get());
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
		
		if(text.startsWith("`")) {
			String cmd = text.substring(1).split(" ")[0].toLowerCase();
			
			if(cmd.equalsIgnoreCase("status")) {
				if(msg.getAuthor().getID().equals("73463573900173312")) {
					try {
						String status = msg.getContent().split(" ", 2)[1];
						client.changeStatus(Status.game(status));
					} catch (Exception e) {
						e.printStackTrace();
					}		
				}
			}
		}
		
		RestClient restClient = new HttpRestClient();
		restClient.setUserAgent("JishBit 1.0");
		
		Submissions subms = new Submissions(restClient);
		List<Submission> submissionsSubreddit = subms.ofSubreddit("memes", SubmissionSort.TOP, -1, 8000, null, null, true);
		
		int index = new Random().nextInt(submissionsSubreddit.size());
		Submission submission = submissionsSubreddit.get(index);
		
		if(text.equalsIgnoreCase("meme")) {
			String linkolio = submission.getUrl();
			if(linkolio.contains("imgur.com") && !submission.getUrl().matches(".+\\.[A-Za-z]{1,5}$")) {
				linkolio += ".jpg";
				sendMessage(msg.getChannel(), submission.getTitle() + " " + submission.getUrl().replaceAll("&amp;", "&"));
			} else {
				sendMessage(msg.getChannel(), submission.getTitle() + " " + submission.getUrl().replaceAll("&amp;", "&"));
			}
			
		}
	}

	public static void sendMessage(IChannel channel, String message){
		try {
			channel.sendMessage(message);
		} catch(Exception e){}
	}
	
}
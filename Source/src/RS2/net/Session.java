package RS2.net;

import org.jboss.netty.channel.Channel;

import RS2.model.player.Client;

public class Session {
	
	private final Channel channel;
	private Client client;
	
	public Session(Channel channel) {
		this.channel = channel;
	}

	public Channel getChannel() {
		return channel;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

}

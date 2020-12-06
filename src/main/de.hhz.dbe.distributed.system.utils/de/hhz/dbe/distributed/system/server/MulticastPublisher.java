package de.hhz.dbe.distributed.system.server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Enumeration;

public class MulticastPublisher {
	private static final String MULTICAST_INTERFACE = "eth0";
	private static final int MULTICAST_PORT = 4321;
	private static final String MULTICAST_IP = "230.0.0.0";

	public void sendMessage(String ip, String iface, int port, String message) throws IOException {
		DatagramChannel datagramChannel = DatagramChannel.open();
		datagramChannel.bind(null);
		NetworkInterface networkInterface = NetworkInterface.getByName(iface);
		datagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
		ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes());
		InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, port);
		datagramChannel.send(byteBuffer, inetSocketAddress);
	}

	public static void main(String[] args) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
//		MulticastPublisher mp = new MulticastPublisher();
//		mp.sendMessage(MULTICAST_IP, MULTICAST_INTERFACE, MULTICAST_PORT, "Hi there!");
	
		 String interfaceName = "eth0";
		    NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
		    Enumeration<InetAddress> inetAddress = networkInterface.getInetAddresses();
		    InetAddress currentAddress;
		 
		    currentAddress = inetAddress.nextElement();
		    System.out.println(currentAddress);
		    while(inetAddress.hasMoreElements())
		    {
		        currentAddress = inetAddress.nextElement();
		        if(currentAddress instanceof Inet4Address && !currentAddress.isLoopbackAddress())
		        {
		           System.out.println(currentAddress.toString());
		            break;
		        }
		    }
	}
}
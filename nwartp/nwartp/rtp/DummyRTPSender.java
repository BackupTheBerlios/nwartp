package nwartp.rtp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DummyRTPSender implements RTPSender
{
  private DatagramSocket dSock_;
  private String hostname_ = "localhost";
  private int port_ = 1234;

  public DummyRTPSender(String hostname, int port)
  {
    hostname_ = hostname;
    port_ = port;
    try 
    {
      dSock_ = new DatagramSocket();
    }
    catch (Exception e)
    {
      System.out.println(e);
    }
    
  }

  public void sendPacket(byte[] packet)
  {
    try 
    {
      DatagramPacket datagram = new DatagramPacket(packet, packet.length, 
                                                   InetAddress.getByName(hostname_), port_);
      dSock_.send(datagram); 
    }
    catch (Exception e)
    {
      System.out.println(e);
    }
    
  }
}

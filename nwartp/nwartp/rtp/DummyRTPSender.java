package nwartp.rtp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DummyRTPSender implements RTPSender
{
  private DatagramSocket dSock_;

  public DummyRTPSender()
  {
    try 
    {
      dSock_ = new DatagramSocket();
    }
    catch (Exception e)
    {
      
    }
    
  }

  public void sendPacket(byte[] packet)
  {
    try 
    {
      DatagramPacket datagram = new DatagramPacket(packet, packet.length, 
                                                   InetAddress.getByName("localhost"), 6666);
      dSock_.send(datagram); 
    }
    catch (Exception e)
    {
      System.out.println(e);
    }
    
  }
}

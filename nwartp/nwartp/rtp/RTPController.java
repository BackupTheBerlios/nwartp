package nwartp.rtp;

import nwartp.util.Logger;

public class RTPController implements Runnable
{
  private Logger logger_ = new Logger("RTPController");

  RTPPacketGenerator packetGenerator_;
  RTPSender sender_;
  boolean stop_ = false;
  long startTime_;

  RTPController(RTPPacketGenerator pg, RTPSender sender)
  {
    packetGenerator_ = pg;
    sender_ = sender;
  }


  //****************************** Runnable Interface ******************************

  //simple synchronization:sends RTP packets in "realtime" (at least not faster...)
  public void run()
  {
    try 
    {    
      RTPPacket packet = packetGenerator_.getNextPacket();
      while (!stop_)
      {
        long time =  System.currentTimeMillis();
        long playingTime = time - startTime_;

        if (playingTime >= packet.getTime())
        {
          sender_.sendPacket(packet.getByteArray());
          packet = packetGenerator_.getNextPacket();
        }
        Thread.currentThread().sleep(10);
      }
    }
    catch (Exception e)
    {
      logger_.log(e.toString(), Logger.LEVEL_DEBUG);
      stop_ = true;
    }
  }


  //****************************** Methods ******************************

  public void start()
  {
    stop_ = false;
    startTime_ = System.currentTimeMillis();
    new Thread(this).start();
  }

  public void stop()
  {
    stop_ = true;
  }

  public static void main(String[] args) throws Exception
  {
    java.io.InputStream is = new nwartp.media.MultipleFileInputStream
      ("/home/manni/rep/nwartp/data/00000", 3, ".jpg", 1);
    nwartp.media.Cutter c = new nwartp.media.JPEGCutter(25);
    c.attachToStream(is);
    RTPPacketGenerator g = new RTPPacketGenerator(c);
    RTPController controller = new RTPController(g, new DummyRTPSender());

    controller.start();
    //Thread.currentThread().sleep(1000);
    //controller.stop();
  }
}

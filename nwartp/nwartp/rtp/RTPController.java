package nwartp.rtp;

import java.util.Properties;
import java.io.FileInputStream;

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


  private static void printUsage()
  {
    System.out.println("USAGE: java nwartp.rtp.RTPController properties target_ip port");
  }

  public static void main(String[] args) throws Exception
  {

    if (args.length < 3)
    {
      printUsage();
      return;
    }

    Properties props = new Properties();
    
    try
    {
      FileInputStream instream = new FileInputStream( args[0] );
      props.load(instream);
    }
    catch( java.io.FileNotFoundException ex )
    {
      System.out.println(" Property file " + args[0] + " not found!");
      printUsage();
      return;
    }
    
    java.io.InputStream is = new nwartp.media.MultipleFileInputStream
                             (
                               props.getProperty("stream_source_name"), 
                               Integer.parseInt(props.getProperty("stream_source_digits")), 
                               props.getProperty("stream_source_extension"), 
                               Integer.parseInt(props.getProperty("stream_source_offset"))
                               );
    
    nwartp.media.Cutter c = new nwartp.media.JPEGCutter( Integer.parseInt(props.getProperty("stream_fps")) );
    c.attachToStream(is);
    RTPPacketGenerator g = new RTPPacketGenerator(c);
    RTPController controller = new RTPController(g, new DummyRTPSender(args[1],Integer.parseInt(args[2])));

    controller.start();
    //Thread.currentThread().sleep(1000);
    //controller.stop();
  }
}

package nwartp.rtp;

import nwartp.media.Cutter;
import nwartp.media.CutterException;
import nwartp.media.RTPPayload;


public class RTPPacketGenerator
{
  private Cutter cutter_;

  //cutter has to be attached to an inputstream!
  public RTPPacketGenerator(Cutter cutter)
  {
    cutter_ = cutter;
  }

  public byte[] getNextPacket() throws RTPPacketException
  {
    RTPPayload payload;
    try 
    {
      payload = cutter_.getNextPayload();
    }
    catch (Exception e)
    {
      throw new RTPPacketException();
    }
    
    return null;
  }
}

package nwartp.rtp;

//Object not initialized in constructor to reduce
//usage of new; so setXY() methods...
public class RTPPacket
{
  private byte[] packet_;
  private long time_;      //time of packet in stream [ms] 

  public byte[] getByteArray()
  {
    return packet_;
  }

  public long getTime()
  {
    return time_;
  }

  //****************************** Methods for Package RTP ******************************

  void setPacket(byte[] a)
  {
    packet_ = a;
  }

  void setTime(long time)
  {
    time_ = time;
  }
}

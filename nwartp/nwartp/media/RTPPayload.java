package nwartp.media;

//Object not initialized in constructor to reduce
//usage of new; so setXY() methods...

public class RTPPayload
{
  private boolean marker_;
  private byte[] payload_;
  private int timestamp_;               //RTP timestamp, unit depends on application profile 
                                        //(for example RFC 3551 defines some profiles)
  private byte rtpPayloadType_;
  private long time_;                   //time of packet (begin) in stream[ms] 
                                        //used in nwartp server to time sending of packets

  //****************************** Public Methods ******************************

  public byte[] getByteArray()
  {
    return payload_;
  }

  public boolean getMarkerBit()
  {
    return marker_;
  }

  public int getTimestamp()
  {
    return timestamp_;
  }

  public byte getRTPPayloadType()
  {
    return rtpPayloadType_;
  }

  public long getTime()
  {
    return time_;
  }

  //****************************** Methods for Package Media ******************************
  //(for cutter implementations)

  void setPayload(byte[] payload)
  {
    payload_ = payload;
  }

  void setMarkerBit(boolean b)
  {
    marker_ = b;
  }

  void setTimestamp(int t)
  {
    timestamp_ = t;
  }

  void setRTPPayloadType(byte pt)
  {
    rtpPayloadType_ = pt;
  }

  void setTime(long time)
  {
    time_ = time;
  }
}

package nwartp.media;

public class RTPPayload
{
  private boolean marker_;
  private byte[] payload_;
  private int timestamp_;

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
}

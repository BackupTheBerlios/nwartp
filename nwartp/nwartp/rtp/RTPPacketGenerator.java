package nwartp.rtp;

import nwartp.media.Cutter;
import nwartp.media.CutterException;
import nwartp.media.EndOfStreamException;
import nwartp.media.RTPPayload;

import java.io.InputStream;
import java.io.IOException;

public class RTPPacketGenerator
{
  private Cutter cutter_;

  private byte[] header_ = new byte[8];
  private int sequenceNumber_ = 0;
  private RTPPacket packet_ = new RTPPacket();

  //cutter has to be attached to an input stream!
  public RTPPacketGenerator(Cutter cutter)
  {
    cutter_ = cutter;
  }

  public RTPPacket getNextPacket() throws RTPPacketException, EndOfStreamException
  {
    RTPPayload payload;
    try 
    {
      payload = cutter_.getNextPayload();
    }
    catch (EndOfStreamException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new RTPPacketException();
    }
    
    byte[] payloadByteArray = payload.getByteArray();
    byte[] packet = new byte[payloadByteArray.length + 8];

    setUpHeader(payload);

    /*    for (int i = 0; i < 8; i++)
    {
      packet[i] = header_[i];
    }
    for (int i = 8; i < packet.length; i++)
    {
      packet[i] = payloadByteArray[i - 8];
      }*/

    //generate the RTP packet byte array
    System.arraycopy(header_, 0, packet, 0, header_.length);
    System.arraycopy(payloadByteArray, 0, packet, header_.length, packet.length - header_.length);
    sequenceNumber_++;

    packet_.setTime(payload.getTime());
    packet_.setPacket(packet);

    return packet_;
  }

  private void setUpHeader(RTPPayload payload)
  {
    header_[0] = -128;
    byte markerBit = 0;
    if (payload.getMarkerBit())
    {
      markerBit = -128;
    }
    header_[1] = (byte)(markerBit | payload.getRTPPayloadType());
    header_[2] = (byte)((sequenceNumber_ >>> 8) & 0xff);
    header_[3] = (byte)(sequenceNumber_ & 0xff);

    header_[4] = (byte)((payload.getTimestamp() >>> 24) & 0xff);
    header_[5] = (byte)((payload.getTimestamp() >>> 16) & 0xff);
    header_[6] = (byte)((payload.getTimestamp() >>> 8) & 0xff);
    header_[7] = (byte)(payload.getTimestamp() & 0xff);
  }

  public static void main(String[] args) throws Exception
  {
    InputStream is = new nwartp.media.MultipleFileInputStream
      ("/home/manni/rep/nwartp/data/00000", 3, ".jpg", 1);
    Cutter c = new nwartp.media.JPEGCutter(25);
    c.attachToStream(is);
    RTPPacketGenerator g = new RTPPacketGenerator(c);

    for (int i = 0; i < 10; i++)
    {
      g.getNextPacket();    
    }
  }
}

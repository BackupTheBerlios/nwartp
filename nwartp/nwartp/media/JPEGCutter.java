package nwartp.media;

import nwartp.util.Logger;

import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;


//This Cutter computes RTP Payloads for JPEG Video.
//Implements Cutter interface
//RFC2435

public class JPEGCutter  extends JPEGParserObserver implements Cutter
{
  private Logger logger_ = new Logger("JPEGCutter", null,  Logger.LEVEL_DEBUG);

  private InputStream inputStream_;

  private MainJPEGHeader mainJPEGHeader_ = new MainJPEGHeader();
  private QuantizationTableHeader quantizationTableHeader_ = new QuantizationTableHeader();

  private JPEGParser parser_;

  private ArrayList payloadBuffer_;
  private RTPPayload payload_ = new RTPPayload();

  private boolean ready_ = false;

  private ArrayList qTableData_ = new ArrayList();

  private class MainJPEGHeader
  {
    void setTypeSpecific(int b)
    {
      header_[0] = (byte)b;
    }

    void setFragmentOffset(int offset)
    {
      int bitmask = 255;
      header_[1] = (byte)((offset >>> 16) & bitmask);
      header_[2] = (byte)((offset >>> 8) & bitmask);
      header_[3] = (byte)(offset & bitmask);
    }

    void setType(int b)
    {
      header_[4] = (byte)b;
    }

    void setQ(int b)
    {
      header_[5] = (byte)b;
    }

    void setWidth(int b)
    {
      header_[6] = (byte)(b >>> 3);
    }


    void setHeight(int b)
    {
      header_[7] = (byte)(b >>> 3);
    }

    byte[] getByteArray()
    {
      return header_;
    }

    private byte[] header_ = new byte[8];
  }

  private class QuantizationTableHeader
  {
    void setMBZ(int b)
    {
      header_[0] = (byte)b;
    }

    void setPresition(int b)
    {
      header_[1] = (byte)b;
    }

    void setLength(int length)
    {
      int bitmask = 255;
      header_[2] = (byte)((length >>> 8) & bitmask);
      header_[3] = (byte)(length & bitmask);
    }

    byte[] getByteArray()
    {
      return header_;
    }

    private byte[] header_ = new byte[4];
  }

  //****************************** End nested classes ******************************


  //****************************** Cutter Interface ******************************

  public void attachToStream(InputStream is)
  {
    inputStream_ = is;
    parser_ = new JPEGParser(is, this);
    payloadBuffer_ = new ArrayList();
  }

  public void detachFromStream()
  {
    inputStream_ = null;
  }

  public RTPPayload getNextPayload() throws CutterException, EndOfStreamException
  {
    if (parser_ == null)
    {
      return null;
    }
    
    initGetNextPayload();
   
    while ( !ready_)
    {
      try 
      {
        parser_.nextEvent();         
      }
      catch (EndOfStreamException  e)
      {
        logger_.log("End of stream", Logger.LEVEL_DEBUG);
        throw e;
      }
      
      catch (Exception e)
      {
        logger_.log("JPEG stream error", Logger.LEVEL_DEBUG);
        throw new CutterException();
      }
    }

    //OK, payload is in payloadBuffer, so prepare RTPPayload object.
    byte[] buffer = new byte[payloadBuffer_.size()];
    logger_.log("Payload size: " + payloadBuffer_.size());

    Iterator it = payloadBuffer_.iterator();
    int i = 0;
    while (it.hasNext()) 
    {
      buffer[i] = ((Byte)it.next()).byteValue();
      i++;
    }
    payload_.setMarkerBit(true);
    payload_.setTimestamp(0);
    payload_.setPayload(buffer);
    return payload_;
  }


  //****************************** JPEGParserObserver ******************************

  public void handleImageSize(int width, int height) 
  {
    logger_.log("handleImageSize()");
    mainJPEGHeader_.setWidth(width);
    mainJPEGHeader_.setHeight(height);
    writeMainHeader();
  };

  public void handleDQT(InputStream stream, int length, int qtNumber, int precision) 
    throws IOException
  {
    logger_.log("handleDQT()");

    byte[] data = new byte[length];

    for (int i = 0; i < length; i++)
    {
      int b = stream.read();
      if (b == -1)
      {
        logger_.log("There should be no end of stream!", Logger.LEVEL_ERROR);
        break;
      }
      data[i] = (byte)b;
    }
    qTableData_.add(data);
  }

  public void handleImageData(InputStream stream) throws IOException
  {
    logger_.log("handleImageData()");
    
    //QT has to be ready here...
    writeQT();

    //ugly reading bytes until end of image...
    int b1 = stream.read();
    while (true)
    {
      if (b1 == JPEGParser.MARKER_SEPERATOR)
      {
        int b2 = stream.read();
        switch (b2)
        {
        case JPEGParser.MARKER_EOI:
          ready_ = true;
          return;

        case JPEGParser.MARKER_SOS:
          logger_.log("second SOS marker found -> JPEG not supported by RTP", 
                      Logger.LEVEL_ERROR);
          throw new IOException();          

        default:
          writeByte(b1);
          b1 = b2;
        }
      }
      else
      {
        writeByte(b1);
        b1 = stream.read();
      }
    }
  }


  //****************************** Methods ******************************

  public JPEGCutter()
  {
    hardcodeHeaders();
  }

  private void hardcodeHeaders()
  {
    mainJPEGHeader_.setTypeSpecific(0);//progressively scanned
    mainJPEGHeader_.setFragmentOffset(0);
    mainJPEGHeader_.setType(0);//0 or 1:no restart markers
    mainJPEGHeader_.setQ(128);//dynamic QTables

    quantizationTableHeader_.setMBZ(0);//meaning of this one is not documented...
    quantizationTableHeader_.setPresition(0);//8 bit tables only
  }

  private void initGetNextPayload()
  {
    ready_ = false;
    payloadBuffer_.clear();
    qTableData_.clear();
  }

  private void writeQT()
  {
    writeQTHeader();
    writeQTData();
  }

  private void writeQTHeader()
  {
    //calculate length of data and set it in QTHeader
    int length = 0;
    Iterator tables = qTableData_.iterator();
    while (tables.hasNext())
    {
      byte[] data = (byte[])tables.next();
      length += data.length;
    }
    logger_.log("QTableDataLength: " + length);
    quantizationTableHeader_.setLength(length);

    writeArray(quantizationTableHeader_.getByteArray());
  }

  private void writeQTData()
  {
    Iterator tables = qTableData_.iterator();
    while (tables.hasNext())
    {
      byte[] data = (byte[])tables.next();
      writeArray(data);
    }
  }

  private void writeMainHeader()
  {
    writeArray(mainJPEGHeader_.getByteArray());
  }


  //this 2 functions handle writing to buffer, buffer implemenation may be
  //changed...
  private void writeArray(byte[] a)
  {
    for (int i = 0; i < a.length; i++)
    {
      payloadBuffer_.add(new Byte(a[i]));
    }
  }

  private void writeByte(int b)
  {
    payloadBuffer_.add(new Byte((byte)b));
  }

  public static void main(String[] args) throws Exception
  {
    //test java byte conversion
    int test = 254;
    System.out.println((byte)test);

    InputStream is = new nwartp.media.MultipleFileInputStream
      ("/home/manni/rep/nwartp/data/00000", 3, ".jpg", 1);
    JPEGCutter c = new JPEGCutter();
    c.attachToStream(is);

    for (int i = 0; i < 30; i++)
    {
      c.getNextPayload();    
    }
  }
}

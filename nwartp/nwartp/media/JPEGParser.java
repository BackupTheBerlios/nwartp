package nwartp.media;

import nwartp.util.Logger;

import java.io.InputStream;
import java.io.IOException;

//This class reads from a bytestream and 
//recognises parts and attributes of a JPEG 
//bytestream that are needed to construct a 
//RTP payload.
//JPEG information: FCD 14495, JFIF spezification from www.jpeg.org
//RFC 2435 shows the parts that are needed for RTP...

//Implementation notes: State Pattern
//The state machine is event driven, like a SAX Parser


//FIXME there may be more than one 0xff between segments
class JPEGParser
{
  private final static Logger logger_ = new Logger("JPEGParser", null, Logger.LEVEL_DEBUG);

  final static int MARKER_SEPERATOR = 0xff;
  final static int MARKER_SOI = 0xd8;
  final static int MARKER_APP0 = 0xe0;
  final static int MARKER_SOF0 = 0xc0;
  final static int MARKER_SOS = 0xda;
  final static int MARKER_LSE = 0xf2;
  final static int MARKER_DQT = 0xdb;
  final static int MARKER_DRI = 0xdd;
  final static int MARKER_DHT = 0xc4;
  final static int MARKER_EOI = 0xd9;

  final static int MARKER_THUMBNAIL = 0x10;
  final static int MARKER_THUMBNAIL_1Byte = 0x11;
  final static int MARKER_THUMBNAIL_3Byte = 0x13;

  private final int[] STRING_JFIF = {0x4a, 0x46, 0x49, 0x46, 0x00};
  private final int[] STRING_JFXX = {0x4a, 0x46, 0x58, 0x58, 0x00};


  private InputStream inputStream_;
  private JPEGParserObserver observer_;

  //possible states, JPEG terminology, so short, non dinostyle names :-)
  private final SOI soi_ = new SOI();
  private final APP0 app0_ = new APP0();
  private final APP0Extension app0Extension_ = new APP0Extension();
  private final DQT dqt_ = new DQT();
  private final SOF0 sof0_ = new SOF0();
  private final DHT dht_ = new DHT();
  private final SOS sos_ = new SOS();
  private final ImageDataState imageDataState_ = new ImageDataState();

  private StreamState state_;


  public class EndOfStreamException extends Exception{}
  public class JPEGStreamException extends Exception{}

  class StreamState
  {
    private final Logger logger_ = new Logger("StreamState", JPEGParser.logger_);

    void nextEvent() throws IOException, EndOfStreamException, JPEGStreamException {}

    int read() throws IOException, EndOfStreamException
    {
      int b = inputStream_.read();
      if (b == -1)
      {
        throw new EndOfStreamException();
      }
      return b;
    }

    int read2ByteInt() throws IOException, EndOfStreamException
    {
      int b1 = read();
      int b2 = read();
      return (b1 << 8) + b2;
    }

    int read3ByteInt() throws IOException, EndOfStreamException
    {
      int b1 = read();
      int b2 = read();
      int b3 = read();
      return (b1 << 16) + (b2 << 8)  + b2;
    }

    boolean compareToStream(int[] ba) throws IOException, EndOfStreamException
    {
      for (int i = 0; i < ba.length; i++)
      {
        if (read() != ba[i])
        {
          return false;
        }
      }
      return true;      
    }
  }

  //eats bytes until start-of-image marker is found
  class SOI extends StreamState
  {
    private final Logger logger_ = new Logger("SOI", JPEGParser.logger_);
    void nextEvent() throws IOException, EndOfStreamException, JPEGStreamException
    {
      int b1 = read();
      while (true)
      {
        if ( b1 == MARKER_SEPERATOR)
        {
          int b2 = read();
          if (b2 == MARKER_SOI)
          {
            //FIXME handle event: new Frame found
            logger_.log("SOI found");            
            setNextState();
            return;
          }
          else
          {
            b1 = b2;
          }
        }
        else
        {
          b1 = read();
        }
      }
    }

    void setNextState() throws IOException, EndOfStreamException, JPEGStreamException
    {
      logger_.log("find next state");
      if (read() != MARKER_SEPERATOR)
      {
        throw new JPEGStreamException();
      }
      int b = read();
      switch (b)
      {
      case  MARKER_APP0:
        state_ = app0_;
        logger_.log("APP0 state found");
        break;
      default:
        logger_.log("Unsupported marker found.", Logger.LEVEL_DEBUG);
        throw new JPEGStreamException();
      }
    }
  }

  class APP0 extends StreamState
  {
    private final Logger logger_ = new Logger("APP0", JPEGParser.logger_);
    void nextEvent() throws IOException, EndOfStreamException, JPEGStreamException
    {
      int length =  read2ByteInt();
      logger_.log("block length: " + length);

      if (!compareToStream(STRING_JFIF))
      {
        throw new JPEGStreamException();
      }

      //version
      int version = read2ByteInt();
      logger_.log("JFIF Version " + " " + version);
      //units
      int units = read();
      int xDensity = read2ByteInt();
      int yDensity = read2ByteInt();
      int xThumbnail = read();
      int yThumbnail = read();

      logger_.log("units " + units);
      logger_.log("Xdensity " + xDensity);
      logger_.log("Ydensity " + yDensity);
      logger_.log("XThumb " + xThumbnail);
      logger_.log("YThumb " + yThumbnail);


      int rgbValuesNumber = xThumbnail + yThumbnail;
      logger_.log("rgbn " + rgbValuesNumber);

      int[] rgbValues = new int[rgbValuesNumber];
      for (int i = 0; i < rgbValuesNumber; i++)
      {
        rgbValues[i] = read3ByteInt();
      }
      
      //FIXME Event

      setNextState();
    }

    void setNextState() throws IOException, EndOfStreamException, JPEGStreamException
    {
      logger_.log("find next state");
      if (read() != MARKER_SEPERATOR)
      {
        throw new JPEGStreamException();
      }
      int b = read();
      switch (b)
      {
      case  MARKER_APP0:
        state_ = app0Extension_;
        logger_.log("APP0 Extension found");
        break;

      case  MARKER_DQT:
        state_ = dqt_;
        logger_.log("DQT found");
        break;

      case  MARKER_SOF0:
        state_ = sof0_;
        logger_.log("SOF0 found");
        break;

      default:
        logger_.log("Unsupported marker: " + b, Logger.LEVEL_ERROR);
        throw new JPEGStreamException();
      }
    }

  }

  //JFIF extension: APP0 marker segment
  class APP0Extension extends StreamState
  {
    private final Logger logger_ = new Logger("APP0Extension", JPEGParser.logger_);
    void nextEvent() throws IOException, EndOfStreamException, JPEGStreamException
    {
      int length =  read2ByteInt();

      if (!compareToStream(STRING_JFXX))
      {
        throw new JPEGStreamException();
      }

      int extensionCode = read();

      switch (extensionCode)
      {
      case  MARKER_THUMBNAIL:
        logger_.log("Thumbnail coded using JPEG", Logger.LEVEL_DEBUG);
        break;

      case  MARKER_THUMBNAIL_1Byte:
        logger_.log("JPEGParser:Thumbnail 1 byte/pixel", Logger.LEVEL_DEBUG);
        break;

      case  MARKER_THUMBNAIL_3Byte:
        logger_.log("JPEGParser:Thumbnail 3 byte/pixel", Logger.LEVEL_DEBUG);
        break;
      }

      //thumbnailsare not supported by rtp, so skip them
      for (int i = 0; i < length - 8; i++)
      {
        read();
      }
      setNextState();
    }
    void setNextState() throws IOException, EndOfStreamException, JPEGStreamException
    {
      logger_.log("find next state");
      app0_.setNextState();
      state_.nextEvent();
    }
  }

  //define quantisition table
  class DQT extends StreamState
  {
    private final Logger logger_ = new Logger("DQT", JPEGParser.logger_);
    void nextEvent() throws IOException, EndOfStreamException, JPEGStreamException
    {
      int length =  read2ByteInt();
      logger_.log("block length: " + length);

      parseQtTable(length, 3);
      setNextState();
      
    }

    void parseQtTable(int dqtLength, int currentLength) throws 
      IOException, EndOfStreamException, JPEGStreamException
    {
      int qtInfo = read();

      int qtNumber = qtInfo & 0xf;
      logger_.log("QT number: " + qtNumber);
      int precision = (qtInfo & (~0xf)) >>> 4;
      logger_.log("precision: " + precision);
      int length = 64 * (precision + 1);

      //generate event before possible recursion
      observer_.handleDQT(inputStream_, length, qtNumber, precision);

      //a single DQT segment may contain more QTs...
      if (currentLength + length < dqtLength)
      {
        logger_.log("in this DQT block is another table... " + length);
        parseQtTable(dqtLength, currentLength + length);
      }
      
    }

    void setNextState() throws IOException, EndOfStreamException, JPEGStreamException
    {
      logger_.log("find next state");
      app0_.setNextState();
    }
  }

  //start of frame (baseline)
  class SOF0 extends StreamState
  {
    private final Logger logger_ = new Logger("SOF0", JPEGParser.logger_);
    void nextEvent() throws IOException, EndOfStreamException, JPEGStreamException
    {
      int length =  read2ByteInt();
      logger_.log("block length: " + length);

      int dataPresition = read();
      int imageHeight = read2ByteInt();
      int imageWidth = read2ByteInt();
      int components = read();
      logger_.log("image size: " + imageWidth + " " + imageHeight);
      logger_.log("components: " + components);
      observer_.handleImageSize(imageWidth, imageHeight);

      for (int i = 0; i < components; i++)
      {
        read();
        read();
        read();
      }
      setNextState();
    }

    void setNextState() throws IOException, EndOfStreamException, JPEGStreamException
    {
      logger_.log("find next state");
      if (read() != MARKER_SEPERATOR)
      {
        throw new JPEGStreamException();
      }
      int b = read();
      switch (b)
      {
      case  MARKER_DHT:
        state_ = dht_;
        logger_.log("DHT found");
        break;

      default:
        logger_.log("Unsupported marker: " + b, Logger.LEVEL_ERROR);
        throw new JPEGStreamException();
      }
    }
  }

  //Define Huffman Table
  class DHT extends StreamState
  {
    private final Logger logger_ = new Logger("DHT", JPEGParser.logger_);

    void nextEvent() throws IOException, EndOfStreamException, JPEGStreamException
    {
      int length =  read2ByteInt();
      logger_.log("block length: " + length);

      parseHTable(length, 2);

      setNextState();
    }

    void parseHTable(int dhtLength, int currentLength) throws 
      IOException, EndOfStreamException, JPEGStreamException
    {
      int htInfo = read();

      int numberOfHT = htInfo & 0xf00;
      int length = 0;
      for (int i = 0; i < 16; i++)
      {
        int codes = read();
        length += codes;
      }
      logger_.log("symbols: " + length);  

      //now are length symbol bytes in the stream
      //generate event before possible recursion
      
      observer_.handleDHT(inputStream_, length);
    

      //a single DHT segment may contain more HTs...
      currentLength += length + 17;

      if (currentLength < dhtLength)
      {
        logger_.log("in this DHT block is another table... " + length);
        parseHTable(dhtLength, currentLength);
      }
    }

    void setNextState() throws IOException, EndOfStreamException, JPEGStreamException
    {
      logger_.log("find next state");
      if (read() != MARKER_SEPERATOR)
      {
        throw new JPEGStreamException();
      }
      int b = read();
      switch (b)
      {
      case  MARKER_DHT:
        logger_.log("DHT found");
        break;

      case  MARKER_SOS:
        state_ = sos_;
        logger_.log("SOS found");
        break;

      default:
        logger_.log("Unsupported marker: " + b, Logger.LEVEL_ERROR);
        throw new JPEGStreamException();
      }

    }
  }

  //Start Of Scan
  class SOS extends StreamState
  {
    private final Logger logger_ = new Logger("SOS", JPEGParser.logger_);
    void nextEvent() throws IOException, EndOfStreamException, JPEGStreamException
    {
      int length =  read2ByteInt();
      logger_.log("block length: " + length);

      int numberOfComponents = read();
      for ( int i = 0; i < numberOfComponents; i++)
      {
        read();
        read();
      }

      read(); //near
      read(); //interleave
      read(); //point transform

      setNextState();
    }

    void setNextState() throws IOException, EndOfStreamException, JPEGStreamException
    {
      logger_.log("find next state");

      /*      int[] lookahead = new int[0];

      int b1 = read();
      if (b1 == MARKER_SEPERATOR)
      {
        int b2 = read();
        switch (b2)
        {
        case  MARKER_SOS:
          state_ = soi_;
          logger_.log("second SOS => this JPEG is not supported by RTP",
                      logger_.LEVEL_ERROR);
          throw new JPEGStreamException();
        }

        lookahead = new int[2];
        lookahead[0] = b1;
        lookahead[1] = b2;
      }
      imageDataState_.setLookahead(lookahead);*/
      state_ = imageDataState_;
      logger_.log("begin image data");
    }
  }

  class ImageDataState extends StreamState
  {
    private final Logger logger_ = new Logger("ImageDataState", JPEGParser.logger_);
    //private int[] lookahead_ = new int[0];

    /*void setLookahead(int[] lookahead)
    {
      lookahead_ = lookahead;
      }*/

    void nextEvent() throws IOException, EndOfStreamException, JPEGStreamException
    {
      observer_.handleImageData(inputStream_);
      setNextState();
    }

    void setNextState() throws IOException, EndOfStreamException, JPEGStreamException
    {
      logger_.log("find next state");
      state_ = soi_;
      logger_.log("SOI set");
    }
  }

  //****************************** End nested classes ******************************


  JPEGParser(InputStream is, JPEGParserObserver observer)
  {
    inputStream_ = is;
    observer_ = observer;

    state_ = soi_;
  }


  public void nextEvent() throws IOException, EndOfStreamException, JPEGStreamException
  {
    state_.nextEvent();
  }




  public static void main(String[] args) throws Exception
  {
    JPEGParser p = new JPEGParser(new java.io.FileInputStream
                                  ("/home/manni/rep/nwartp/data/00000001.jpg"),
                                  new JPEGParserObserver());

    for(int i = 0; i < 12; i++)
    {
      p.nextEvent();
    }
    

  }
 
}

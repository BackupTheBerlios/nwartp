package nwartp.media;

import nwartp.util.Logger;

import java.io.InputStream;
import java.io.IOException;


//no interface because some default implementations are useful
//if a JPEGParser client does not use some events; bytes may 
//have to be read from InutStream, and I don't have to implement
//an Observer implementation for testing ;-)


public class JPEGParserObserver
{
  private Logger logger_ = new Logger("JPEGParserObserver", null);

  public void handleImageSize(int width, int heigth) {};

  public void handleDQT(InputStream stream, int length, int qtNumber, int precision) 
    throws IOException
  {
    logger_.log("handleDQT()");
    for (int i = 0; i < length; i++)
    {
      int b = stream.read();
      if (b == -1)
      {
        logger_.log("There should be no end of stream!", Logger.LEVEL_ERROR);
      }
    }
  }

  public void handleImageData(InputStream stream) throws IOException
  {
    logger_.log("handleImageData()");

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
          logger_.log("EOI found");
          return;

        case JPEGParser.MARKER_SOS:
          logger_.log("second SOS marker found -> JPEG not supported by RTP", 
                      Logger.LEVEL_ERROR);
          throw new IOException();          

        default:
          b1 = b2;
        }
      }
      else
      {
        b1 = stream.read();
      }
    }
  }

  public void handleDHT(InputStream stream, int length)  throws IOException
  {
    logger_.log("handleDHT()");
    for (int i = 0; i < length; i++)
    {
      int b = stream.read();
      if (b == -1)
      {
        logger_.log("There should be no end of stream!", Logger.LEVEL_ERROR);
      }
    }
  }
}

package nwartp.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.lang.String;


public class MultipleFileInputStream extends InputStream
{

  private FileInputStream inputFile_;
  private int fileNumber_;
  private int fileNumberDigits_;
  private String fileName_;
  private String fileExtension_;

  public MultipleFileInputStream(String name, int digits, String extension, 
                                 int offset) throws FileNotFoundException
  {
    fileName_ = new String(name);
    fileNumberDigits_ = digits;
    fileExtension_ = extension;
    fileNumber_ = offset;

    inputFile_ = new FileInputStream(name + getFileNumberSring(offset, digits) + extension);
  }


  public int available() throws IOException
  {
    return inputFile_.available();
  }

  public void close() throws IOException
  {
    inputFile_.close();
  }

  public int read() throws IOException
  {
    int retVal = inputFile_.read();
    if (retVal == -1)
    {
      inputFile_.close();
      try 
      {
        nextInputFile();
        return inputFile_.read();
      }
      catch (FileNotFoundException e)
      {
        return -1;
      }      
    }
    return retVal;
  }

  public int read(byte[] b) throws IOException
  {
    return read(b, 0, b.length);
  }

  public int read(byte[] b, int off, int len) throws IOException
  {
    int bytesRead = inputFile_.read(b, off, len);
    if (bytesRead == -1)
    {
      inputFile_.close();
      try 
      {
        nextInputFile();
        return inputFile_.read(b, off, len);
      }
      catch (FileNotFoundException e)
      {
        return -1;
      }      
    }

    if (bytesRead < b.length)
    {
      inputFile_.close();
      try 
      {
        nextInputFile();
        int nextBytesRead = inputFile_.read(b, bytesRead, b.length - bytesRead);
        if (nextBytesRead == -1)
        {
          nextBytesRead = 0;
        }
        return bytesRead + nextBytesRead;
      }
      catch (FileNotFoundException e)
      {
        return bytesRead;
      }      
    }
    return bytesRead;    
  }


  public long skip(long n) throws IOException
  {
    return inputFile_.skip(n);
  }

  private String getFileNumberSring(int number, int digits)
  {
    String numberString = new String();
    for ( int i = 1; i <=digits; i++)
    {
      int rest = number % 10;
      numberString = rest + numberString;
      number /= 10;
    }
    return numberString;
  }

  private void nextInputFile() throws FileNotFoundException
  {
    fileNumber_++;
    inputFile_ = new FileInputStream(fileName_ + 
                                     getFileNumberSring(fileNumber_, fileNumberDigits_) +
                                     fileExtension_);
  }

  private MultipleFileInputStream()
  {
  }

  public static void main(String[] args)
  {
    MultipleFileInputStream mfs = new MultipleFileInputStream();
    System.out.println(mfs.getFileNumberSring(7,4));
    System.out.println(mfs.getFileNumberSring(77,4));
    System.out.println(mfs.getFileNumberSring(7777,4));    
    System.out.println(mfs.getFileNumberSring(997,4));
    System.out.println(mfs.getFileNumberSring(9,1));
 
  }
  

}

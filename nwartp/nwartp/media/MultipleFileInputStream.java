package nwartp.media;

import nwartp.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.lang.String;


//This class simulates/generates a continuous stream from multiple 
//inputfiles.

//File naming convention:  xyz
//x:prefix
//y:number with n digits
//z:suffix

//x and z are constant, y is inremented after each EOS (end of stream), but
//has a fixed number of digits.

//Example: You collect documents with the ugliest table of contents known
//         to (at least)mankind, so you have an archive with thousands of .doc files.
//         They are named bla000000.doc to bla934234.doc.
//         To make them even more fearsome, this is how to create
//         an continuous InputStream of them: 
//         new MultipleFileInputStream("bla", 6, ".doc", 0);


//There are 2 possible ways for triggering end of stream:
//1. Next file with the generated/incremented name not found, so EOS
//   reached. (Exception: if first file is not there, FileNotFoundException...)
//2. maximal file number, representable with the number of digits is reached
//   So, for example: if you have 1000 files and num of digits is 1, EOS
//   after reading file 9.

public class MultipleFileInputStream extends InputStream
{
  private Logger logger_ = new Logger("MultipleFileInputStream", null);

  private FileInputStream inputFile_;
  private int fileNumber_;
  private int fileNumberDigits_;
  private int maxFileNumber_;
  private String fileName_;
  private String fileExtension_;

  public MultipleFileInputStream(String name, int digits, String extension, 
                                 int offset) throws FileNotFoundException
  {
    fileName_ = new String(name);
    fileNumberDigits_ = digits;
    fileExtension_ = extension;
    fileNumber_ = offset;

    maxFileNumber_ = 1;
    for (int i = 0; i < digits; i++)
    {
      maxFileNumber_ *= 10;
    }
    maxFileNumber_ -= 1;
    logger_.log("Max number of files: " + maxFileNumber_, Logger.LEVEL_DEBUG);

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
        logger_.log("File not found", Logger.LEVEL_DEBUG);
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
    if (fileNumber_ > maxFileNumber_)
    {
      logger_.log("Max file number reached", Logger.LEVEL_DEBUG);
      throw new FileNotFoundException();
    }
    
    inputFile_ = new FileInputStream(fileName_ + 
                                     getFileNumberSring(fileNumber_, fileNumberDigits_) +
                                     fileExtension_);
  }

  //for testing only, see main
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

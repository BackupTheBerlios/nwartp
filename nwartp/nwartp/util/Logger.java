package nwartp.util;

import java.io.InputStream;
import java.io.IOException;

//this is a primitive logger

//to reduce amount of typing each Logger object holds a prefix String, 
//the downside of this is, the log() method is not static, so you
//have to create instances of Logger...

public class Logger
{
  public static final int LEVEL_LOOP = 0;//for frequent, repetetive output, may seriously
                                         //hit performance of running program
  public static final int LEVEL_DEBUG = 1;//general infos for developers, SHOULD NOT 
                                          //be performance critical
  public static final int LEVEL_ERROR = 2;//things that should not be...

  private final static int MIN_LOGGING_PRIORITY = LEVEL_LOOP;
  private final static int LEVEL_DEFAULT = LEVEL_LOOP;

  private Logger logger_;
  private String prefix_;


  public Logger(String prefix)
  {
    init(prefix, null);
  }

  //prefix: this string (+ ":") will prefix all output
  //logger: prefix of this logger will prefix the prefix :-)
  //        so, you can create a list of Loggers/prefixes;
  //        may be seen as a very primitive Decorator...
  public Logger(String prefix, Logger logger)
  {
    init(prefix, logger);
  }

  public void log(String s)
  {
    log(s, LEVEL_DEFAULT);
  }

  public void log(String s, int priority)
  {
    if (priority >= MIN_LOGGING_PRIORITY)
    {

      System.out.println(getPrefix() + ":" + s + " " + getLevelString(priority));
    }
    
  }

  private String getLevelString(int priority)
  {
    switch (priority)
    {
      case  LEVEL_LOOP:
        return "[LOOP]";

      case  LEVEL_DEBUG:
        return "[DEBUG]";
    }
    return "[*** ERROR ***]";
  }

  private void init(String prefix, Logger logger)
  {
    prefix_ = prefix;
    logger_ = logger;
  }

  private String getPrefix()
  {
    if (logger_ == null)
    {
      return prefix_;
    }
    return logger_.getPrefix() + ":" + prefix_;
  }
}

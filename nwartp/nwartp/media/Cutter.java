package nwartp.media;

import java.io.InputStream;

public interface Cutter 
{
  public RTPPayload getNextPayload() throws CutterException;
  public void attachToStream(InputStream is);
  public void detachFromStream();
}

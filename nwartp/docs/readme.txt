readme.txt

Gruppe 01 - Streaming Server
1.) Entpacken des Archives
./ziele.txt
./projektplan.txt
./readme.txt
./src/properties
./src/Makefile
./src/nwartp/ -> Klassenhierachie
./src/docs/ -> zusätzliche Doku
./src/data/ -> Streaming Data

2.) Übersetzen der Source-codes
Im Verzeichnis ./src/ befindet sich ein Makefile. In dieses
Verzeichnis wechseln und make ausführen. Der Java-Compiler
(getestet mit blackdown-1.4.1) übersetzt den Code.

3.) Ausführen des Servers
Der Server wird mittels

java nwartp.rtp.RTPController ./properties localhost 1234

gestartet und beginnt sofort mit dem Senden von RTP-Packeten
an das port 1234 des hosts "localhost".

------------------------------------------------------------

Wie können die Streams wiedergegeben werden?

Das war/ist das grösste Problem unserer Implementierung.
Da die Wiedergabe von MJPEG-Streams weder mit vlc noch mit
mplayer möglich sind, sind wir auf den player ausgewichen,
welcher mit dem Java Media Framework (JMF) mitgeliefert
wird (http://java.sun.com/products/java-media/jmf/2.1.1/download.html).

Nach der Installation zu starten mittels: jmstudio 

//TODO Einstellungen in jmstudio

------------------------------------------------------------

Welche Dateien können gestreamed werden?
Mit dieser Version ist es möglich, Motion JPEG Videos zu
streamen. Die einzelnene Frames liegen als JPEG-Bilder im
Verzeichnis ./data/ vor. Die Dateinamen unterscheiden sich
lediglich um die letzten Stellen vor der Endung (.jpg).

Bsp:
00000001.jpg
00000002.jpg
...
00000768.jpg

Die Quellen können im properties-file festgelegt werden:
stream_source_name = Pfad + prefix der Frames
stream_source_digits = Anzahl der änderbaren Stellen
stream_source_extension = postfix der Frames
stream_source_offset = Index des ersten Frames
stream_fps = Anzahl der Frames pro Sekunde

------------------------------------------------------------

Wie können kompatible Streams erstellt werden?
Die einzelnen Frames können mit Hilfe des mplayer erstellt
werden:

mplayer -vo jpeg -jpeg quality=50:smooth=0:baseline:noprogressive:optimize=0 filename

Die parameter "quality" und "smooth" können den Bedürfnissen
angepasst werden.

------------------------------------------------------------

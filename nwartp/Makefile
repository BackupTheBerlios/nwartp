
CLASSPATH = ./

all:
	-javac -classpath $(CLASSPATH) nwartp/util/*.java
	-javac -classpath $(CLASSPATH) nwartp/media/*.java
	-javac -classpath $(CLASSPATH) nwartp/rtp/*.java



clean:
	-find * -name "*.class" -exec rm '{}' ';'

distclean:

	-find * -name "*~" -exec rm '{}' ';'
	-find * -name "*.class" -exec rm '{}' ';'
	-find * -name "#*#" -exec rm '{}' ';'

tags:
	exuberant-ctags -e `find * -name "*.java"`

#!/bin/sh
javadoc \
    -public \
    -overview src/main/overview.html \
    -sourcepath src/main/java \
    -classpath lib/mail-1.4.2.jar \
    -d build/docs/javadoc \
    -windowtitle "Pantomime 0.992 API Documentation" \
    -doctitle "Pantomime 0.992 API Documentation" \
    -top "<div id=pantomimeTop>Read the <a href=\"http://barbee.bitbucket.org/pantomime/manual/pantomime_manual.html\">Pantomime manual</a>.</div>" \
    -bottom "Copyright &copy; 2013. JH Barbee" \
    -link http://download.oracle.com/javase/6/docs/api/ \
    -link http://download.oracle.com/javaee/6/api/ \
    -subpackages org \
    -stylesheetfile etc/javadocs.css 


#rm -r build/docs/javadoc
#mkdir -p build/docs/javadoc
#mv src/main/java/org/blackmist/pantomime/Multipart.aj \
#    src/main/java/org/blackmist/pantomime/Multipart.java
#mv src/main/java/org/blackmist/pantomime/SinglePart.aj \
#    src/main/java/org/blackmist/pantomime/SinglePart.java
#ajdoc -public -d build/docs/javadoc -source 1.6 \
#    -classpath build/classes/main/:lib/aspectjrt.jar:lib/mail-1.4.2.jar \
#    -windowtitle "Pantomime 0.90 API Documentation" \
#    -link http://download.oracle.com/javase/6/docs/api/ \
#    -link http://download.oracle.com/javaee/6/api/ \
#    src/main/java/org/blackmist/pantomime/*.j* src/main/java/org/blackmist/pantomime/content/*.java
#
#mv src/main/java/org/blackmist/pantomime/Multipart.java \
#src/main/java/org/blackmist/pantomime/Multipart.aj
#mv src/main/java/org/blackmist/pantomime/SinglePart.java \
#src/main/java/org/blackmist/pantomime/SinglePart.aj

open build/docs/javadoc/index.html


#/usr/bin/sh

javac --module-path ./lib --add-modules=javafx.controls EmpireWorldbuilder.java

java --module-path ./lib --add-modules=javafx.controls EmpireWorldbuilder

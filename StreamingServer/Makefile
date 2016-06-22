args:=
extern_libs=$(CLASSPATH):extern/JMF/*:
class_dir=build/classes/

all: clean pre-compile compile

pre-compile:
	@echo preparing build directories...
	@mkdir -p build
	@mkdir -p build/classes

# -Xlint extra warning options (can be specified by -Xlint:<option>)
compile:
	@echo compiling...
	@javac -Xlint -cp  $(extern_libs) -d  $(class_dir) src/streamingserver/*.java

run:
	java -cp build/classes/ streamingserver/StreamingServer $(args)

clean:
	@clear
	@rm -frd build/classes/*
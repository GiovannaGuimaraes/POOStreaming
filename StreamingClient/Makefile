args:=
GLOBIGNORE="*ignore*"

all: clean pre-compile compile

pre-compile:
	@echo preparing build directories...
	@shopt -s extglob # extended glob
	@set GLOBIGNORE="*ignore*"
	@mkdir -p build
	@mkdir -p build/classes
	@mkdir -p build/classes/tmp

# -Xlint extra warning options (can be specified by -Xlint:<option>)
compile:
	@echo compiling...
	@javac -Xlint -d build/classes/ src/streamingclient/*.java

run:
	sudo java -cp build/classes/ streamingclient/StreamingClient

clean:
	@clear
	@rm -frd build/classes/*
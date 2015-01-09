UNAME = $(shell uname)
ifeq ($(UNAME), SunOS) # Sun OS
MY_LIBS = -lresolv -lsocket -lnsl
endif
ifeq ($(UNAME), Linux) # Linux
MY_LIBS = -lresolv -lnsl -lpthread
endif
ifeq ($(UNAME), Darwin) # Mac OS
MY_LIBS =
endif

CC := g++

all:
	${CC} -m32 srao2_server.cpp -o server

##==========================================================================
clean:
	@- $(RM) server	
	@- echo "Data Cleansing Done.Ready to Compile"



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

all: abt gbn sr

abt : abt.o
	${CC} -o abt abt.o 

gbn : gbn.o
	${CC} -o gbn gbn.o 

sr : sr.o
	${CC} -o sr sr.o 

##==========================================================================
clean:
	@- $(RM) abt gbn sr
	@- echo “Data Cleansing Done.Ready to Compile”

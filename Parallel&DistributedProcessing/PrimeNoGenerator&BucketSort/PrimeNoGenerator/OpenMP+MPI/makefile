CC = mpiicc
FLAGS = -std=c99 -openmp

PrimeNumOpenMpMpi:PrimeNumOpenMpMpi.c
	$(CC) $(FLAGS) PrimeNumOpenMpMpi.c -o PrimeNumOpenMpMpi

.phony: clean
clean:
	rm -f PrimeNumOpenMpMpi
CC = nvcc
FLAGS = -arch sm_20

PrimeNumCuda:PrimeNumCuda.cu
	$(CC) $(FLAGS) PrimeNumCuda.cu -o PrimeNumCuda

.phony: clean
clean:
	rm -f PrimeNumCuda

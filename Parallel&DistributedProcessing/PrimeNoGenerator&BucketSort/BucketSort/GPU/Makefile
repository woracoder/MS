CC = nvcc
FLAGS = -arch sm_20

BucketSortCuda:BucketSortCuda.cu
	$(CC) $(FLAGS) BucketSortCuda.cu -o BucketSortCuda

.phony: clean
clean:
	rm -f BucketSortCuda

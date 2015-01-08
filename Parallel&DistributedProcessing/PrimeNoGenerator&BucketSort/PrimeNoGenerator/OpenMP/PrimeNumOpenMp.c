#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <omp.h>

int main(int argc, char* argv[]) {

	if (argc < 3 || argc > 4) {
		fprintf(stderr, "Please provide P: The number of processors, N: The problem size and/or optional flag(1) to print prime numbers.\n");
	}

	struct timeval t;
	double start_t, end_t, time_spent;
	int i, maxPrime=0, numPrimes = 0, procs=atoi(argv[1]), size=atoi(argv[2]);

	FILE *pn;
	if (argc == 4) {
		pn = fopen("PnOpenMpNos.txt", "w");
		if (pn == NULL) {
			fprintf(stderr, "Can't open file to write output\n");
			return EXIT_FAILURE;
		}
		fprintf(pn, "%d\n", 2);
		fprintf(pn, "%d\n", 3);
		fprintf(pn, "%d\n", 5);
		fprintf(pn, "%d\n", 7);
	}

	gettimeofday(&t, NULL);
	start_t = (t.tv_sec * 1000000.0) + t.tv_usec;
	#pragma omp parallel for default(shared) private(i) schedule(dynamic, 10) reduction(max:maxPrime) reduction(+:numPrimes)
	for (i = 9; i < size; i += 2) {
		int sizeRt = (int)ceil(sqrt(i));
		int k=3;
		for (k = 3; k <= sizeRt; k++) {
			if (i % k == 0) {
				break;
			}
		}
		if (k>sizeRt) {
			maxPrime = i;
			numPrimes++;
			if(argc == 4) {
				fprintf(pn, "%d\n", i);
			}
		}
	}
	gettimeofday(&t, NULL);
	end_t = (t.tv_sec * 1000000.0) + t.tv_usec;
	time_spent = end_t - start_t;

	if (argc == 4) {
		fclose(pn);
	}

	FILE *tr = fopen("PnOpenMpTime.txt", "a+");
	if (tr == NULL) {
		fprintf(stderr, "Can't open file to write output\n");
		return EXIT_FAILURE;
	}
	
	fprintf(tr, "Maximum prime number = %d. Number of primes generated = %d. Total time taken for %d numbers using OpenMP on %d cores = %f secs\n", maxPrime, numPrimes+4, size, procs, time_spent/1000000.0);
	fclose(tr);

	return EXIT_SUCCESS;
}

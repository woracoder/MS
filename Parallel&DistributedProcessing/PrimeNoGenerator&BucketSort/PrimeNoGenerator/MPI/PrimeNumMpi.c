#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <math.h>
#include "mpi.h"

int main(int argc, char*argv[]) {

	if (argc < 3 || argc > 4) {
		fprintf(stderr, "Please provide P: The number of processors, N: The problem size and/or optional flag(1) to print prime numbers.\n");
	}

	int rank, size;
	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Status status;

	struct timeval t;
	double start_t, end_t, time_spent;

	FILE *pn;
	if (argc == 4) {
		if(rank == 0) {
			pn = fopen("PnMpiNos.txt", "w");
			if (pn == NULL) {
				fprintf(stderr, "Can't open file to write output\n");
				return EXIT_FAILURE;
			}
			fprintf(pn, "%d\n", 2);
			fprintf(pn, "%d\n", 3);
			fprintf(pn, "%d\n", 5);
			fprintf(pn, "%d\n", 7);
		} else {
			char filename[15];
			sprintf(filename, "PnMpiNos_%d.txt", rank);
			pn = fopen(filename, "w");
		}
	}

	gettimeofday(&t, NULL);
	start_t = (t.tv_sec * 1000000.0) + t.tv_usec;
	int procs = atoi(argv[1]), nos = atoi(argv[2]), i, maxPrime=0, tmpPrime=0, numPrimes=0, tmpNumPrimes=0;
	for(i=9; i<=nos; i+=2) {
		if (rank == ((i-9)/2) % size){
			int noRt = (int) ceil(sqrt(i)), k;
			for(k=3; k<=noRt; k+=2) {
				if (i % k == 0) {
					break;
				}
			}
			if (k>noRt) {
				maxPrime = i;
				numPrimes++;
				if(argc == 4) {
					fprintf(pn, "%d\n", i);
				}
			}
		}
	}

	if(argc == 4) {
		fclose(pn);
	}

	if(rank == 0) {
		long long a=1;
		for(a=1; a<size; a++) {
			MPI_Recv(&tmpPrime, 1, MPI_INT, a, 1, MPI_COMM_WORLD, &status);
			MPI_Recv(&tmpNumPrimes, 1, MPI_INT, a, 1, MPI_COMM_WORLD, &status);
			numPrimes += tmpNumPrimes;
			if(tmpPrime > maxPrime) {
				maxPrime = tmpPrime;
			}
		}
		if(argc == 4) {
			FILE* tmp;
			for(a=1; a<size; a++) {
				tmp = fopen("PnMpiNos.txt", "a+");
				char filename [15];
				sprintf(filename, "PnMpiNos_%lld.txt", a);
				FILE* rtmp = fopen(filename, "r");
				char line [20];
				while(fgets(line, sizeof(line), rtmp)) {
					fprintf(tmp, "%s", line);
				}
				fclose(rtmp);
				remove(filename);
			}
			fclose(tmp);
		}
	} else {
		MPI_Send(&maxPrime, 1, MPI_INT, 0, 1, MPI_COMM_WORLD);
		MPI_Send(&numPrimes, 1, MPI_INT, 0, 1, MPI_COMM_WORLD);
	}

	gettimeofday(&t, NULL);
	end_t = (t.tv_sec * 1000000.0) + t.tv_usec;

	if(rank == 0) {
		FILE *tr = fopen("PnMpiTime.txt", "a+");
		if (tr == NULL) {
			fprintf(stderr, "Can't open file to write output\n");
			return EXIT_FAILURE;
		}
		time_spent = end_t - start_t;
		fprintf(tr, "Maximum prime number = %d. Number of primes generated = %d. Total time taken for %d numbers using MPI on %d nodes = %f secs\n", maxPrime, numPrimes+4, nos, procs, time_spent/1000000.0);
		fclose(tr);
	}
	MPI_Finalize();
	return EXIT_SUCCESS;
}

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>
#include "mpi.h"
#include "omp.h"

int main(int argc, char*argv[]) {

	if (argc < 4 || argc > 5) {
		fprintf(stderr, "Please provide P: The number of processors, C: The number of cores, N: The problem size and/or optional flag(1) to print all prime numbers to a file.\n");
	}

	int rank, size;
	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Status status;

	time_t start_t, end_t;
	double time_spent;

	FILE *pn;
	if (argc == 5) {
		if(rank == 0) {
			pn = fopen("PnOpenMpMpiNos.txt", "w");
			if (pn == NULL) {
				fprintf(stderr, "Can't open file to write output\n");
				return EXIT_FAILURE;
			}
			fprintf(pn, "%d\n", 2);
			fprintf(pn, "%d\n", 3);
			fprintf(pn, "%d\n", 5);
			fprintf(pn, "%d\n", 7);
		} else {
			char filename[22];
			sprintf(filename, "PnOpenMpMpiNos_%d.txt", rank);
			pn = fopen(filename, "w");
		}
	}

	time(&start_t);
	long long procs = atol(argv[1]), cores = atol(argv[2]), nos = atol(argv[3]), i, maxPrime=0, tmpPrime=0;

	#pragma omp parallel for default(shared) private(i) schedule(dynamic, 10) reduction(max:maxPrime)
	for(i=9; i<=nos; i+=2) {
		if (rank == ((i-9)/2) % size){
			long long noRt = (long long) ceil(sqrt(i)), k;
			for(k=3; k<=noRt; k++) {
				if (i % k == 0) {
					break;
				}
			}
			if (k>noRt) {
				maxPrime = i;
				if(argc == 5) {
					fprintf(pn, "%lld\n", i);
				}
			}
		}
	}

	if(argc == 5) {
		fclose(pn);
	}

	if(rank == 0) {
		long long a=1;
		for(a=1; a<size; a++) {
			MPI_Recv(&tmpPrime, 1, MPI_LONG, a, 1, MPI_COMM_WORLD, &status);
			if(tmpPrime > maxPrime) {
				maxPrime = tmpPrime;
			}
		}
		if(argc == 5) {
			FILE* tmp;
			for(a=1; a<size; a++) {
				tmp = fopen("PnOpenMpMpiNos.txt", "a+");
				char filename [22];
				sprintf(filename, "PnOpenMpMpiNos_%lld.txt", a);
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
		MPI_Send(&maxPrime, 1, MPI_LONG, 0, 1, MPI_COMM_WORLD);
	}

	time(&end_t);

	if(rank == 0) {
		FILE *tr = fopen("PnOpenMpMpiTime.txt", "a+");
		if (tr == NULL) {
			fprintf(stderr, "Can't open file to write output\n");
			return EXIT_FAILURE;
		}
		time_spent = difftime(end_t, start_t);
		fprintf(tr, "Max prime number = %lld Total time taken for %lld numbers using OpenMP+MPI on %lld nodes with %lld cores = %f secs\n", maxPrime, nos, procs, cores, time_spent);
		fclose(tr);
	}

	MPI_Finalize();
	return EXIT_SUCCESS;
}

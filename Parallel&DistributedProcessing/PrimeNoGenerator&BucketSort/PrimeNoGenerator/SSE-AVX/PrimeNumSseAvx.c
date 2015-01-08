#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>
#include <stdbool.h>
#include <xmmintrin.h> //1
//#include <emmintrin.h> //2
//#include <pmmintrin.h> //3
//#include <smmintrin.h> //4.1
//#include <nmmintrin.h> //4.2

int main(int argc, char*argv[]) {

	if (argc < 3 || argc > 4) {
		fprintf(stderr,
				"Please provide P: The number of processors, N: The problem size and/or optional flag(1) to print prime numbers.\n");
	}

	time_t start_t, end_t;
	double time_spent;
	int i, maxPrime = 0, procs = atoi(argv[1]), size = atoi(argv[2]);
	bool isPrime = true;

	FILE *pn;
	if (argc == 4) {
		pn = fopen("PnSseAvxNos.txt", "w");
		if (pn == NULL) {
			fprintf(stderr, "Can't open file to write output\n");
			return EXIT_FAILURE;
		}
		fprintf(pn, "%d\n", 2);
		fprintf(pn, "%d\n", 3);
		fprintf(pn, "%d\n", 5);
		fprintf(pn, "%d\n", 7);
	}

	int adFour[4], adTwo[4], nm[4], ns[4], ans[4];
	int d=0;
	for(d=0; d<4; d++) {
		adFour[d] = 4;
		adTwo[d] = 2;
		nm[d] = 7;
		ns[d] = d-1;
	}

	__m128i addTwo = _mm_loadu_si128((__m128i*)adTwo), addFour = _mm_loadu_si128((__m128i*)adFour), num = _mm_loadu_si128((__m128i*)nm), nos = _mm_loadu_si128((__m128i*)ns),
			quot = _mm_setzero_si128(), quotIntoNos = _mm_setzero_si128(), numMinusQuotIntoNos = _mm_setzero_si128();

	time(&start_t);
	for (i = 9; i < size; i += 2) {
		int sizeRt = (int)ceil(sqrt(i));
		num = _mm_add_epi32(num, addTwo);
		int k = 3;
		for (k = 3; k <= sizeRt; k+=4) {
			nos = _mm_add_epi32(nos, addFour);
			quot = _mm_div_epi32(num, nos);
			quotIntoNos = _mm_mul_epi32(nos, quot);
			numMinusQuotIntoNos = _mm_sub_epi32(num, quotIntoNos);
			_mm_storeu_si128((__m128i*)ans, numMinusQuotIntoNos);
			for(d=0; d<4; d++) {
				if (ans[d] == 0) {
					isPrime=false;
					break;
				}
			}
			if(!isPrime) {
				break;
			}
		}
		nos = _mm_loadu_si128((__m128i*)ns);
		quot = _mm_setzero_si128();
		quotIntoNos = _mm_setzero_si128();
		numMinusQuotIntoNos = _mm_setzero_si128();
		if (isPrime) {
			maxPrime = i;
			if (argc == 4) {
				fprintf(pn, "%d\n", i);
			}
		} else {
			isPrime = true;
		}
	}
	time(&end_t);

	if (argc == 4) {
		fclose(pn);
	}

	time_spent = difftime(end_t, start_t);

	FILE *tr = fopen("PnSseAvxTime.txt", "a+");
	if (tr == NULL) {
		fprintf(stderr, "Can't open file to write output\n");
		return EXIT_FAILURE;
	}
	fprintf(tr,
			"Maximum prime number = %d Total time taken for %d numbers by SSE-AVX on %d processor = %f secs\n",
			maxPrime, size, procs, time_spent);
	fclose(tr);

	return EXIT_SUCCESS;
}

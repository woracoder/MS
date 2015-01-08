#include <iostream>
#include <vector>
#include <stdio.h>
#include <algorithm>
#include <math.h>
#include <stdlib.h>
#include <sys/time.h>
#include <float.h>
#include <stdint.h>
#include <omp.h>

using namespace std;

void putNumbersIntoBuckets(vector<float> buckets[], float nos[], int size, int argc);
float calculateMean(float nos[], int size);
double calculateStandardDeviation(float nos[], int size, float mean);
void initializeCdfArray(float cdfValues[]);
void sortAllBuckets(vector<float> buckets[]);
int printSortedNumbers(vector<float> buckets[], double time_spent, int size, int noCores, int argc);

void random_number_generator_normal(float* arr, int size, int max_number);
void r4_nor_setup(uint32_t kn[128], float fn[128], float wn[128]);
float r4_nor(uint32_t *jsr, uint32_t kn[128], float fn[128], float wn[128]);
uint32_t shr3_seeded(uint32_t *jsr);
float r4_uni(uint32_t *jsr);

int main(int argc, char*argv[]) {

	if (argc < 4 || argc > 5) {
		cout << "Kindly enter -t, size of input and number of cores as parameter. Optional 4th parameter (give as 1) will print 3 files of unsorted numbers, the created bucket and sorted numbers in files." << endl;
		return EXIT_FAILURE;
	}

	struct timeval t;
	double start_t, end_t, time_spent;

	int cores = atoi(argv[3]), size = atoi(argv[2]);
	float* nos = (float*) malloc(size * sizeof(float));
	random_number_generator_normal(nos, size, size);

	if(argc == 5) {
		FILE *un = fopen("UnsortedNos.txt", "w");
		for(int i=0; i<size; i++) {
			fprintf(un, "%f\n", nos[i]);
		}
		fclose(un);
	}

	gettimeofday(&t, NULL);
	start_t = (t.tv_sec * 1000000.0) + t.tv_usec;
	vector<float> buckets[20];
	putNumbersIntoBuckets(buckets, nos, size, argc);
	free(nos);
	sortAllBuckets(buckets);
	gettimeofday(&t, NULL);
	end_t = (t.tv_sec * 1000000.0) + t.tv_usec;
	time_spent = end_t - start_t;

	printSortedNumbers(buckets, time_spent, size, cores, argc);
	return EXIT_SUCCESS;
}

void putNumbersIntoBuckets(vector<float> buckets[], float nos[], int size, int argc) {
	float mean = calculateMean(nos, size);
	double stdDev = calculateStandardDeviation(nos, size, mean);
	float cdfValues[20];
	initializeCdfArray(cdfValues);
	float distanceFromStdDev = 0.0;
	for (int i = 0; i < size; i++) {
		distanceFromStdDev = (nos[i] - mean) / stdDev;
		for (int j = 0; j < 20; j++) {
			if (distanceFromStdDev < cdfValues[j]) {
				buckets[j].push_back(nos[i]);
				break;
			}
		}
	}
	if(argc == 5) {
		FILE *ub = fopen("UnsortedBucket.txt", "w");
		for(int i=0; i<20; i++) {
			int bs = 0;
			for(int j=0, k=buckets[i].size(); j<k; j++, bs++) {
				fprintf(ub, "%f\n", buckets[i][j]);
			}
			fprintf(ub, "\nBucket number %d is of size = %d\n\n", (i+1), bs);
			cout << "Bucket number " << (i+1) << " is of size = " << bs << endl;
		}
		fclose(ub);
	}
}

float calculateMean(float nos[], int size) {
	float sum = 0;
	for (int i = 0; i < size; i++) {
		sum += nos[i];
	}
	return (sum / size);
}

double calculateStandardDeviation(float nos[], int size, float mean) {
	double var = 0;
	for (int i = 0; i < size; i++) {
		var += pow((nos[i] - mean), 2.0);
	}
	var /= size;
	return (sqrt(var));
}

//http://www.danielsoper.com/statcalc3/calc.aspx?id=53
void initializeCdfArray(float cdfValues[]) {
	cdfValues[0] = -1.64;
	cdfValues[1] = -1.28;
	cdfValues[2] = -1.04;
	cdfValues[3] = -0.84;
	cdfValues[4] = -0.67;
	cdfValues[5] = -0.52;
	cdfValues[6] = -0.38;
	cdfValues[7] = -0.25;
	cdfValues[8] = -0.12;
	cdfValues[9] = 0;
	cdfValues[10] = 0.12;
	cdfValues[11] = 0.25;
	cdfValues[12] = 0.38;
	cdfValues[13] = 0.52;
	cdfValues[14] = 0.67;
	cdfValues[15] = 0.84;
	cdfValues[16] = 1.04;
	cdfValues[17] = 1.28;
	cdfValues[18] = 1.64;
	cdfValues[19] = FLT_MAX;
}

void sortAllBuckets(vector<float> buckets[]) {
	int i=0;
	#pragma omp parallel for private(i) default(shared) schedule(dynamic, 1)
	for (i=0; i<20; i++) {
		sort(buckets[i].begin(), buckets[i].end());
	}
}

int printSortedNumbers(vector<float> buckets[], double time_spent, int size, int noCores, int argc) {
	FILE *tt = fopen("TimeTakenBucketSortOpenMp.txt", "a+");
	if (tt == NULL) {
		fprintf(stderr, "Can't open file to write output\n");
		return EXIT_FAILURE;
	}
	if(argc == 5) {
		FILE *sn = fopen("SortedNos.txt", "w");
		if (sn == NULL) {
			fprintf(stderr, "Can't open file to write output\n");
			return EXIT_FAILURE;
		}
		for(int i=0; i<20; i++) {
			for(int j=0, k=buckets[i].size(); j<k; j++) {
				fprintf(sn, "%f\n", buckets[i][j]);
			}
		}
		fclose(sn);
	}
	cout << "Total time required on " << noCores << " cores in seconds = " << (time_spent / 1000000.0) << " for sorting " << size << " numbers."<< endl;
	fprintf(tt, "Total time required on %d cores in seconds = %f for sorting %d numbers.\n", noCores, (time_spent / 1000000.0), size);
	fclose(tt);
	return EXIT_SUCCESS;
}

void random_number_generator_normal(float* arr, int size, int max_number) {
	uint32_t kn[128];
	float fn[128], wn[128];
	r4_nor_setup(kn, fn, wn);
	float rnd;
	uint32_t seed = (uint32_t) time(NULL);
	float var = sqrt(max_number);
	for (int i = 0; i < size; i++) {
		rnd = r4_nor(&seed, kn, fn, wn);
		arr[i] = max_number / 2 + rnd * var;
	}
}

void r4_nor_setup(uint32_t kn[128], float fn[128], float wn[128]) {
	double dn = 3.442619855899;
	int i;
	const double m1 = 2147483648.0;
	double q;
	double tn = 3.442619855899;
	const double vn = 9.91256303526217E-03;

	q = vn / exp(-0.5 * dn * dn);

	kn[0] = (uint32_t) ((dn / q) * m1);
	kn[1] = 0;

	wn[0] = (float) (q / m1);
	wn[127] = (float) (dn / m1);

	fn[0] = 1.0;
	fn[127] = (float) (exp(-0.5 * dn * dn));

	for (i = 126; 1 <= i; i--) {
		dn = sqrt(-2.0 * log(vn / dn + exp(-0.5 * dn * dn)));
		kn[i + 1] = (uint32_t) ((dn / tn) * m1);
		tn = dn;
		fn[i] = (float) (exp(-0.5 * dn * dn));
		wn[i] = (float) (dn / m1);
	}

	return;
}

float r4_nor(uint32_t *jsr, uint32_t kn[128], float fn[128], float wn[128]) {
	int hz;
	uint32_t iz;
	const float r = 3.442620;
	float value;
	float x;
	float y;

	hz = (int) shr3_seeded(jsr);
	iz = (hz & 127);

	if (fabs(hz) < kn[iz]) {
		value = (float) (hz) * wn[iz];
	} else {
		for (;;) {
			if (iz == 0) {
				for (;;) {
					x = -0.2904764 * log(r4_uni(jsr));
					y = -log(r4_uni(jsr));
					if (x * x <= y + y) {
						break;
					}
				}

				if (hz <= 0) {
					value = -r - x;
				} else {
					value = +r + x;
				}
				break;
			}

			x = (float) (hz) * wn[iz];

			if (fn[iz] + r4_uni(jsr) * (fn[iz - 1] - fn[iz])
					< exp(-0.5 * x * x)) {
				value = x;
				break;
			}

			hz = (int) shr3_seeded(jsr);
			iz = (hz & 127);

			if (fabs(hz) < kn[iz]) {
				value = (float) (hz) * wn[iz];
				break;
			}
		}
	}

	return value;
}

uint32_t shr3_seeded(uint32_t *jsr) {
	uint32_t value;

	value = *jsr;

	*jsr = (*jsr ^ (*jsr << 13));
	*jsr = (*jsr ^ (*jsr >> 17));
	*jsr = (*jsr ^ (*jsr << 5));

	value = value + *jsr;

	return value;
}

float r4_uni(uint32_t *jsr) {
	uint32_t jsr_input;
	float value;

	jsr_input = *jsr;

	*jsr = (*jsr ^ (*jsr << 13));
	*jsr = (*jsr ^ (*jsr >> 17));
	*jsr = (*jsr ^ (*jsr << 5));

	value = fmod(0.5 + (float) (jsr_input + *jsr) / 65536.0 / 65536.0, 1.0);

	return value;
}

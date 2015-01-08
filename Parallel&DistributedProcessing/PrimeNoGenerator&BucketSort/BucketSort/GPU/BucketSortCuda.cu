#include <iostream>
#include <vector>
#include <stdio.h>
#include <algorithm>
#include <math.h>
#include <stdlib.h>
#include <sys/time.h>
#include <float.h>
#include <stdint.h>
#include <cuda.h>

using namespace std;

__global__ void sortBuckets(float *bucketsAsArrayD, int *eachBucketSizeArrayD) {

	//Copy to shared memory after initializing shared memory
	__shared__ float numsInBucket[4000];
	for(int i=0; i<4; i++) {
		if(i%1000 == threadIdx.x) {
			numsInBucket[i] = 10000000000.0;
		}
	}
	__syncthreads();

	int bucketSize = eachBucketSizeArrayD[blockIdx.x+1] - eachBucketSizeArrayD[blockIdx.x];
	int blockStart = eachBucketSizeArrayD[blockIdx.x];
	for(int i=0, j=0; i<4; i++) {
		if((j=(threadIdx.x + (i*1000))) < bucketSize) {
			numsInBucket[j] = bucketsAsArrayD[blockStart + j];
		}
	}
	__syncthreads();

	//Sort the bucket
	if(threadIdx.x == 0) {
		float key = 0.0;
		for(int j=1, i=0; j<bucketSize; j++) {
			key = numsInBucket[j];
			i = j - 1;
			while(i >= 0 and numsInBucket[i] > key) {
				numsInBucket[i + 1] = numsInBucket[i];
				i--;
			}
			numsInBucket[i + 1] = key;
		}
	}
	__syncthreads();

	//Copy back to global memory
	for(int i=0, j=0; i<4; i++) {
		if( (j=(threadIdx.x + (i*1000))) < bucketSize) {
			bucketsAsArrayD[blockStart + j] = numsInBucket[j];
		}
	}
}

vector< vector<float> > putNumbersIntoBuckets(vector< vector<float> > buckets, float nos[], int size, int argc);
double calculateMean(float nos[], int size);
double calculateStandardDeviation(float nos[], int size, float mean);
double cdf(double x);
int printSortedNumbers(float *nums, double time_spent, int size, int argc);

void random_number_generator_normal(float* arr, int size, int max_number);
void r4_nor_setup(uint32_t kn[128], float fn[128], float wn[128]);
float r4_nor(uint32_t *jsr, uint32_t kn[128], float fn[128], float wn[128]);
uint32_t shr3_seeded(uint32_t *jsr);
float r4_uni(uint32_t *jsr);

int main(int argc, char*argv[]) {

	if (argc < 3 || argc > 4) {
		cout << "Kindly enter -t, size of input and optional parameter (give 1) to print unsorted numbers, created bucket and sorted numbers in files." << endl;
		return EXIT_FAILURE;
	}

	struct timeval t;
	double start_t, end_t, time_spent;

	int size = atoi(argv[2]);
	float* nos = (float*) malloc(size * sizeof(float));
	random_number_generator_normal(nos, size, size);

	if(argc == 4) {
		FILE *un = fopen("UnsortedNosGpu.txt", "w");
		for(int i=0; i<size; i++) {
			fprintf(un, "%f\n", nos[i]);
		}
		fclose(un);
	}

	int noOfBuckets = (size/3000) + 1;
	vector< vector<float> > buckets(noOfBuckets, vector<float>(1));
	buckets = putNumbersIntoBuckets(buckets, nos, size, argc);
	free(nos);

	//Create a contiguous number list from buckets to copy to global memory
	float *bucketsAsArray = (float*)malloc(size * sizeof(float));
	int ptr = 0;
	for(int i=0, j=buckets.size(); i<j; i++) {
		for(int k=1, l=buckets.at(i).size(); k<l; k++) {
			bucketsAsArray[ptr++] = buckets.at(i).at(k);
		}
	}

	//Store size of each bucket in an array cumulatively
	int *eachBucketSizeArray = (int*) malloc((buckets.size()+1) * sizeof(int));
	eachBucketSizeArray[0] = 0;
	for(int i=0, j=buckets.size(); i<j; i++) {
		eachBucketSizeArray[i+1] = eachBucketSizeArray[i] + buckets.at(i).size() - 1;
	}

	//Make the call to kernel
	float *bucketsAsArrayD;
	int *eachBucketSizeArrayD;
	cudaMalloc(&bucketsAsArrayD, size * sizeof(float));
	cudaMalloc(&eachBucketSizeArrayD, (buckets.size()+1) * sizeof(int));
	cudaMemcpy(bucketsAsArrayD, bucketsAsArray, size * sizeof(float), cudaMemcpyHostToDevice);
	cudaMemcpy(eachBucketSizeArrayD, eachBucketSizeArray, (buckets.size()+1) * sizeof(int), cudaMemcpyHostToDevice);

	dim3 dimGrid(noOfBuckets);
	dim3 dimBlock(1000);

	gettimeofday(&t, NULL);
	start_t = (t.tv_sec * 1000000.0) + t.tv_usec;
	sortBuckets<<<dimGrid, dimBlock>>> (bucketsAsArrayD, eachBucketSizeArrayD);

	cudaMemcpy(bucketsAsArray, bucketsAsArrayD, size * sizeof(float), cudaMemcpyDeviceToHost);
	gettimeofday(&t, NULL);
	end_t = (t.tv_sec * 1000000.0) + t.tv_usec;
	time_spent = end_t - start_t;

	cudaFree(bucketsAsArrayD);
	cudaFree(eachBucketSizeArrayD);

	printSortedNumbers(bucketsAsArray, time_spent, size, argc);
	return EXIT_SUCCESS;
}

vector< vector<float> > putNumbersIntoBuckets(vector< vector<float> > buckets, float nos[], int size, int argc) {
	double mean = calculateMean(nos, size);
	double stdDev = calculateStandardDeviation(nos, size, mean);
	double distanceFromStdDev = 0.0;
	for (int i = 0; i < size; i++) {
		distanceFromStdDev = (nos[i] - mean) / stdDev;
		double cdfVal = cdf(distanceFromStdDev);
		int bucketPosition = (int)(cdfVal * buckets.size());
		buckets.at(bucketPosition).push_back(nos[i]);
	}
	if(argc == 4) {
		FILE *ub = fopen("UnsortedBucketsGpu.txt", "w");
		for(int i=0; i<buckets.size(); i++) {
			fprintf(ub, "\nBucket %d size = %d\n\n", (i+1), buckets.at(i).size()-1);
		}
		fprintf(ub, "Bucket sizes list ends.\n\n");
		for(int i=0; i<buckets.size(); i++) {
			for(int j=1, k=buckets.at(i).size(); j<k; j++) {
				fprintf(ub, "%f\n", buckets.at(i).at(j));
			}
			fprintf(ub, "Bucket %d ends here with size = %d.\n\n", i+1, buckets.at(i).size()-1);
		}
		fclose(ub);
	}
	return buckets;
}

int printSortedNumbers(float* nums, double time_spent, int size, int argc) {
	FILE *tt = fopen("TimeTakenBucketSortGpu.txt", "a+");
	if (tt == NULL) {
		fprintf(stderr, "Can't open file to write output\n");
		return EXIT_FAILURE;
	}
	if(argc == 4) {
		FILE *sn = fopen("SortedNosGpu.txt", "w");
		if (sn == NULL) {
			fprintf(stderr, "Can't open file to write output\n");
			return EXIT_FAILURE;
		}
		for(int i=0; i<size; i++) {
			fprintf(sn, "%f\n", nums[i]);
		}
		fclose(sn);
	}
	fprintf(tt, "Total time required in seconds = %f for sorting %d numbers using GPU.\n", (time_spent / 1000000.0), size);
	fclose(tt);
	return EXIT_SUCCESS;
}

double calculateMean(float nos[], int size) {
	double sum = 0;
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

//http://www.johndcook.com/cpp_phi.html
double cdf(double x) {

    double a1 =  0.254829592;
    double a2 = -0.284496736;
    double a3 =  1.421413741;
    double a4 = -1.453152027;
    double a5 =  1.061405429;
    double p  =  0.3275911;

    int sign = 1;
    if (x < 0) {
        sign = -1;
    }
    x = fabs(x)/sqrt(2.0);

    double t = 1.0/(1.0 + p*x);
    double y = 1.0 - (((((a5*t + a4)*t) + a3)*t + a2)*t + a1)*t*exp(-x*x);

    return 0.5*(1.0 + sign*y);
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

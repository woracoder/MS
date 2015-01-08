#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netdb.h>
#include <netinet/in.h>
#include <inttypes.h>
#include <errno.h>
#include <arpa/inet.h>
#include <sys/wait.h>
#include <signal.h>
#include <sys/stat.h>
#include <syslog.h>
#include <pthread.h>
#include <algorithm>
#include <string>
#include <time.h>
#include <sys/fcntl.h>
#include <iostream>
#include <fstream>
#include <list>
#include <sstream>
#include <strings.h>
#include <signal.h>
#include <dirent.h>
#include <vector>

#define BUF_SIZE 16384

using namespace std;

struct clientDetails {
	int fileDesId;
	string ip;
	string recTime;
};

struct requestDetails {
	struct clientDetails *clDt;
	string schedTime;
	string reqText;
	int reqStatus;
	int reqFileSize;
	string reqType;
	string filename;
	string protocol;
	string filepath;
	bool isFileRequest;
	string fileExtension;
	bool isRequestCorrect;
};

void intHandler(int);
void printUsage();
void *threadScheduling(void *);
bool sortRequestQueue(const requestDetails &, const requestDetails &);
void *serviceClientRequest(void *);
void start_processing(char *);
void sigchld_handler(int s);
void *get_in_addr(struct sockaddr *);
void checkAndVerifyIncomingData(int, char *, char *);
char* getStringFromText(string);
void deleteArray(char*);
bool checkIfFilePresent(char *);
void putRequestIntoQueue(requestDetails);
void createResponseForClientRequest(requestDetails);
void sendResponseToClient(requestDetails, string);
void checkIfFileOrDirExists(requestDetails);
void listFileAndFoldersInDirectory(requestDetails);
bool sortDirectoryByAlphabeticalOrder(const string &, const string &);

string schedPolicy = "FCFS";
int queueing_time = 60;
string rootdir = "";
int debug = 0, portNo;
char *port = "8080";
string logfile = "";
int n_threads = 4;
pthread_mutex_t reqQueueMutex;
pthread_cond_t reqQueueCond;
pthread_mutex_t schedQueueMutex;
pthread_cond_t schedQueueCond;
list<requestDetails> requestQueue;
list<requestDetails> scheduledQueue;

int main(int argc, char **argv) {

	signal(SIGINT, intHandler);

	char cwd[1024];
	if (getcwd(cwd, sizeof(cwd)) != NULL)
	   fprintf(stdout, "Current working dir: %s\n", rootdir.c_str());
    else
	   perror("getcwd() error");
	rootdir = cwd;

	int c;
	while ((c = getopt(argc, argv, "dhl:p:r:t:n:s:")) != -1) {
		switch (c) {
		case 'd':
			debug = 1;
			break;
		case 'h':
			printUsage();
			exit(1);
		case 'l':
			logfile = optarg;
			if(logfile.size()<1 || logfile.size()>255 || logfile.find('/', 0) != -1) {
				fprintf(stderr, "[error] Invalid log file name.\n");
				printUsage();
				exit(1);
			}
			break;
		case 'p':
			if (isdigit(*optarg)) {
				port = optarg;
				portNo = atoi(optarg);
			} else {
				fprintf(stderr, "[error] Port must be a number and should be greater than or equal to 1024.\n");
				printUsage();
				exit(1);
			}
			if (portNo < 1024) {
				fprintf(stderr, "[error] Port number must be greater than or equal to 1024.\n");
				printUsage();
				exit(1);
			}
			break;
		case 'r':
			rootdir = optarg;
			struct stat sb;
			if (stat(rootdir.c_str(), &sb) < 0 || !S_ISDIR(sb.st_mode)) {
				fprintf(stderr, "[error] The provided root directory does not exist.\n");
				printUsage();
			}
			break;
		case 't':
			if(isdigit(*optarg))
				queueing_time = atoi(optarg);
			else {
				fprintf(stderr, "[error] Queuing time must be a number and should be greater than 0.\n");
				printUsage();
				exit(1);
			}
			if (queueing_time < 1) {
				fprintf(stderr, "[error] Queuing time must be greater than 0.\n");
				printUsage();
				exit(1);
			}
			break;
		case 'n':
			if(isdigit(*optarg))
				n_threads = atoi(optarg);
			else {
				fprintf(stderr, "[error] Number of threads must be a number and should be greater than 0.\n");
				printUsage();
				exit(1);
			}
			if (n_threads < 1) {
				fprintf(stderr, "[error] Number of threads must be greater than 0.\n");
				printUsage();
				exit(1);
			}
			break;
		case 's':
			schedPolicy = optarg;
			if(!strcasecmp(schedPolicy.c_str(), "FCFS") || !strcasecmp(schedPolicy.c_str(), "SJF")) {
				fprintf(stderr, "[error] Scheduling policy can be either FCFS or SJF.\n");
				printUsage();
				exit(1);
			}
			break;
		default:
			printUsage();
			exit(1);
		}
	}

	if (debug == 1) {
		fprintf(stdout, "myhttpd logging to stdout");
		fprintf(stdout, "myhttpd port number: %s\n", port);
		fprintf(stdout, "myhttpd root directory: %s\n", rootdir.c_str());
		fprintf(stdout, "myhttpd queuing time in seconds = %d\n", queueing_time);
		fprintf(stdout, "myhttpd number of threads = %d\n", n_threads);
		fprintf(stdout, "myhttpd scheduling policy: %s\n", schedPolicy.c_str());
		n_threads = 1;
	} else if (debug == 0) {
		pid_t pid, sid;
		pid = fork();
		if (pid < 0) {
			exit(EXIT_FAILURE);
		}
		if (pid > 0) {
			exit(EXIT_SUCCESS);
		}
		umask(0);
		sid = setsid();
		if (sid < 0) {
			exit(EXIT_FAILURE);
		}
		if ((chdir(rootdir.c_str())) < 0) {
			exit(EXIT_FAILURE);
		}
		close(STDIN_FILENO);
		close(STDOUT_FILENO);
		close(STDERR_FILENO);
		openlog("myhttpd", LOG_CONS | LOG_NDELAY | LOG_NOWAIT | LOG_PID, LOG_LOCAL0);
	}

	pthread_t schedulerThread;
	pthread_t threadPool[n_threads];

	pthread_mutex_init(&reqQueueMutex, NULL);
	pthread_cond_init(&reqQueueCond, NULL);
	pthread_mutex_init(&schedQueueMutex, NULL);
	pthread_cond_init(&schedQueueCond, NULL);

	pthread_create(&schedulerThread, NULL, &threadScheduling, NULL);

	for (int i = 0; i < n_threads; i++)
		pthread_create(&threadPool[i], NULL, &serviceClientRequest, NULL);

	start_processing(port);

	pthread_mutex_destroy(&reqQueueMutex);
	pthread_cond_destroy(&reqQueueCond);
	pthread_mutex_destroy(&schedQueueMutex);
	pthread_cond_destroy(&schedQueueCond);
	pthread_exit(NULL);
}

void intHandler(int signum) {
   pthread_exit(NULL);
   exit(signum);
}

void printUsage(void) {
	fprintf(stderr, "Usage: myhttpd [−d] [−h] [−l logFilename] [−p portNumber] [−r rootDirectory] [−t threadWaitTimeInSecs] "
			"[−n numberOfWorkerThreads] [−s schedulingPolicy]\n");
	fprintf(stderr, "\t−d : This option enables debugging mode for the web server myhttpd.\n"
					"\t\t In debugging mode the web server myhttpd will not run as a daemon process and only accept one request at a time.\n"
					"\t\t Logging will be done to stdout.\n "
					"\t\t Without this option, the web server myt\httpd will run as a daemon process in the background.\n"
					"\t−h : This option prints a usage summary with all options for the web server myhttpd and exits.\n"
					"\t−l file : This option logs all the requests sent to the web server myhttpd to the given file.\n"
					"\t−p port : Listen on the given port. If not provided, the web server myhttpd will listen on port 8080.\n"
					"\t−r dir : Set the root directory for the web server myhttpd to dir.\n"
					"\t−t time : Set the queuing time to time in seconds. The default is 60 seconds.\n"
					"\t−n threadnum : Set number of threads waiting ready in the execution thread pool to threadnum.\n"
					"\t\t The default will be 4 execution threads.\n"
					"\t−s sched : Set the scheduling policy. It can be either FCFS or SJF. The default will be FCFS.\n");
}

void *threadScheduling(void *arg) {
	sleep(queueing_time);
	while (1) {
		pthread_mutex_lock(&reqQueueMutex);
		while (requestQueue.empty())
			pthread_cond_wait(&reqQueueCond, &reqQueueMutex);
		if (!strcasecmp(schedPolicy.c_str(), "SJF"))
			requestQueue.sort(sortRequestQueue);
		requestDetails rd = requestQueue.front();
		requestQueue.pop_front();
		pthread_mutex_unlock(&reqQueueMutex);
		pthread_mutex_lock(&schedQueueMutex);
		scheduledQueue.push_back(rd);
		pthread_cond_signal(&schedQueueCond);
		pthread_mutex_unlock(&schedQueueMutex);
	}
}

bool sortRequestQueue(const requestDetails &small, const requestDetails &large) {
	return small.reqFileSize < large.reqFileSize;
}

void *serviceClientRequest(void *arg) {
	pthread_detach(pthread_self());
	while(1) {
		pthread_mutex_lock(&schedQueueMutex);
		while(scheduledQueue.empty())
			pthread_cond_wait(&schedQueueCond, &schedQueueMutex);
		requestDetails rd = scheduledQueue.front();
		scheduledQueue.pop_front();
		time_t now = time(NULL);
		rd.schedTime = asctime(gmtime((const time_t*) &now));
		pthread_mutex_unlock(&schedQueueMutex);
		createResponseForClientRequest(rd);
	}
}

void start_processing(char *port) {

	char hostName[256];
	struct hostent *hostDetail;
	gethostname(hostName, 256);
	hostDetail = gethostbyname(hostName);

	struct addrinfo addrInfo, *serverInfo, *temp;
	memset(&addrInfo, 0, sizeof(addrInfo));
	addrInfo.ai_family = AF_INET;
	addrInfo.ai_socktype = SOCK_STREAM;
	addrInfo.ai_flags = AI_PASSIVE;

	int status;
	if ((status = getaddrinfo(hostDetail->h_name, port, &addrInfo, &serverInfo)) != 0) {
		fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(status));
	}

	int socFileDes, yes = 1;
	for (temp = serverInfo; temp != NULL; temp = temp->ai_next) {
		if ((socFileDes = socket(temp->ai_family, temp->ai_socktype, temp->ai_protocol)) == -1) {
			perror("server: socket");
			continue;
		}
		if (setsockopt(socFileDes, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int)) == -1) {
			perror("setsockopt");
			exit(1);
		}
		if (bind(socFileDes, temp->ai_addr, temp->ai_addrlen) == -1) {
			close(socFileDes);
			perror("server: bind");
			continue;
		}
		break;
	}

	if (temp == NULL) {
		fprintf(stderr, "server: failed to bind\n");
	}

	freeaddrinfo(serverInfo);
	if (listen(socFileDes, 10) == -1) {
		perror("listen");
		exit(1);
	}

	struct sigaction sa;
	sa.sa_handler = sigchld_handler;
	sigemptyset(&sa.sa_mask);
	sa.sa_flags = SA_RESTART;
	if (sigaction(SIGCHLD, &sa, NULL) == -1) {
		perror("sigaction");
		exit(1);
	}

	int newFileDes, numBytes;
	char remoteIP[INET_ADDRSTRLEN], bufArr[BUF_SIZE];
	socklen_t sin_size;
	struct sockaddr_storage their_addr;
	sin_size = sizeof their_addr;

	while (1) {
		newFileDes = accept(socFileDes, (struct sockaddr *) &their_addr, &sin_size);
		if (newFileDes == -1) {
			perror("accept");
			continue;
		}

		inet_ntop(their_addr.ss_family, get_in_addr((struct sockaddr *) &their_addr), remoteIP, sizeof remoteIP);

		while ((numBytes = recv(newFileDes, bufArr, BUF_SIZE - 1, 0)) == -1) {

		}
		bufArr[numBytes] = '\0';

		checkAndVerifyIncomingData(newFileDes, remoteIP, bufArr);
	}
}

void sigchld_handler(int s) {
	while (waitpid(-1, NULL, WNOHANG) > 0);
}

void *get_in_addr(struct sockaddr *sa) {
	if (sa->sa_family == AF_INET)
		return &(((struct sockaddr_in*) sa)->sin_addr);
	return &(((struct sockaddr_in6*) sa)->sin6_addr);
}

void checkAndVerifyIncomingData(int newFileDes, char *remoteIP, char *bufArr) {

	clientDetails cd;
	cd.fileDesId = newFileDes;
	cd.ip = remoteIP;
	time_t now = time(NULL);
	cd.recTime = asctime(gmtime((const time_t*) &now));

	requestDetails rd;
	rd.clDt = &cd;

	string requestLine(bufArr);

	int pos;
	if ((pos = requestLine.find_first_of("\r\n", 0)) == -1) {
		rd.isRequestCorrect = false;
	} else {
		rd.reqText = requestLine.substr(0, pos);
		char* buf = getStringFromText(rd.reqText);
		char *p = strtok(buf, " ");
		string topLine[3];
		topLine[0].assign(p);
		for (int i = 1; i < 3; i++) {
			p = strtok(NULL, " ");
			topLine[i].assign(p);
		}
		rd.reqType = topLine[0];
		rd.filename = topLine[1];
		rd.protocol = topLine[2];

		rd.filepath = rootdir + rd.filename;

		checkIfFileOrDirExists(rd);
		if((rd.reqType == "GET" || rd.reqType == "HEAD") && (rd.protocol == "HTTP/1.0"  || rd.protocol == "HTTP/1.1"))
			rd.isRequestCorrect = true;
		else
			rd.isRequestCorrect = true;
		deleteArray(buf);
	}
	if (debug)
		createResponseForClientRequest(rd);
	else
		putRequestIntoQueue(rd);
}

void checkIfFileOrDirExists(requestDetails rd) {

	int tp = rd.filepath.find_first_of("~",0);
	if(tp>0 && tp<rd.filepath.size()) {
		int sp = rd.filepath.find_first_of("/", tp);
		string username = rd.filepath.substr(tp+1, sp-(tp+1));
		string remainingpath = rd.filepath.substr(sp, rd.filepath.size()-sp);
		rd.filepath.erase(tp, rd.filepath.size());
		rd.filepath = rd.filepath + username + "/myhttpd" + remainingpath;
	}

	DIR *d = opendir(rd.filepath.c_str());
	if(d != NULL) {
		rd.isRequestCorrect = true;
		FILE *indexFile = fopen((rd.filepath+"/index.html").c_str(), "r");
		if(indexFile != NULL) {
			rd.isFileRequest = true;
			rd.filepath = rd.filepath+"/index.html";
			fseek(indexFile, 0, SEEK_END);
			rd.reqFileSize = ftell(indexFile);
			fclose(indexFile);
		} else {
			rd.isFileRequest = false;
			rd.reqFileSize = 0;
		}
	} else {
		 FILE * file = fopen(rd.filepath.c_str(), "r");
		 if(file != NULL) {
			 rd.isRequestCorrect = true;
			 rd.isFileRequest = true;
			 fseek(file, 0, SEEK_END);
			rd.reqFileSize = ftell(file);
			fclose(file);
		 } else {
			 rd.isFileRequest= false;
			 rd.isRequestCorrect = false;
			 rd.reqFileSize = 0;
		 }
	}

}

char* getStringFromText(string text) {
	char* buf = new char[text.size() + 1];
	strcpy(buf, text.c_str());
	return buf;
}

void deleteArray(char* str) {
	delete[] str;
	str = NULL;
}

bool checkIfFilePresent(char *file) {
	struct stat check;
	if (stat(file, &check) != -1)
		return true;
	return false;
}

void putRequestIntoQueue(requestDetails rd) {
	pthread_mutex_lock(&reqQueueMutex);
	requestQueue.push_back(rd);
	pthread_cond_signal(&reqQueueCond);
	pthread_mutex_unlock(&reqQueueMutex);
}

void createResponseForClientRequest(requestDetails rd) {

	if(rd.isRequestCorrect) {
		if(rd.isFileRequest) {
			string fileExt = rd.filename.substr(rd.filename.find_last_of(".") + 1, rd.filename.size());

			if(!strcasecmp(fileExt.c_str(), "txt") || !strcasecmp(fileExt.c_str(), "html") || !strcasecmp(fileExt.c_str(), "htm"))
				rd.fileExtension = "text/html";
			else if (!strcasecmp(fileExt.c_str(), "gif") || !strcasecmp(fileExt.c_str(), "jpeg") || !strcasecmp(fileExt.c_str(), "jpg")
					|| !strcasecmp(fileExt.c_str(), "png") || !strcasecmp(fileExt.c_str(), "bmp") || !strcasecmp(fileExt.c_str(), "tif"))
				rd.fileExtension = "image/" + fileExt;
			else
				rd.fileExtension = "text/html";

			stringstream strStream;
			strStream << rd.protocol + " 200 OK\r\nDate: ";
			time_t now = time(NULL);
			strStream << asctime(gmtime((const time_t*) &now));
			//strStream << "\r\n";
			strStream << "Server: myhttpd 1.0\r\n";
			strStream << "Last-Modified: ";
			struct stat buf;
			char *filename = getStringFromText(rd.filepath);
			stat(filename, &buf);
			deleteArray(filename);
			strStream << ctime(&buf.st_mtime);
			strStream << "Content-Type: " + rd.fileExtension + "\r\n";
			strStream << "Content-Length: ";
			strStream << "\r\n\r\n";
			string response = strStream.str();
			sendResponseToClient(rd, response);
		} else {
			listFileAndFoldersInDirectory(rd);
			close(rd.clDt->fileDesId);
		}
	} else {
		write(rd.clDt->fileDesId, "Error 404: Directory was not found", 34);
		rd.reqStatus = 404;
		close(rd.clDt->fileDesId);
	}
}

void sendResponseToClient(requestDetails rd, string resHead) {

	if (send(rd.clDt->fileDesId, resHead.c_str(), resHead.length()+1, 0) == -1)
		perror("Error while sending the request header.");
	//close(rd.clDt->fileDesId);
	rd.reqStatus = 200;
	if (rd.reqType == "GET") {
		char *buf;
		size_t size;
		ifstream ifs;
		ifs.open(rd.filepath.c_str());
		if (ifs.is_open()) {
			ifs.seekg(0, ios::end);
			size = ifs.tellg();
			buf = new char[size];
			ifs.seekg(0, ios::beg);
			ifs.read(buf, size);
		} else
			cout << "Never went Inside" << endl;
		if (send(rd.clDt->fileDesId, buf, size, 0) == -1)
			perror("Error while sending the request body.");
		ifs.close();
		deleteArray(buf);
		close(rd.clDt->fileDesId);
	}

}

void listFileAndFoldersInDirectory(requestDetails rd) {
	struct dirent *ent;
	DIR *dir = NULL;
	vector<string> dirlist;
	if ((dir = opendir(rd.filepath.c_str())) == NULL) {
		write(rd.clDt->fileDesId, "Error 404: Directory was not found", 34);
		rd.reqStatus = 404;
	} else {
		while ((ent = readdir(dir)) != NULL) {
			string s(ent->d_name);
			dirlist.push_back(s);
		}
		vector<string>::iterator iter;
		std::sort(dirlist.begin(), dirlist.end(),
				sortDirectoryByAlphabeticalOrder);
		write(rd.clDt->fileDesId, "Directory and File Listing: ", 28);
		for (iter = dirlist.begin(); iter < dirlist.end(); iter++) {
			write(rd.clDt->fileDesId, (*iter).c_str(), strlen((*iter).c_str()));
			write(rd.clDt->fileDesId, "\n", 1);
		}
		closedir(dir);
	}
}

bool sortDirectoryByAlphabeticalOrder(const string &small, const string &large) {
	for (string::const_iterator left = small.begin(), right = large.begin();
			left != small.end() && right != large.end(); ++left, ++right)
		if (tolower(*left) < tolower(*right))
			return true;
		else if (tolower(*left) > tolower(*right))
			return false;
	if (small.size() < large.size())
		return true;
	return false;
}

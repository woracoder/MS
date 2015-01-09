/**
 * srao2_proj1.cpp :
 * Single file to handle the file sharing application.
 * Starts off as a server or a client on a given port.
 * Created for CSE 589 Spring 2014 Programming Assignment 1.
 * @author Sudarshan Rao
 * @created 8 February 2014
 */

#include <arpa/inet.h>
#include <netdb.h>
#include <netinet/in.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <unistd.h>
#include <cctype>
#include <iostream>
#include <iterator>
#include <map>
#include <string>
#include <utility>
#include <vector>
#include <sstream>
#include <fstream>
#include <sys/stat.h>
#include <time.h>
#include <sys/time.h>

//File descriptor for standard input
#define STDIN 0
#define GOOGLE_PORT "53"
#define GOOGLE_IP "8.8.8.8"
#define SPACE " "

using namespace std;

//Function definitions begin
void printUsage(void);
void validateInput(int argc, char* argv[]);
int listenToPortAndStdin(void);
string getHostIPAddress(void);
string getLocalHostName(void);
string getRealHostNameFromIP(string& ip);
int processUserCommands(const string& userCommand);
void printHelp(void);
void trim(string& s);
int serveRegisterCommand(char* first);
void initializeCommandIdMap(void);
int sendall(int s, char *buf, int *len);
void makeEntryIntoServerIpList(int id, int socFD, string& hostname,
		string& ipAdd, string& portNo);
void *get_in_addr(struct sockaddr *sa);
int recvall(int s);
void addNewClient(int s, string& portN);
void makeEntryIntoConnectionList(int fd, int hostTyp, string& hostname,
		string& ipAdd, string& portNo);
int getPortNoFromSockFd(int fd);
int serveConnectCommand(char* first);
int sendRejectToClient(int s);
void serveListCommand(void);
int serveTerminateCommand(char* first);
int handleRemoteHostExitTerminate(int s);
void broadcastServerIPList(void);
void serveExitCommand();
int serveUploadCommand(char* first);
int performFileUpload(int fd, string& filename, int bufSize);
int receiveUpload(int fd, unsigned int totalData, int bufSize);
int serveDownloadCommand(char* first);
int validateFileExistence(string& filename);
int serveUploadRequest(int s, string& filename);
int sendUploadRejectMessage(int sockfd);
//Function definitions end

//Global variables list starts
//Map to hold the command and its id mapping
map<string, unsigned int> commandIdMap;
map<unsigned int, string> idCommandMap;

//Structure to hold each host network details
struct serverIP {
	int id;
	string host;
	string ip;
	string port;
	int sockFd;
};

//Structure to hold the connection list for each process
struct conn {
	int id;
	int sockFd;
	//1 indicates server, 0 indicates other clients
	int hostType;
	string host;
	string ip;
	string port;
};

//Collection that holds the entire server IP list
vector<serverIP> serverIPList;

//Collection that holds the entire server IP list
vector<conn> connectionList;

//Variable to hold the maximum server IP list id
int silMaxId = 2;

//Variable to hold the maximum connection list id
int connlMaxId = 1;

//Variable that indicates if the process is a Server or Client
string processType;

//Variable to hold the port number on which this process is listening
string port;

//Variable to hold the real IP address of this process
string hostIP;

//Variable to hold the local host name
string localhostName;

//Variable to hold the real host name
string realHostName;

//Master file descriptor list
fd_set master;

//Temporary file descriptor list for select()
fd_set read_fds;

//Download file descriptor list
fd_set downloadFds;

//Maximum file descriptor number
int fdmax;

//Socket file descriptor to listen to for this process
int listener;

//Variable to read input entered by user on console
string userCommand;
//Global variables list ends

/**
 * The main method that takes the user input arguments and acts
 * accordingly.
 *
 * References:
 * 1) http://www.cplusplus.com
 * 2) Let us C by Yeshwant Kanetkar
 * 3) Beej guide
 * 4) https://publib.boulder.ibm.com
 * 5) http://stackoverflow.com
 * 6) The C programming language by K & R
 * 7) http://pubs.opengroup.org
 * 8) http://www.toptip.ca/2010/03/trim-leading-or-trailing-white-spaces.html
 * 9) https://ia600609.us.archive.org/22/items/TheUltimateSo_lingerPageOrWhyIsMyTcpNotReliable/
 * the-ultimate-so_linger-page-or-why-is-my-tcp-not-reliable.html
 */
int main(int argc, char* argv[]) {

	//2 arguments required
	if (argc != 3) {
		fprintf(stderr, "[error] Incorrect number of arguments provided.\n");
		fprintf(stderr,
				"[error] Please check the program usage section provided below and provide correct number of arguments.\n");
		printUsage();
		exit(EXIT_FAILURE);
	}

	//Indicates if the process is a Server or Client
	processType = argv[1];

	//Variable to hold the port number
	port = argv[2];

	//This function checks the input arguments
	validateInput(argc, argv);

	//Initialize the protocol map for command-id
	initializeCommandIdMap();

	//Start listening to the provided port as well as STDIN for user input
	listenToPortAndStdin();

} //Main ends

/**
 * Function to validate the input arguments provided by the user
 */
void validateInput(int argc, char* argv[]) {

	//Check if the 1st argument is "s" or "c" only else show error message, usage text and exit.
	if (strcmp(argv[1], "s") && strcmp(argv[1], "c")) {
		fprintf(stderr,
				"[error] The process type can either be s(Server) or c(Client).\n");
		printUsage();
		exit(EXIT_FAILURE);
	}
	//Validate port number
	//Check if entered port number is not empty
	if (strlen(argv[2])) {
		//Check if port is a number
		char * portNo = argv[2];
		while (*portNo != '\0') {
			if (isdigit(*portNo)) {
				portNo++;
			} else {
				fprintf(stderr, "[error] Port entered should be a number.\n");
				printUsage();
				exit(EXIT_FAILURE);
			}
		}
		//Validate range of port number provided
		if (atoi(argv[2]) < 1024 || atoi(argv[2]) > 65535) {
			fprintf(stderr,
					"[error] Port number should be greater than or equal to 1024 and less than or equal to 65535.\n");
			printUsage();
			exit(EXIT_FAILURE);
		}
	} else {
		fprintf(stderr,
				"[error] Port number should not be blank and should be a number.\n");
		printUsage();
		exit(EXIT_FAILURE);
	}
} //validateInput ends

/**
 * Function to print usage of program on the console to the user.
 */
void printUsage(void) {
	fprintf(stderr, "Usage: ./RFSS (s/c) (port number)\n"
			"Sample usage as server: ./RFSS s 4573\n"
			"Sample usage as client: ./RFSS c 4578\n"
			"\t s - to run the process as a server.\n"
			"\t c - to run the process as a client.\n"
			"\t port number - port specific to this server or client.\n");
	fflush(stderr);
} //printUsage ends

/**
 * Function to initialize the command id map for protocol
 */
void initializeCommandIdMap(void) {
	commandIdMap["REGISTER"] = 1;
	idCommandMap[1] = "REGISTER";
	commandIdMap["SERVERIPLIST"] = 2;
	idCommandMap[2] = "SERVERIPLIST";
	commandIdMap["REGISTERREJECT"] = 3;
	idCommandMap[3] = "REGISTERREJECT";
	commandIdMap["CONNECT"] = 4;
	idCommandMap[4] = "CONNECT";
	commandIdMap["CONNECTREJECT"] = 5;
	idCommandMap[5] = "CONNECTREJECT";
	commandIdMap["CONNECTACCEPT"] = 6;
	idCommandMap[6] = "CONNECTACCEPT";
	commandIdMap["UPLOAD"] = 7;
	idCommandMap[7] = "UPLOAD";
	commandIdMap["UPLOADREQUEST"] = 8;
	idCommandMap[8] = "UPLOADREQUEST";
	commandIdMap["UPLOADREJECT"] = 9;
	idCommandMap[9] = "UPLOADREJECT";
} //End initializeCommandIdMap

/**
 * Function to listen to given port and console input of user.
 */
int listenToPortAndStdin(void) {

	//Clear the master and temporary file descriptor lists
	FD_ZERO(&master);
	FD_ZERO(&read_fds);

	//Get the IP address of this process
	hostIP = getHostIPAddress();

	//Get the local host name
	localhostName = getLocalHostName();

	//Get the real host name
	realHostName = getRealHostNameFromIP(hostIP);

	/**
	 * Hints - addrinfo filled out to pass to getaddrinfo
	 * ai - head node of addrinfo linkedlist returned by getaddrinfo that contains all relevant IP information
	 * p - temporary variable to iterate over ai linkedlist returned by getaddrinfo
	 */
	struct addrinfo hints, *ai, *p;

	//Make sure that the struct is empty
	memset(&hints, 0, sizeof hints);

	//Specifying to use IP family, TCP socket and self IP.
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_PASSIVE;

	//Return value variable to check status of getaddrinfo call.
	int rv;

	//Call to getaddrinfo to get network information of the host.
	if ((rv = getaddrinfo(NULL, port.c_str(), &hints, &ai)) != 0) {
		fprintf(stderr, "select server: %s\nExiting.\n", gai_strerror(rv));
		fflush(stderr);
		exit(EXIT_FAILURE);
	}

	//for setsockopt() SO_REUSEADDR, below
	int yes = 1;

	//Iterate over the addrinfo linked list returned by getaddrinfo to get the relevant network details of the host
	for (p = ai; p != NULL; p = p->ai_next) {

		//Get the socket file descriptor by making the socket call
		if ((listener = socket(p->ai_family, p->ai_socktype, p->ai_protocol))
				< 0) {
			perror("Problem in the socket call: socket");
			continue;
		}

		// lose the pesky "address already in use" error message
		if (setsockopt(listener, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int))
				== -1) {
			perror("Problem in the set sock call: setsockopt.\nExiting.\n");
			exit(EXIT_FAILURE);
		}

		//Bind to the socket file descriptor
		if (bind(listener, p->ai_addr, p->ai_addrlen) == -1) {
			close(listener);
			perror("Problem in the binding call: bind");
			continue;
		}

		break;
	}

	// if below condition is true, it means we didn't get bound
	if (p == NULL) {
		fprintf(stderr, "select server: failed to bind\nExiting.\n");
		fflush(stderr);
		exit(EXIT_FAILURE);
	}

	//Free the linked list as we have extracted the required information
	freeaddrinfo(ai);

	// listen
	if (listen(listener, 10) == -1) {
		perror("Problem while listening: listen.\nExiting.\n");
		exit(EXIT_FAILURE);
	}

	//Add entry to server ip map if server process
	if (!strcmp(processType.c_str(), "s")) {
		makeEntryIntoServerIpList(1, listener, realHostName, hostIP, port);
	}

	//Add STDIN and the listener to the master set
	FD_SET(STDIN, &master);
	FD_SET(listener, &master);

	//Keep track of the biggest file descriptor so far, it's this one
	fdmax = listener;

	//Variable to hold the new file descriptor value returned by accept
	int newFd;

	//Variable to hold the details of the remote host connecting to this host
	struct sockaddr_in remote_addr;
	remote_addr.sin_family = AF_INET;

	//Variable to hold the size of the remote address structure
	socklen_t addrlen;

	//Variable to hold the remote IP
	char remoteIP[INET_ADDRSTRLEN];

	//Continuously check for any activity on the file descriptors
	while (1) {

		//Copy master file descriptor set into read file descriptors set
		read_fds = master;

		//Make the select call which blocks
		if (select(fdmax + 1, &read_fds, NULL, NULL, NULL) == -1) {
			perror(
					"Problem faced in the select function call: select\nExiting.\n");
			exit(EXIT_FAILURE);
		}

		// run through the existing connections looking for data to read
		for (int i = 0; i <= fdmax; i++) {
			//Check if we got one new set file descriptor
			if (FD_ISSET(i, &read_fds)) {
				if (i == listener) {
					//handle new connections
					addrlen = sizeof(remote_addr);
					newFd = accept(listener, (struct sockaddr *) &remote_addr,
							&addrlen);
					if (newFd == -1) {
						perror("accept");
					} else {
						// add to master set
						FD_SET(newFd, &master);
						if (newFd > fdmax) {
							// keep track of the max
							fdmax = newFd;
						}
						printf("New connection request from %s on socket %d\n",
								inet_ntop(AF_INET,
										get_in_addr(
												(struct sockaddr*) &remote_addr),
										remoteIP, INET_ADDRSTRLEN), newFd);
					}
				}
				//Check if user entered a command
				else if (i == STDIN) {
					getline(cin, userCommand);
					trim(userCommand);
					if (strlen(userCommand.c_str())) {
						cout << "Command entered by user is: "
								<< userCommand.c_str() << endl;
						processUserCommands(userCommand);
					}
				}

				//Get data from existing connections
				else {
					//Receive all the data and decode it first
					if (recvall(i) == -1) {
						perror("Error occurred while sending data: sendall()");
					}
				}	//End get data from existing conns
			}	//End is fd set check
		}	//End for
	}	//End while
} //listenToPortAndStdin ends

/**
 * Function to get the host process IP address
 */
string getHostIPAddress(void) {

	//Return value variable to check status of getaddrinfo call.
	int rv;

	//Socket file descriptor to listen for this process
	int sockFd;

	/**
	 * Hints - addrinfo filled out to pass to getaddrinfo
	 * ai - head node of addrinfo linkedlist returned by getaddrinfo that contains all relevant IP information
	 * p - temporary variable to iterate over ai linkedlist returned by getaddrinfo
	 */
	struct addrinfo hints, *ai, *p;

	//Make sure that the struct is empty
	memset(&hints, 0, sizeof hints);

	//Code to get IP address
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;

	//Call to getaddrinfo to get network information of the host.
	if ((rv = getaddrinfo(GOOGLE_IP, GOOGLE_PORT, &hints, &ai)) != 0) {
		fprintf(stderr,
				"Problem connecting to google server.\nselect server: %s\n",
				gai_strerror(rv));
		fflush(stderr);
		exit(EXIT_FAILURE);
	}

	//Iterate over the addrinfo linked list returned by getaddrinfo to get the relevant network details of the host
	for (p = ai; p != NULL; p = p->ai_next) {

		//Get the socket file descriptor by making the socket call
		if ((sockFd = socket(p->ai_family, p->ai_socktype, p->ai_protocol))
				< 0) {
			perror("Problem in the socket call: socket");
			continue;
		}

		//Try to connect to the destination
		if (connect(sockFd, p->ai_addr, p->ai_addrlen) < 0) {
			close(sockFd);
			perror("Problem in the connect call: connect");
			continue;
		}

		break;
	}

	if (p == NULL) {
		fprintf(stderr, "server: failed to connect.\nExiting\n");
		fflush(stderr);
		exit(EXIT_FAILURE);
	}

	//All done with this structure
	freeaddrinfo(ai);

	//Array to hold the IP address
	char ip4[INET_ADDRSTRLEN];

	//Structure variable to get host network information from socket descriptor
	struct sockaddr_in my_address;

	//Initialize my_address
	memset(&my_address, 0, sizeof(struct sockaddr_in));

	my_address.sin_family = AF_INET;

	//Variable to hold the length of my_address variable
	socklen_t len = sizeof(struct sockaddr_in);

	//Read the network information of this host process from socket descriptor
	if (getsockname(sockFd, (struct sockaddr*) &my_address, &len) < 0) {
		perror("Problem in getsockname()\nExiting.\n");
		exit(EXIT_FAILURE);
	}

	//Get the IP address in presentation format
	inet_ntop(AF_INET, &(my_address.sin_addr), ip4, INET_ADDRSTRLEN);

	//Return the IP if not null else print error message and exit
	if (ip4 == NULL) {
		fprintf(stderr, "IP lookup operation failed.\nExiting.\n");
		fflush(stderr);
		exit(EXIT_FAILURE);
	}

	//Close the socket file descriptor
	close(sockFd);

	cout << "IP Address: " << ip4 << endl;

	//Return the actual IP address
	return string(ip4);
} //getHostIPAddress ends

/**
 * Function to get the local host name.
 */
string getLocalHostName(void) {
	char hostname[256];
	gethostname(hostname, sizeof hostname);
	cout << "Local hostname: " << hostname << endl;
	return string(hostname);
} //getLocalHostName ends

/**
 * Function to get the host name from the ip address
 */
string getRealHostNameFromIP(string & ip) {
	struct sockaddr_in sa;
	char host[1024];
	char service[20];
	memset(&sa, 0, sizeof sa);
	inet_pton(AF_INET, ip.c_str(), &sa.sin_addr);
	sa.sin_family = AF_INET;
	if (getnameinfo((struct sockaddr*) &sa, sizeof sa, host, sizeof host,
			service, sizeof service, 0)) {
		fprintf(stderr,
				"Error occurred while trying to get host name. Please enter proper IP address.\n");
		fflush(stderr);
		return string("");
	}
	cout << "Real host name: " << string(host) << endl;
	return string(host);
} //getRealHostName ends

/**
 * Function to make entry into the serverIp list
 */
void makeEntryIntoServerIpList(int id, int socFD, string& hostname,
		string& ipAdd, string& portNo) {
	struct serverIP entry;
	//If id = 0 then it is an entry for a client else id = 1 for server
	if (id == 0) {
		entry.id = silMaxId++;
	} else {
		entry.id = id;
	}
	entry.sockFd = socFD;
	entry.host = hostname;
	entry.ip = ipAdd;
	entry.port = portNo;
	serverIPList.push_back(entry);
} //End makeEntryIntoServerIpList

/**
 * Function to service the list command
 */
void serveListCommand(void) {
	//Iterate over the connection list and print the existing connections
	//If process is the server then print the server IP list
	if (!strcmp(processType.c_str(), "s")) {
		fprintf(stdout, "id:\tHostname\t\tIP Address\tPort No.\n");
		for (vector<serverIP>::iterator it = serverIPList.begin();
				it != serverIPList.end(); ++it) {
			fprintf(stdout, "%d:\t%s\t%s\t%s\n", it->id, it->host.c_str(),
					it->ip.c_str(), it->port.c_str());
		}
		fflush(stdout);
	}
	//Else if process is client print the connection list
	else {
		//If connection list is empty this means this host is not registered with the server
		if (connectionList.size() > 0) {
			fprintf(stdout, "id:\tHostname\t\tIP address\tPort No.\n");
			for (vector<conn>::iterator it = connectionList.begin();
					it != connectionList.end(); ++it) {
				fprintf(stdout, "%d:\t%s\t%s\t%s\n", it->id, it->host.c_str(),
						it->ip.c_str(), it->port.c_str());
			}
			fflush(stdout);
		} else {
			fprintf(stderr,
					"This process is not registered with the server.\n");
			fflush(stderr);
		}
	}
}

/**
 * This function takes appropriate actions based on the provided user commands
 */
int processUserCommands(const string& userCommand) {

	//Make a new string of user command as we need to tokenize this
	//char usrCom[userCommand.size()];
	char* usrComo = (char*) malloc(userCommand.size());
	memset(usrComo, 0, userCommand.size());

	char* usrCom = usrComo;

	//Copy the user command parameter to this editable string which will be tokenized
	strcpy(usrCom, userCommand.c_str());

	//Get the first word from the user command to check for commands having multiple space separated strings
	char* first = strtok(usrCom, SPACE);

	//A big else if block for all the user commands
	//Check if HELP command
	if (!strcasecmp(userCommand.c_str(), "HELP")) {
		printHelp();
	}

	//Check if MYIP command
	else if (!strcasecmp(userCommand.c_str(), "MYIP")) {
		cout << "The IP address of this process is: " << hostIP << endl;
	}

	//Check if MYPORT command
	else if (!strcasecmp(userCommand.c_str(), "MYPORT")) {
		cout
				<< "This process is listening for incoming connections by running on port: "
				<< port << endl;
	}

	//Check if the REGISTER command entered by the user is in correct format
	else if (!strcasecmp(first, "REGISTER")) {
		int i = serveRegisterCommand(first);
		free(usrComo);
		return i;
	}

	//Check if CONNECT command
	else if (!strcasecmp(first, "CONNECT")) {
		int i = serveConnectCommand(first);
		free(usrComo);
		return i;
	}

	//Check if LIST command
	else if (!strcasecmp(userCommand.c_str(), "LIST")) {
		serveListCommand();
	}

	//Check if TERMINATE command
	else if (!strcasecmp(first, "TERMINATE")) {
		int i = serveTerminateCommand(first);
		free(usrComo);
		return i;
	}

	//Check if EXIT command
	else if (!strcasecmp(userCommand.c_str(), "EXIT")) {
		serveExitCommand();
	}

	//Check if UPLOAD command
	else if (!strcasecmp(first, "UPLOAD")) {
		int i = serveUploadCommand(first);
		free(usrComo);
		return i;
	}

	//Check if DOWNLOAD command
	else if (!strcasecmp(first, "DOWNLOAD")) {
		int i = serveDownloadCommand(first);
		free(usrComo);
		return i;
	}

	//Check if CREATOR command
	else if (!strcasecmp(userCommand.c_str(), "CREATOR")) {
		fprintf(stdout, "Name: Sudarshan Surendra Rao\n"
				"UBIT Name: srao2\n"
				"UB email address: srao2@buffalo.edu\n");
		fflush(stdout);
	}

	//In case of incorrect commands print help
	else {
		fprintf(stderr, "Please enter correct commands.\n");
		fflush(stderr);
		printHelp();
	}
	free(usrComo);
	return 1;
} //processUserCommands ends

/**
 * Handle the exit command
 */
void serveExitCommand() {
	//Do not allow the server to exit unless its the only host left in the server IP list which means no clients are left.
	if (!strcmp(processType.c_str(), "s")) {
		if (serverIPList.size() > 1) {
			fprintf(stdout,
					"This program cannot exit as it is a server and has active clients registered with it.\n");
			fflush(stdout);
		} else {
			fprintf(stdout, "This server program will now exit.\n");
			fflush(stdout);
			exit(EXIT_SUCCESS);
		}
	} else {
		//Iterate over the entire connection list and close all connections and then exit the program
		for (vector<conn>::iterator it = connectionList.begin();
				it != connectionList.end(); ++it) {
			close(it->sockFd);
		}
		fprintf(stdout, "This client program will now exit.\n");
		fflush(stdout);
		exit(EXIT_SUCCESS);
	}
}

/**
 * Handle the terminate command
 */
int serveTerminateCommand(char* first) {

	//Check first if its a client or not
	if (!strcmp(processType.c_str(), "c")) {

		//Check number of arguments first and take IP & Port provided by user
		int count = 0;
		int conId;
		string conStr;
		while (first != NULL) {
			count++;
			first = strtok(NULL, SPACE);
			if (NULL != first) {
				if (count == 1) {
					conStr = first;
				} else {
					fprintf(stderr, "Incorrect number of arguments provided.\n"
							"Usage: Terminate <Connection ID>\n");
					fflush(stderr);
					return 1;
				}
			} else if (count < 2) {
				fprintf(stderr, "Incorrect number of arguments provided.\n"
						"Usage: Terminate <Connection ID>\n");
				fflush(stderr);
				return 1;
			}
		}

		//Check if the 2nd parameter provided as the connection ID is correct
		//Check if entered connection ID number is not empty
		if (strlen(conStr.c_str())) {
			//Check if connection ID is a number
			char* conIdB = (char*) malloc(conStr.size());
			memset(conIdB, 0, conStr.size());
			char* conNo = conIdB;
			strcpy(conNo, conStr.c_str());
			while (*conNo != '\0') {
				if (isdigit(*conNo)) {
					conNo++;
				} else {
					fprintf(stderr,
							"[error] Connection ID entered should be a number.\n");
					fflush(stderr);
					free(conIdB);
					return 1;
				}
			}
			free(conIdB);
		} else {
			fprintf(stderr,
					"[error] Connection ID should not be blank and should be a number.\n");
			fflush(stderr);
			return 1;
		}

		//Now put the string into the int as it has been validated to be an int
		conId = atoi(conStr.c_str());

		int socFd = -1, liCou = -1;

		//Now check if this connection ID is a valid ID in the connection list
		//Also do not allow to terminate if the connection ID is that of the server
		for (vector<conn>::iterator it = connectionList.begin();
				it != connectionList.end(); ++it) {
			liCou++;
			if (it->id == conId) {
				if (it->hostType == 1) {
					fprintf(stderr,
							"The TERMINATE command is valid to terminate only connections for client machines.\n");
					fflush(stderr);
					return 1;
				} else {
					socFd = it->sockFd;
					break;
				}
			}
		}

		//Terminate this connection and remove its entry from the connection list if sockFd != -1
		if (socFd != -1) {
			//Provide message to user
			fprintf(stdout,
					"Terminated connection with host %s running on IP address %s with Port No. %s.\n",
					connectionList[liCou].host.c_str(),
					connectionList[liCou].ip.c_str(),
					connectionList[liCou].port.c_str());
			fflush(stdout);
			//Remove entry from connection list
			connectionList.erase(connectionList.begin() + liCou);
			//Remove the fd from master set and close the connection
			if (socFd == fdmax) {
				fdmax--;
			}
			FD_CLR(socFd, &master);
			close(socFd);
			return 1;
		} else {
			fprintf(stderr,
					"The provided Connection ID is invalid and not present in the connection list.\n");
			fflush(stderr);
			return 1;
		}

	} else {
		fprintf(stderr,
				"The TERMINATE command is valid only for client machines.\n");
		fflush(stderr);
		return 1;
	}
}

/**
 * Function to handle the download command
 */
int serveDownloadCommand(char* first) {

	//Check first if its a client or not
	if (!strcmp(processType.c_str(), "c")) {

		//First check if even number of arguments & less than 7
		int count = 0;
		string idFile[6];
		while (first != NULL) {
			if (count > 6) {
				fprintf(stderr,
						"Incorrect number of arguments provided.\n"
								"Usage: DOWNLOAD <connection id 1> <file1> <connection id 2> <file2> <connection id 3> <file3>\n");
				fflush(stderr);
				return -1;
			}
			first = strtok(NULL, SPACE);
			if (NULL != first) {
				count++;
				idFile[count - 1] = first;
			} else if (count < 2) {
				fprintf(stderr,
						"Incorrect number of arguments provided.\n"
								"Usage: DOWNLOAD <connection id 1> <file1> <connection id 2> <file2> <connection id 3> <file3>\n");
				fflush(stderr);
				return -1;
			}
		}

		//Check for evenness of arguments
		if (count % 2 != 0) {
			fprintf(stderr,
					"Incorrect number of arguments provided.\n"
							"Usage: DOWNLOAD <connection id 1> <file1> <connection id 2> <file2> <connection id 3> <file3>\n");
			fflush(stderr);
			return -1;
		}

		//Check if every provided connection id is a valid number first
		for (int i = 0; i < count; i += 2) {
			//Check if entered connection id number is not empty
			if (strlen(idFile[i].c_str())) {
				//Check if connection id is a number
				char* conIdNoB = (char*) malloc(idFile[0].size());
				memset(conIdNoB, 0, idFile[0].size());
				char* conNo = conIdNoB;
				strcpy(conNo, idFile[0].c_str());
				while (*conNo != '\0') {
					if (isdigit(*conNo)) {
						conNo++;
					} else {
						fprintf(stderr,
								"[error] Connection ID entered should be a number.\n");
						fflush(stderr);
						free(conIdNoB);
						return -1;
					}
				}
				free(conIdNoB);
			} else {
				fprintf(stderr,
						"[error] Connection ID should not be blank and should be a number.\n");
				fflush(stderr);
				return -1;
			}
		}

		//Then check if each even argument in the array is a valid connection in the connection list
		//We cannot check the files from here so we will have a feedback mechanism
		bool isValid = false;
		for (int i = 0; i < count; i += 2) {
			isValid = false;
			for (vector<conn>::iterator it = connectionList.begin();
					it != connectionList.end(); ++it) {
				if (it->id == atoi(idFile[i].c_str())) {
					if (it->hostType == 1) {
						fprintf(stderr,
								"The DOWNLOAD command is valid to download files only from client machines.\n");
						fflush(stderr);
						return -1;
					} else {
						isValid = true;
						break;
					}
				}
			}
			if (!isValid) {
				fprintf(stderr,
						"The provided Connection ID is invalid and not present in the connection list.\n");
				fflush(stderr);
				return -1;
			}
		}

		//Now all the validations are done so we can send the remote hosts the download requests
		//i.e. request them to upload the required files

		//We put all the sock fds in a map along with their count as it is possible that 2 download requests can be made to the same host
		map<int, int> fdFreqMap;
		map<int, int>::iterator iter;
		//Clear the download file descriptor lists
		FD_ZERO(&downloadFds);
		//Max values among the fds
		int downFdMax = 0;

		//First get the sock fd for each connection and then send the required filename to that remote host with a request to upload the file
		for (int i = 0; i < count; i += 2) {
			for (vector<conn>::iterator it = connectionList.begin();
					it != connectionList.end(); ++it) {
				if (it->id == atoi(idFile[i].c_str())) {

					//If we find the entry in the connection list then add that fd to the download fd set,
					// the fd frequency map and frequency of that fd as value
					iter = fdFreqMap.find(it->sockFd);
					if (iter != fdFreqMap.end()) {
						iter->second = iter->second + 1;
					} else {
						if (it->sockFd > downFdMax) {
							downFdMax = it->sockFd;
						}
						fdFreqMap[it->sockFd] = 1;
						FD_SET(it->sockFd, &downloadFds);
					}

					int len = 2 * sizeof(unsigned int) + idFile[i + 1].length()
							+ 1;
					char* bufo = (char*) malloc(len);
					memset(bufo, 0, len);
					unsigned int command = htonl(
							commandIdMap.find("UPLOADREQUEST")->second);
					memcpy(bufo, &command, sizeof(unsigned int));
					unsigned int fnamLength = htonl(idFile[i + 1].length() + 1);
					memcpy(bufo + sizeof(unsigned int), &fnamLength,
							sizeof(unsigned int));
					memcpy(bufo + 2 * sizeof(unsigned int),
							idFile[i + 1].c_str(), idFile[i + 1].length());

					if (sendall(it->sockFd, bufo, &len) == -1) {
						fprintf(stdout,
								"Download request for file %s could not be sent to remote host %s with IP address %s "
										"and running on Port No. %s because an error occurred while sending the request.\n",
								idFile[i + 1].c_str(), it->host.c_str(),
								it->ip.c_str(), it->port.c_str());
						fflush(stdout);
						free(bufo);
						return -1;
					}
					free(bufo);
				}
			}
		}

		int fdcnt = count / 2;
		//Now iterate over the fd set for the downloads one by one and receive all the files
		while (1) {

			//Make the select call which blocks
			if (select(downFdMax + 1, &downloadFds, NULL, NULL, NULL) == -1) {
				perror(
						"Problem faced in the select function call while downloading files.\n");
				return -1;
			}

			// run through the existing connections looking for data to read
			for (int i = 0; i <= downFdMax; i++) {

				if (FD_ISSET(i, &downloadFds)) {
					//Buffer for receiving data
					char headBuf1[8];
					//Length first set to 8 bytes to get the command and length of data to be received
					int len1 = 8;
					//variable which checks how many bytes we've received
					int total1 = 0;
					//variable which checks how many we have left to receive
					int bytesleft1 = len1;
					//Variable to hold number of variables received in each recv call
					int n1;
					//Variable to hold the received command
					string com1;

					//Receive header data first of 8 bytes
					while (total1 < len1) {
						if ((n1 = recv(i, headBuf1 + total1, bytesleft1, 0))
								<= 0) {
							//This implies that the remote host has terminated its process
							return handleRemoteHostExitTerminate(i);
						} else {
							total1 += n1;
							bytesleft1 -= n1;
						}
					}

					//Check if 1st 8 bytes received
					//First 8 bytes read so we can decode the command
					//1st 4 bytes have total length of data sent
					char header1[4];
					memcpy(header1, headBuf1, 4);
					unsigned int * tmp1 = (unsigned int *) header1;
					unsigned int commandId1 = ntohl(*tmp1);
					com1 = idCommandMap.find(commandId1)->second;

					//Next 4 bytes have the command id
					memcpy(header1, headBuf1 + 4, 4);
					tmp1 = (unsigned int *) header1;
					unsigned int totalData1 = ntohl(*tmp1);
					bytesleft1 = totalData1;
					len1 = bytesleft1;
					total1 = 0;
					n1 = 0;

					if (!strcmp(com1.c_str(), "UPLOAD")) {
						int i = receiveUpload(i, totalData1, 1024);
					} else if (!strcmp(com1.c_str(), "UPLOADREJECT")) {
						fprintf(stderr,
								"The file requested for download does not exist on the remote machine.\n");
						fflush(stderr);
					}
					FD_CLR(i, &downloadFds);
				}
			}
			fdcnt--;
			if (fdcnt == 0) {
				return 1;
			}
		}
		FD_ZERO(&downloadFds);
	} else {
		fprintf(stderr,
				"The DOWNLOAD command is valid only for client machines.\n");
		fflush(stderr);
		return -1;
	}
	return 1;
}

/**
 * Function to handle the upload command
 */
int serveUploadCommand(char* first) {

	//Check first if its a client or not
	if (!strcmp(processType.c_str(), "c")) {

		//Check number of arguments first and take id & file name provided by user
		int count = 0;
		string idFile[2];
		while (first != NULL) {
			count++;
			first = strtok(NULL, SPACE);
			if (NULL != first) {
				if (count == 1) {
					idFile[0] = first;
				} else if (count == 2) {
					idFile[1] = first;
				} else {
					fprintf(stderr, "Incorrect number of arguments provided.\n"
							"Usage: Upload <Connection ID> <filename>\n");
					fflush(stderr);
					return -1;
				}
			} else if (count < 3) {
				fprintf(stderr, "Incorrect number of arguments provided.\n"
						"Usage: Upload <Connection ID> <filename>\n");
				fflush(stderr);
				return -1;
			}
		}

		char* conIdNoB;
		//Check if the 1st parameter provided as a connection id number is correct
		//Check if entered connection id number is not empty
		if (strlen(idFile[0].c_str())) {
			//Check if connection id is a number
			conIdNoB = (char*) malloc(idFile[0].size());
			memset(conIdNoB, 0, idFile[0].size());
			char* conNo = conIdNoB;
			strcpy(conNo, idFile[0].c_str());
			while (*conNo != '\0') {
				if (isdigit(*conNo)) {
					conNo++;
				} else {
					fprintf(stderr,
							"[error] Connection ID entered should be a number.\n");
					fflush(stderr);
					free(conIdNoB);
					return -1;
				}
			}
		} else {
			fprintf(stderr,
					"[error] Connection ID should not be blank and should be a number.\n");
			fflush(stderr);
			return -1;
		}

		int conId;
		string conStr(conIdNoB);
		free(conIdNoB);

		//Now put the string into the int as it has been validated to be an int
		conId = atoi(conStr.c_str());
		int socFd;
		bool isConValid = false;

		for (vector<conn>::iterator it = connectionList.begin();
				it != connectionList.end(); ++it) {
			if (it->id == conId) {
				if (it->hostType == 1) {
					fprintf(stderr,
							"The UPLOAD command is valid to upload files only to client machines.\n");
					fflush(stderr);
					return -1;
				} else {
					isConValid = true;
					socFd = it->sockFd;
					break;
				}
			}
		}

		if (!isConValid) {
			fprintf(stderr,
					"The provided Connection ID is invalid and not present in the connection list.\n");
			fflush(stderr);
			return -1;
		}

		//Validate file existence and if it is actually a file an not a directory
		int i = validateFileExistence(idFile[1]);
		if (i == 1) {
			//All validations are done now upload the file to the client
			int k = performFileUpload(socFd, idFile[1], 1024);
			return k;
		} else {
			return i;
		}

		return 1;
	} else {
		fprintf(stderr,
				"The UPLOAD command is valid only for client machines.\n");
		fflush(stderr);
		return -1;
	}
	return 1;
}

/**
 * Method that uploads the file to the remote client
 */
int performFileUpload(int fd, string& filename, int bufSize) {
	//Open the file in read mode
	ifstream ifile;
	ifile.open(filename.c_str(), ifstream::binary);
	if (!ifile.is_open()) {
		fprintf(stderr,
				"The provided file name and path is invalid and not present on this client.\n");
		fflush(stderr);
		return -1;
	}

	//Get the filename
	istringstream iss(filename);
	string fnam, token;
	while (getline(iss, token, '/')) {
		fnam = token;
	}

	// get length of file:
	ifile.seekg(0, ifile.end);
	int flength = ifile.tellg();
	ifile.seekg(0, ifile.beg);

	//Get remote host name from connection list based on fd
	string remoteHostname, remoteIP, remotePortNo;
	for (vector<conn>::iterator it = connectionList.begin();
			it != connectionList.end(); ++it) {
		if (it->sockFd == fd) {
			remoteHostname = it->host;
			remoteIP = it->ip;
			remotePortNo = it->port;
			break;
		}
	}

	fprintf(stdout,
			"Starting upload of file %s to the remote host %s with IP address %s and running on port %s.\n",
			filename.c_str(), remoteHostname.c_str(), remoteIP.c_str(),
			remotePortNo.c_str());
	fflush(stdout);

	//Send the header info to remote host
	if (ifile.good()) {
		//Length of data to be sent
		int len = 3 * sizeof(unsigned int) + fnam.length() + 1;
		char* bufo = (char*) malloc(len);
		memset(bufo, 0, len);
		unsigned int command = htonl(commandIdMap.find("UPLOAD")->second);
		memcpy(bufo, &command, sizeof(unsigned int));
		unsigned int dataLength = htonl(flength);
		memcpy(bufo + sizeof(unsigned int), &dataLength, sizeof(unsigned int));
		unsigned int fnamLength = htonl(fnam.length() + 1);
		memcpy(bufo + 2 * sizeof(unsigned int), &fnamLength,
				sizeof(unsigned int));
		memcpy(bufo + 3 * sizeof(unsigned int), fnam.c_str(), fnam.length());

		if (sendall(fd, bufo, &len) == -1) {
			fprintf(stdout,
					"File %s could not be uploaded to remote host %s with IP address %s and running on Port No. %s "
							"because an error occurred while sending the file.\n",
					filename.c_str(), remoteHostname.c_str(), remoteIP.c_str(),
					remotePortNo.c_str());
			fflush(stdout);
			free(bufo);
			return -1;
		}
		free(bufo);
	}

	//Buffer to read from the file
	char *fbuf = (char*) malloc(bufSize * sizeof(char));
	memset(fbuf, 0, bufSize);

	int total = 0;
	int bytesleft = flength;
	int buflen = bufSize;

	//Start the time to calculate upload time for the file
	struct timeval tv;
	gettimeofday(&tv, NULL);
	time_t startTime = tv.tv_sec;

	while (!ifile.eof() && ifile.good()) {
		if (total < flength) {
			if (bytesleft < bufSize) {
				ifile.read(fbuf, bytesleft);
				buflen = bytesleft;
			} else {
				ifile.read(fbuf, bufSize);
				buflen = bufSize;
			}
			if (sendall(fd, fbuf, &buflen) == -1) {
				fprintf(stdout,
						"File %s could not be uploaded to remote host %s with IP address %s and running on Port No. %s "
								"because an error occurred while sending the file.\n",
						filename.c_str(), remoteHostname.c_str(),
						remoteIP.c_str(), remotePortNo.c_str());
				fflush(stdout);
				free(fbuf);
				return -1;
			}
		}
		total += buflen;
		bytesleft -= buflen;
	}

	//File upload complete to get the end time
	gettimeofday(&tv, NULL);
	time_t totalTime = tv.tv_sec - startTime;

	if (totalTime <= 0)
		totalTime = 1;
	double txRate = (flength * 8) / totalTime;

	free(fbuf);
	ifile.close();

	istringstream istrs(realHostName);
	string partLocalHostName;
	string tok;
	getline(istrs, tok, '.');
	partLocalHostName = tok;

	istrs.clear();
	istrs >> remoteHostname;
	string partRemoteHostName;
	getline(istrs, tok, '.');
	partRemoteHostName = tok;

	fprintf(stdout,
			"File %s successfully uploaded to remote host %s with IP address %s and running on Port No. %s.\n",
			filename.c_str(), remoteHostname.c_str(), remoteIP.c_str(),
			remotePortNo.c_str());
	fprintf(stdout,
			"Tx (%s): %s -> %s, File Size: %u Bytes, Time Taken: %ld seconds, Tx Rate: %f bits/second.\n",
			partLocalHostName.c_str(), partLocalHostName.c_str(),
			partRemoteHostName.c_str(), flength, totalTime, txRate);
	fflush(stdout);
	return 1;
}

/**
 * Handle the CONNECT command
 */
int serveConnectCommand(char* first) {

	//Check first if its a client or not
	if (!strcmp(processType.c_str(), "c")) {

		//Check number of arguments first and take IP & Port provided by user
		int count = 0;
		string ipPort[2];
		while (first != NULL) {
			count++;
			first = strtok(NULL, SPACE);
			if (NULL != first) {
				if (count == 1) {
					ipPort[0] = first;
				} else if (count == 2) {
					ipPort[1] = first;
				} else {
					fprintf(stderr, "Incorrect number of arguments provided.\n"
							"Usage: Connect <Client IP> <Client Port>\n");
					fflush(stderr);
					return 1;
				}
			} else if (count < 3) {
				fprintf(stderr, "Incorrect number of arguments provided.\n"
						"Usage: CONNECT <Client IP> <Client Port>\n");
				fflush(stderr);
				return 1;
			}
		}

		//Check if the 2nd parameter provided as a port number is correct
		//Check if entered port number is not empty
		if (strlen(ipPort[1].c_str())) {
			//Check if port is a number
			char* portNoB = (char*) malloc(ipPort[1].size());
			memset(portNoB, 0, ipPort[1].size());
			char* portNo = portNoB;
			strcpy(portNo, ipPort[1].c_str());
			while (*portNo != '\0') {
				if (isdigit(*portNo)) {
					portNo++;
				} else {
					fprintf(stderr,
							"[error] Port entered should be a number.\n");
					fflush(stderr);
					free(portNoB);
					return 1;
				}
			}
			free(portNoB);
			//Validate range of port number provided
			if (atoi(ipPort[1].c_str()) < 1024
					|| atoi(ipPort[1].c_str()) > 65535) {
				fprintf(stderr,
						"[error] Port number should be greater than or equal to 1024 and less than or equal to 65535.\n");
				fflush(stderr);
				return 1;
			}
		} else {
			fprintf(stderr,
					"[error] Port number should not be blank and should be a number.\n");
			fflush(stderr);
			return 1;
		}

		//Do not allow localhost/127.0.0.1/hostname to register
		//TODO: change index to 0
		if (!strcmp(ipPort[0].c_str(), "localhost")
				|| !strcmp(ipPort[0].c_str(), "127.0.0.1")
				|| !strcmp(ipPort[0].c_str(), localhostName.c_str())
				|| !strcmp(ipPort[0].c_str(), realHostName.c_str())
				|| !strcmp(ipPort[0].c_str(), hostIP.c_str())) {
			fprintf(stderr, "Not allowed to connect to self.\n");
			fflush(stderr);
			return 1;
		}

		//Check if connected to server or not
		if (serverIPList.size() < 1) {
			fprintf(stderr,
					"Not allowed to directly connect to client.\nPlease register with the server first.\n");
			fflush(stderr);
			return 1;
		}

		//Check for limit of connections. No more than 3 connections are allowed, hence check if connection list size is <= 4
		if (connectionList.size() > 4) {
			fprintf(stderr,
					"Not allowed to connect to more than 3 clients simultaneously.\n"
							"Please terminate your connection with one of the clients first.\n");
			fflush(stderr);
			return 1;
		}

		//Check if already connected or not in the connection list which also checks for server
		for (vector<conn>::iterator it = connectionList.begin();
				it != connectionList.end(); ++it) {
			if (!strcmp(ipPort[0].c_str(), it->ip.c_str())
					|| !strcmp(ipPort[0].c_str(), it->host.c_str())) {
				fprintf(stderr, "You are already connected to this host/IP.\n");
				fflush(stderr);
				return 1;
			}
		}

		//cout << "Length of server IP list is :" << serverIPList.size() << endl;

		bool doesRemoteClientExist = false;
		//Then check if the destination host exists in the server IP list & then allow
		for (vector<serverIP>::iterator it = serverIPList.begin();
				it != serverIPList.end(); ++it) {
			if ((!strcmp(it->ip.c_str(), ipPort[0].c_str())
					|| !strcmp(it->host.c_str(), ipPort[0].c_str()))
					&& !strcmp(it->port.c_str(), ipPort[1].c_str())) {
				doesRemoteClientExist = true;
				break;
			}
		}

		if (!doesRemoteClientExist) {
			fprintf(stderr,
					"The client you want to connect to is not registered with the server.\n"
							"Please enter correct IP/Hostname and Port No.\n");
			fflush(stderr);
			return 1;
		}

		//All validations are done so now we can connect to that host by sending a message
		//Return value variable to check status of getaddrinfo call.
		int rv;

		//Socket file descriptor to listen for this process
		int sockFd;

		/**
		 * Hints - addrinfo filled out to pass to getaddrinfo
		 * ai - head node of addrinfo linkedlist returned by getaddrinfo that contains all relevant IP information
		 * p - temporary variable to iterate over ai linkedlist returned by getaddrinfo
		 */
		struct addrinfo hints, *ai, *p;

		//Make sure that the struct is empty
		memset(&hints, 0, sizeof hints);

		//Specifying to use IP family, TCP socket
		hints.ai_family = AF_INET;
		hints.ai_socktype = SOCK_STREAM;

		//Call to getaddrinfo to get network information of the host.
		if ((rv = getaddrinfo(ipPort[0].c_str(), ipPort[1].c_str(), &hints, &ai))
				!= 0) {
			fprintf(stderr,
					"select server: %s\nError while getting server information.\nExiting.\n",
					gai_strerror(rv));
			fflush(stderr);
			return 1;
		}

		//Iterate over the addrinfo linked list returned by getaddrinfo to get the relevant network details of the host
		for (p = ai; p != NULL; p = p->ai_next) {

			//Get the socket file descriptor by makeing the socket call
			if ((sockFd = socket(p->ai_family, p->ai_socktype, p->ai_protocol))
					< 0) {
				perror("Error during function call: socket");
				continue;
			}

			if (connect(sockFd, p->ai_addr, p->ai_addrlen) == -1) {
				close(sockFd);
				perror("Error while connecting to server: connect");
				continue;
			}
			break;
		}

		if (p == NULL) {
			fprintf(stderr, "client: failed to connect\n");
			fflush(stderr);
			return 1;
		}

		// all done with this structure
		freeaddrinfo(ai);

		//Add the file descriptor to the FD SET and increment max if required
		FD_SET(sockFd, &master);
		if (sockFd > fdmax) {
			fdmax = sockFd;
		}

		//Length of data to be sent
		int length = 2 * sizeof(unsigned int);
		char* buf = (char*) malloc(length);
		memset(buf, 0, length);
		unsigned int command = htonl(commandIdMap.find("CONNECT")->second);
		memcpy(buf, &command, sizeof(unsigned int));
		unsigned int dataLength = htonl(0);
		memcpy(buf + sizeof(unsigned int), &dataLength, sizeof(unsigned int));

		if (sendall(sockFd, buf, &length) == -1) {
			perror("Error occurred while sending data: sendall()\n");
			printf("We only sent %d bytes because of the error!\n", length);
			free(buf);
			return 1;
		}
		free(buf);
	} else {
		fprintf(stderr,
				"The CONNECT command is valid only for client machines.\n");
		fflush(stderr);
		return 1;
	}

	return 1;
}

/**
 * Handle the REGISTER command
 */
int serveRegisterCommand(char* first) {

	//Register command is valid only for clients
	if (!strcmp(processType.c_str(), "c")) {

		//Check number of arguments first and take IP & Port provided by user
		int count = 0;
		string ipPort[2];
		while (first != NULL) {
			count++;
			first = strtok(NULL, SPACE);
			if (NULL != first) {
				if (count == 1) {
					ipPort[0] = first;
				} else if (count == 2) {
					ipPort[1] = first;
				} else {
					fprintf(stderr, "Incorrect number of arguments provided.\n"
							"Usage: REGISTER <Server IP> <Server Port>\n");
					fflush(stderr);
					return 1;
				}
			} else if (count < 3) {
				fprintf(stderr, "Incorrect number of arguments provided.\n"
						"Usage: REGISTER <Server IP> <Server Port>\n");
				fflush(stderr);
				return 1;
			}
		}

		//Check if the 2nd parameter provided as a port number is correct
		//Check if entered port number is not empty
		if (strlen(ipPort[1].c_str())) {
			//Check if port is a number
			char* portNoB = (char*) malloc(ipPort[1].size());
			memset(portNoB, 0, ipPort[1].size());
			char* portNo = portNoB;
			strcpy(portNo, ipPort[1].c_str());
			while (*portNo != '\0') {
				if (isdigit(*portNo)) {
					portNo++;
				} else {
					fprintf(stderr,
							"[error] Port entered should be a number.\n");
					fflush(stderr);
					free(portNoB);
					return 1;
				}
			}
			free(portNoB);
			//Validate range of port number provided
			if (atoi(ipPort[1].c_str()) < 1024
					|| atoi(ipPort[1].c_str()) > 65535) {
				fprintf(stderr,
						"[error] Port number should be greater than or equal to 1024 and less than or equal to 65535.\n");
				fflush(stderr);
				return 1;
			}
		} else {
			fprintf(stderr,
					"[error] Port number should not be blank and should be a number.\n");
			fflush(stderr);
			return 1;
		}

		//Do not allow localhost/127.0.0.1/hostname to register
		//TODO: change index to 0
		if (!strcmp(ipPort[0].c_str(), "localhost")
				|| !strcmp(ipPort[0].c_str(), "127.0.0.1")
				|| !strcmp(ipPort[0].c_str(), localhostName.c_str())
				|| !strcmp(ipPort[0].c_str(), realHostName.c_str())
				|| !strcmp(ipPort[0].c_str(), hostIP.c_str())) {
			fprintf(stderr, "Not allowed to connect to self.\n");
			fflush(stderr);
			return 1;
		}

		//Check if already registered. If registered the serverIP list size will be > 0
		if (serverIPList.size() > 0) {
			fprintf(stderr,
					"You are already registered with the server so cannot register again.\n");
			fflush(stderr);
			return 1;
		}

		//All validations done so now connect to server and send your port number
		//Return value variable to check status of getaddrinfo call.
		int rv;

		//Socket file descriptor to listen for this process
		int sockFd;

		/**
		 * Hints - addrinfo filled out to pass to getaddrinfo
		 * ai - head node of addrinfo linkedlist returned by getaddrinfo that contains all relevant IP information
		 * p - temporary variable to iterate over ai linkedlist returned by getaddrinfo
		 */
		struct addrinfo hints, *ai, *p;

		//Make sure that the struct is empty
		memset(&hints, 0, sizeof hints);

		//Specifying to use IP family, TCP socket
		hints.ai_family = AF_INET;
		hints.ai_socktype = SOCK_STREAM;

		//Call to getaddrinfo to get network information of the host.
		if ((rv = getaddrinfo(ipPort[0].c_str(), ipPort[1].c_str(), &hints, &ai))
				!= 0) {
			fprintf(stderr,
					"select server: %s\nError while getting server information.\nExiting.\n",
					gai_strerror(rv));
			fflush(stderr);
			return 1;
		}

		//Iterate over the addrinfo linked list returned by getaddrinfo to get the relevant network details of the host
		for (p = ai; p != NULL; p = p->ai_next) {

			//Get the socket file descriptor by makeing the socket call
			if ((sockFd = socket(p->ai_family, p->ai_socktype, p->ai_protocol))
					< 0) {
				perror("Error during function call: socket");
				continue;
			}

			if (connect(sockFd, p->ai_addr, p->ai_addrlen) == -1) {
				close(sockFd);
				perror("Error while connecting to server: connect");
				continue;
			}
			break;
		}

		if (p == NULL) {
			fprintf(stderr, "client: failed to connect\n");
			fflush(stderr);
			return 1;
		}

		// all done with this structure
		freeaddrinfo(ai);

		//Add the file descriptor to the FD SET and increment max if required
		FD_SET(sockFd, &master);
		if (sockFd > fdmax) {
			fdmax = sockFd;
		}

		//Now send the port number to the server
		//Length of data to be sent
		int length = 2 * sizeof(unsigned int) + port.length() + 1;
		char* buf = (char*) malloc(length);
		memset(buf, 0, length);
		unsigned int command = htonl(commandIdMap.find("REGISTER")->second);
		memcpy(buf, &command, sizeof(unsigned int));
		unsigned int dataLength = htonl(port.length() + 1);
		memcpy(buf + sizeof(unsigned int), &dataLength, sizeof(unsigned int));
		memcpy(buf + 2 * sizeof(unsigned int), port.c_str(), port.length());

		if (sendall(sockFd, buf, &length) == -1) {
			perror("Error occurred while sending data: sendall()\n");
			printf("We only sent %d bytes because of the error!\n", length);
			free(buf);
			return 1;
		}
		free(buf);
	} else {
		fprintf(stderr,
				"The REGISTER command is valid only for client machines.\n");
		fflush(stderr);
		return 1;
	}
	return 1;
}	//serveRegisterCommand ends

/**
 * This function broadcasts the server IP list from the server to all the registered clients
 */
void broadcastServerIPList() {
	//Broadcast the server IP list to all the connected clients
	//Generate the serverIP list string
	stringstream ss;
	ss << "id:\tHostname\tIP Address\tPort No.\n";
	for (vector<serverIP>::iterator it = serverIPList.begin();
			it != serverIPList.end(); ++it) {
		ss << it->id << ":\t" << it->host << "\t" << it->ip << "\t" << it->port
				<< "\n";
	}
	string dataToSend = ss.str();
	//Length of data to be sent
	int len = 2 * sizeof(unsigned int) + dataToSend.length() + 1;
	char* buf = (char*) (malloc(len));
	memset(buf, 0, len);
	unsigned int command = htonl(commandIdMap.find("SERVERIPLIST")->second);
	memcpy(buf, &command, sizeof(unsigned int));
	unsigned int dataLength = htonl(dataToSend.length() + 1);
	memcpy(buf + sizeof(unsigned int), &dataLength, sizeof(unsigned int));
	memcpy(buf + 2 * sizeof(unsigned int), dataToSend.c_str(),
			dataToSend.length());
	//Iterate over the connection list & send the list to all clients
	for (vector<conn>::iterator it = connectionList.begin();
			it != connectionList.end(); ++it) {
		if (sendall(it->sockFd, buf, &len) == -1) {
			perror("Error occurred while sending data: sendall()\n");
			printf("We only sent %d bytes because of the error!\n", len);
			break;
		}
	} //End for
	free(buf);
}

/**
 * Serve the upload request from the remote host
 */
int serveUploadRequest(int s, string& filename) {

	//If the requested file exists on this host send initiate an upload by sending an UPLOADACCEPT message
	if (validateFileExistence(filename) == 1) {
		int k = performFileUpload(s, filename, 1024);
		if (k == -1) {
			return sendUploadRejectMessage(s);
		}
		return k;
	}
	//Else send UPLOADREJECT message
	else {
		return sendUploadRejectMessage(s);
	}
	return 1;
}

/**
 * Function to add a new client to the server ip list and then broadcast that list to the remaining clients
 */
void addNewClient(int s, string& portN) {

	socklen_t soclen;
	struct sockaddr_in addr;
	addr.sin_family = AF_INET;
	char ipstr[INET_ADDRSTRLEN];
	soclen = sizeof addr;
	getpeername(s, (struct sockaddr*) (&addr), &soclen);

	struct sockaddr_in *sin = (struct sockaddr_in *) &addr;
	inet_ntop(AF_INET, &sin->sin_addr, ipstr, sizeof ipstr);

	struct sockaddr_in sa;
	memset(&sa, 0, sizeof sa);
	inet_pton(AF_INET, ipstr, &sa.sin_addr);
	sa.sin_family = AF_INET;
	char host[1024];
	char service[20];
	if (getnameinfo((struct sockaddr*) &sa, sizeof sa, host, sizeof host,
			service, sizeof service, 0)) {
		fprintf(stderr, "Error occurred while trying to get host name.\n");
		fflush(stderr);
		exit(EXIT_FAILURE);
	}

	string hostNam(host);
	string ipAdd(ipstr);

	makeEntryIntoServerIpList(0, s, hostNam, ipAdd, portN);
	makeEntryIntoConnectionList(s, 0, hostNam, ipAdd, portN);

	broadcastServerIPList();
}

/**
 * Function that handles termination of connection by the remote host
 */
int handleRemoteHostExitTerminate(int s) {
	//If remote client terminated connection with server then
	//we first need to remove its entry from the server IP list & connection list &
	//then send the updated server IP list to the rest of the clients
	//And close the socket fd for that client
	if (!strcmp(processType.c_str(), "s")) {
		int index = -1;
		for (vector<conn>::iterator it = connectionList.begin();
				it != connectionList.end(); ++it) {
			index++;
			if (it->sockFd == s) {
				break;
			}
		}
		fprintf(stdout,
				"The remote host %s with IP Address %s and running on Port No. %s has terminated the connection.\n",
				connectionList[index].host.c_str(),
				connectionList[index].ip.c_str(),
				connectionList[index].port.c_str());
		fflush(stdout);

		connectionList.erase(connectionList.begin() + index);

		index = -1;
		for (vector<serverIP>::iterator it = serverIPList.begin();
				it != serverIPList.end(); ++it) {
			index++;
			if (it->sockFd == s) {
				if (s == fdmax) {
					fdmax--;
				}
				FD_CLR(s, &master);
				close(s);
				break;
			}
		}

		serverIPList.erase(serverIPList.begin() + index);
		broadcastServerIPList();
	}
	//Else remove the entry from the connection list and close the fd and display message to user
	else {
		int index = -1;
		for (vector<conn>::iterator it = connectionList.begin();
				it != connectionList.end(); ++it) {
			index++;
			if (it->sockFd == s) {
				if (s == fdmax) {
					fdmax--;
				}
				FD_CLR(s, &master);
				close(s);
				break;
			}
		}
		fprintf(stdout,
				"The remote host %s with IP Address %s and running on Port No. %s has terminated the connection.\n",
				connectionList[index].host.c_str(),
				connectionList[index].ip.c_str(),
				connectionList[index].port.c_str());
		fflush(stdout);
		connectionList.erase(connectionList.begin() + index);
	}
	return 1;
}

/**
 * Function that completes the UPLOAD
 */
int receiveUpload(int fd, unsigned int totalData, int bufSize) {

	//Buffer for receiving data
	char headBuf[4];
	//Length first set to 8 bytes to get the command and length of data to be received
	int len = 4;
	//variable which checks how many bytes we've received
	int total = 0;
	//variable which checks how many we have left to receive
	int bytesleft = len;
	//Variable to hold number of variables received in each recv call
	int n;

	//Receive header data first of 8 bytes
	while (total < len) {
		if ((n = recv(fd, headBuf + total, bytesleft, 0)) <= 0) {
			//This implies that the remote host has terminated its process
			return handleRemoteHostExitTerminate(fd);
		} else {
			total += n;
			bytesleft -= n;
		}
	}

	//1st 4 bytes have total length of file name
	char header[4];
	memcpy(header, headBuf, 4);
	unsigned int * tmp = (unsigned int *) header;
	unsigned int fnamLen = ntohl(*tmp);

	//Buffer to hold the actual file name
	char* fnamBufo = (char*) malloc(fnamLen * sizeof(char));
	memset(fnamBufo, 0, fnamLen);

	bytesleft = fnamLen;
	len = bytesleft;
	total = 0;
	n = 0;

	//Get remote host name from connection list based on fd
	string remoteHostname, remoteIP, remotePortNo;
	for (vector<conn>::iterator it = connectionList.begin();
			it != connectionList.end(); ++it) {
		if (it->sockFd == fd) {
			remoteHostname = it->host;
			remoteIP = it->ip;
			remotePortNo = it->port;
			break;
		}
	}

	//Second call to receive to get the file name
	//Receive header data first of 8 bytes
	while (total < len) {
		if ((n = recv(fd, fnamBufo + total, bytesleft, 0)) <= 0) {
			fprintf(stdout,
					"File could not be downloaded from remote host %s with IP address %s and running on Port No. %s "
							"because an error occurred while sending.\n",
					remoteHostname.c_str(), remoteIP.c_str(),
					remotePortNo.c_str());
			fflush(stdout);
			return handleRemoteHostExitTerminate(fd);
		} else {
			total += n;
			bytesleft -= n;
		}
	}

	string fnam(fnamBufo);
	free(fnamBufo);

	fprintf(stdout,
			"Starting download of file %s from the remote host %s with IP address %s and running on port %s.\n",
			fnam.c_str(), remoteHostname.c_str(), remoteIP.c_str(),
			remotePortNo.c_str());
	fflush(stdout);

	//Last call to recv to read the actual file data
	bytesleft = totalData;
	len = bytesleft;
	total = 0;
	n = 0;
	int partTotal = 0, partLeft = bufSize;
	char* dataBuf = (char*) malloc(bufSize * sizeof(char));
	memset(dataBuf, 0, bufSize);

	//Start the time to calculate upload time for the file
	struct timeval tv;
	gettimeofday(&tv, NULL);
	time_t startTime = tv.tv_sec;

	//Create a new file
	ofstream ofs(fnam.c_str(), ofstream::binary);
	if (ofs.is_open()) {
		while (total < len) {
			if (bytesleft < bufSize) {
				partLeft = bytesleft;
			} else {
				partLeft = bufSize;
			}
			partTotal = 0;
			while (partTotal < partLeft) {
				if ((n = recv(fd, dataBuf + partTotal, bufSize, 0)) <= 0) {
					free(dataBuf);
					fprintf(stdout,
							"File %s could not be downloaded from remote host %s with IP address %s and running on Port No. %s "
									"because an error occurred while receiving the file.\n",
							fnam.c_str(), remoteHostname.c_str(),
							remoteIP.c_str(), remotePortNo.c_str());
					fflush(stdout);
					return handleRemoteHostExitTerminate(fd);
				} else {
					partTotal += n;
					partLeft -= n;
				}
			}
			ofs.write(dataBuf, partTotal);
			total += n;
			bytesleft -= n;
		}
	} else {
		cout << "Error faced while opening file " << fnam.c_str()
				<< " while downloading. Aborting." << endl;
		ofs.close();
		free(dataBuf);
		return -1;
	}
	ofs.close();

	//File upload complete to get the end time
	gettimeofday(&tv, NULL);
	time_t totalTime = tv.tv_sec - startTime;

	if (totalTime <= 0)
		totalTime = 1;
	double txRate = (totalData * 8) / totalTime;

	istringstream iss(realHostName);
	string partLocalHostName;
	string token;
	getline(iss, token, '.');
	partLocalHostName = token;

	iss.clear();
	iss >> remoteHostname;
	string partRemoteHostName;
	getline(iss, token, '.');
	partRemoteHostName = token;

	fprintf(stdout,
			"File %s successfully downloaded from remote host %s with IP address %s and running on Port No. %s.\n",
			fnam.c_str(), remoteHostname.c_str(), remoteIP.c_str(),
			remotePortNo.c_str());
	fprintf(stdout,
			"Rx (%s): %s -> %s, File Size: %u Bytes, Time Taken: %ld seconds, Rx Rate: %f bits/second.\n",
			partLocalHostName.c_str(), partLocalHostName.c_str(),
			partRemoteHostName.c_str(), totalData, totalTime, txRate);
	fflush(stdout);
	free(dataBuf);
	return 1;
}

/**
 * Function to handle partial recvs
 */
int recvall(int s) {

	//Buffer for receiving data
	char headBuf[8];
	//Length first set to 8 bytes to get the command and length of data to be received
	int len = 8;
	//variable which checks how many bytes we've received
	int total = 0;
	//variable which checks how many we have left to receive
	int bytesleft = len;
	//Variable to hold number of variables received in each recv call
	int n;
	//Variable to hold the received command
	string com;

	//Receive header data first of 8 bytes
	while (total < len) {
		if ((n = recv(s, headBuf + total, bytesleft, 0)) <= 0) {
			//This implies that the remote host has terminated its process
			return handleRemoteHostExitTerminate(s);
		} else {
			total += n;
			bytesleft -= n;
		}
	}

	//Check if 1st 8 bytes received
	//First 8 bytes read so we can decode the command
	//1st 4 bytes have total length of data sent
	char header[4];
	memcpy(header, headBuf, 4);
	unsigned int * tmp = (unsigned int *) header;
	unsigned int commandId = ntohl(*tmp);
	com = idCommandMap.find(commandId)->second;

	//Next 4 bytes have the command id
	memcpy(header, headBuf + 4, 4);
	tmp = (unsigned int *) header;
	unsigned int totalData = ntohl(*tmp);
	bytesleft = totalData;
	len = bytesleft;
	total = 0;
	n = 0;

	//We get an upload command from other client
	if (!strcmp(com.c_str(), "UPLOAD")) {
		int i = receiveUpload(s, totalData, 1024);
		return i;
	}

	//Buffer to hold the actual data sent
	char* dataBufo = (char*) malloc(totalData * sizeof(char));
	memset(dataBufo, 0, totalData);
	char* dataBuf = dataBufo;

	//Second call to receive to get the actual data
	//Receive header data first of 8 bytes
	while (total < len) {
		if ((n = recv(s, dataBuf + total, bytesleft, 0)) <= 0) {
			return handleRemoteHostExitTerminate(s);
		} else {
			total += n;
			bytesleft -= n;
		}
	}

	string data(dataBuf);

	//Based on the command received perform the action
	//We get a UPLOADREQUEST from the remote client
	if (!strcmp(com.c_str(), "UPLOADREQUEST")) {
		int i = serveUploadRequest(s, data);
		free(dataBufo);
		return i;
	}

	//We get an UPLOADREJECT message from the remote client
	else if (!strcmp(com.c_str(), "UPLOADREJECT")) {
		fprintf(stderr, "%s\n", dataBuf);
		fflush(stderr);
	}

	//We get a REGISTER from the other client
	else if (!strcmp(com.c_str(), "REGISTER")) {

		//If the process type of this host is c then reject this connection and remove the fd from fd set & decrement fdmax by 1
		if (!strcmp(processType.c_str(), "c")) {
			int i = sendRejectToClient(s);
			free(dataBufo);
			return i;
		} else {
			//Add the client to the server IP list and the connection list
			addNewClient(s, data);
		}
	}	//End REGISTER command if

	//We get a REGISTERREJECT from the other client
	else if (!strcmp(com.c_str(), "REGISTERREJECT")) {
		fprintf(stderr, "%s\n", dataBuf);
		fflush(stderr);
		if (s == fdmax) {
			fdmax--;
		}
		FD_CLR(s, &master);
		close(s);
	}

	//We have all the data so just display it
	else if (!strcmp(com.c_str(), "SERVERIPLIST")) {
		//Display the list to the client
		fprintf(stdout, "Below is the current Server-Client list:\n%s",
				dataBuf);
		fflush(stdout);

		//Add to server IP list
		//First clear the list
		serverIPList.clear();

		//Now parse the received data and update the serverIPList
		string strDataBuf(dataBuf);

		istringstream iss(strDataBuf);
		string token;
		//Ignore the first header line
		getline(iss, token, '\n');
		while (getline(iss, token, '\n')) {
			istringstream iniss(token);
			string inTok;
			string data[4];
			int j = 0;
			while (getline(iniss, inTok, '\t')) {
				data[j++] = inTok;
			}
			makeEntryIntoServerIpList(atoi(data[0].c_str()), s, data[1],
					data[2], data[3]);
		}

		//If the connection list is empty now the it means this is the first time this client is receiving the server IP list
		//So we add the server entry to the connection list
		//Iterate over the server IP list and copy the entry with id 1 into the connection list
		if (connectionList.size() == 0) {
			for (vector<serverIP>::iterator it = serverIPList.begin();
					it != serverIPList.end(); ++it) {
				if (it->id == 1) {
					makeEntryIntoConnectionList(s, 1, it->host, it->ip,
							it->port);
					break;
				}
			}
		}
	}

	//We get a CONNECT request from another client
	else if (!strcmp(com.c_str(), "CONNECT")) {

		//First get the remote host details from its sockFD
		struct sockaddr_in addr;
		addr.sin_family = AF_INET;
		char ipstr[INET_ADDRSTRLEN];
		socklen_t length = sizeof addr;
		getpeername(s, (struct sockaddr*) (&addr), &length);
		if (addr.sin_family == AF_INET) {
			struct sockaddr_in *sin = (struct sockaddr_in *) &addr;
			inet_ntop(AF_INET, &sin->sin_addr, ipstr, sizeof ipstr);
		}

		string ipN(ipstr);
		string hostN, portNum;
		//Get the rest of the details of this client from the serverIP list
		for (vector<serverIP>::iterator it = serverIPList.begin();
				it != serverIPList.end(); ++it) {
			if (!strcmp(ipstr, it->ip.c_str())
					&& strcmp(port.c_str(), it->port.c_str())) {
				hostN = it->host;
				portNum = it->port;
				break;
			}
		}

		//If current client has more than 3 connections already it refuses the connection else accepts
		if (connectionList.size() > 4) {
			fprintf(stderr,
					"Rejecting connection from host %s with IP %s and listening port %s.\n",
					hostN.c_str(), ipN.c_str(), portNum.c_str());
			fflush(stderr);

			//Send negative response to that client
			string dataToSend =
					"Your CONNECT request is being rejected as the remote client is already connected to 3 other clients.\n";
			//Length of data to be sent
			int length = 2 * sizeof(unsigned int) + dataToSend.length() + 1;
			char* buf = (char*) malloc(length);
			memset(buf, 0, length);
			unsigned int command = htonl(
					commandIdMap.find("CONNECTREJECT")->second);
			memcpy(buf, &command, sizeof(unsigned int));
			unsigned int dataLength = htonl(dataToSend.length() + 1);
			memcpy(buf + sizeof(unsigned int), &dataLength,
					sizeof(unsigned int));
			memcpy(buf + 2 * sizeof(unsigned int), dataToSend.c_str(),
					dataToSend.length());

			if (sendall(s, buf, &length) == -1) {
				perror("Error occurred while sending data: sendall()\n");
				printf("We only sent %d bytes because of the error!\n", length);
			}

			if (s == fdmax) {
				fdmax--;
			}
			FD_CLR(s, &master);
			free(buf);
			close(s);
		} else {
			fprintf(stdout,
					"Accepting connection from host %s with IP %s and listening port %s.\n",
					hostN.c_str(), ipN.c_str(), portNum.c_str());
			fflush(stdout);
			//Add this host to the connection list
			makeEntryIntoConnectionList(s, 0, hostN, ipN, portNum);
			//Send positive response to that client
			stringstream ss;
			ss << realHostName << "\t" << hostIP << "\t" << port;
			string dataToSend = ss.str();
			//Length of data to be sent
			int length = 2 * sizeof(unsigned int) + dataToSend.length() + 1;
			char* buf = (char*) malloc(length);
			memset(buf, 0, length);
			unsigned int command = htonl(
					commandIdMap.find("CONNECTACCEPT")->second);
			memcpy(buf, &command, sizeof(unsigned int));
			unsigned int dataLength = htonl(dataToSend.length() + 1);
			memcpy(buf + sizeof(unsigned int), &dataLength,
					sizeof(unsigned int));
			memcpy(buf + 2 * sizeof(unsigned int), dataToSend.c_str(),
					dataToSend.length());

			if (sendall(s, buf, &length) == -1) {
				perror("Error occurred while sending data: sendall()\n");
				printf("We only sent %d bytes because of the error!\n", length);
			}
			free(buf);
		}
	}

	//Command received is CONNECTACCEPT
	else if (!strcmp(com.c_str(), "CONNECTACCEPT")) {
		//Now parse the received data and update the connection List
		string details[3];
		char* tmp = strtok(dataBuf, "\t");
		int i = 0;
		while (tmp != NULL) {
			details[i++] = tmp;
			tmp = strtok(NULL, "\t");
		}
		makeEntryIntoConnectionList(s, 0, details[0], details[1], details[2]);
		//Show the display message to the user
		fprintf(stdout,
				"Successfully connected to host %s having IP Address %s and listening on Port No. %s\n",
				details[0].c_str(), details[1].c_str(), details[2].c_str());
		fflush(stdout);
	}

	//Command received is CONNECTREJECT
	else if (!strcmp(com.c_str(), "CONNECTREJECT")) {
		fprintf(stderr, "%s\n", dataBuf);
		fflush(stderr);
		if (s == fdmax) {
			fdmax--;
		}
		FD_CLR(s, &master);
		close(s);
	}

// return number actually received here
	len = total;
	free(dataBufo);
// return -1 on failure, 0 on success
	return n == -1 ? -1 : 0;
} //recvall ends

/**
 * Function to trim a string
 */
void trim(string& s) {
	size_t p = s.find_first_not_of(" \t");
	s.erase(0, p);
	p = s.find_last_not_of(" \t");
	if (string::npos != p)
		s.erase(p + 1);
} //trim ends

/**
 * Function to handle partial sends
 */
int sendall(int s, char *buf, int *len) {
	//variable which checks how many bytes we've sent
	int total = 0;
	//variable which checks how many we have left to send
	int bytesleft = *len;
	int n;
	while (total < *len) {
		n = send(s, buf + total, bytesleft, 0);
		if (n == -1) {
			break;
		}
		total += n;
		bytesleft -= n;
	}
	// return number actually sent here
	*len = total;
	// return -1 on failure, 0 on success
	return n == -1 ? -1 : 0;
} //sendall ends

/**
 * Function to print the help screen
 */
void printHelp(void) {
	fprintf(stdout,
			"The available user interface options are:\n"
					"1) HELP: Displays this text message.\n"
					"2) MYIP: Displays the IP address of this process.\n"
					"3) MYPORT: Display the port on which this process is listening for incoming connections.\n"
					"4) REGISTER <server IP> <port_no>:\n"
					"\tThis command is used to register this process with the server and to get the IP and listening port\n"
					"\tnumbers of all the other peers currently registered with the server.\n"
					"\tSample usage: REGISTER 192.72.173.234 9823\n"
					"5) CONNECT <destination> <port no>:\n"
					"\tThis command establishes a new TCP connection to the specified <destination> at the specified < port no>.\n"
					"\tThe <destination> can either be an IP address or a hostname.\n"
					"\te.g.,CONNECT euston.cse.buffalo.edu 3456 or CONNECT 192.168.45.55 3456\n"
					"6) LIST: Displays a numbered list of all the connections this process is part of.\n"
					"\tThis numbered list includes connections initiated by this process and connections initiated by other processes.\n"
					"\tThe output displays the hostname, IP address and the listening port of all the peers the process is connected to.\n"
					"\tThis also includes the server details.\n"
					"7) TERMINATE <connection id>:\n"
					"\tThis command will terminate the connection listed under the specified number when LIST is used to display all "
					"connections.\n"
					"\te.g., TERMINATE 2 should terminate the connection which has the connection id 2.\n"
					"8) EXIT: Close all connections and terminate the process.\n"
					"9) UPLOAD <connection id> <file name>:\n"
					"\tFor example, ‘UPLOAD 3 /local/Spring_2014/dimitrio/a.txt’ will upload the file a.txt\n"
					"\twhich is located in /local/Spring_2014/dimitrio/, to the host on the connection that has the connection id 3.\n"
					"10) DOWNLOAD <connection id 1> <file1> <connection id 2> <file2> <connection id 3> <file3>:\n"
					"\tThis will download a file from each host specified in the command.\n"
					"11) CREATOR: Display developers full name, your UBIT name and UB email address.\n");
	fflush(stdout);
} //printHelp ends

/*
 * Function to make entry into the connection list
 */
void makeEntryIntoConnectionList(int fd, int hostTyp, string& hostname,
		string& ipAdd, string& portNo) {
	struct conn entry;
	entry.id = connlMaxId++;
	entry.sockFd = fd;
	//Host type 1 indicates server, 0 indicates client
	entry.hostType = hostTyp;
	entry.host = hostname;
	entry.ip = ipAdd;
	entry.port = portNo;
	connectionList.push_back(entry);
} //End makeEntryIntoConnectionList

/**
 * Get sockaddr, IPv4 or IPv6:
 */
void *get_in_addr(struct sockaddr *sa) {
	if (sa->sa_family == AF_INET) {
		return &(((struct sockaddr_in*) sa)->sin_addr);
	}
	return &(((struct sockaddr_in6*) sa)->sin6_addr);
} //End get_in_addr

/**
 * Function to get the port number from a socket file descriptor.
 * Needed as we do not know what port the OS has assigned when we bind & connect without assigning a port number.
 */
int getPortNoFromSockFd(int fd) {
	struct sockaddr_in sin;
	socklen_t len = sizeof(sin);
	if (getsockname(fd, (struct sockaddr *) &sin, &len) == -1)
		perror("getsockname");
	else
		printf("port number %d\n", ntohs(sin.sin_port));
	return ntohs(sin.sin_port);
}

/**
 * Send reject response to client
 */
int sendRejectToClient(int s) {
	//Send response to other client
	string dataToSend =
			"You tried registering to a client machine. You can only register with the server.\n";
	//Length of data to be sent
	int length = 2 * sizeof(unsigned int) + dataToSend.length() + 1;
	char* buf = (char*) malloc(length);
	memset(buf, 0, length);
	unsigned int command = htonl(commandIdMap.find("REGISTERREJECT")->second);
	memcpy(buf, &command, sizeof(unsigned int));
	unsigned int dataLength = htonl(dataToSend.length() + 1);
	memcpy(buf + sizeof(unsigned int), &dataLength, sizeof(unsigned int));
	memcpy(buf + 2 * sizeof(unsigned int), dataToSend.c_str(),
			dataToSend.length());

	if (sendall(s, buf, &length) == -1) {
		perror("Error occurred while sending data: sendall()\n");
		printf("We only sent %d bytes because of the error!\n", length);
	}

	if (s == fdmax) {
		fdmax--;
	}
	FD_CLR(s, &master);
	free(buf);
	close(s);
	return 1;
}

/**
 * This function is used to validate the existence of a file and verify if the provided file is indeed a file and not a directory
 */
int validateFileExistence(string& filename) {

	//Validate the provided filename if it exists
	ifstream ifile;
	ifile.open(filename.c_str(), ifstream::binary);
	if (!ifile.is_open()) {
		fprintf(stderr,
				"The provided file name and path is invalid and not present on this client.\n");
		fflush(stderr);
		return -1;
	}
	ifile.close();

	//Validate if its a file or directory
	struct stat fileStat;

	int status = stat(filename.c_str(), &fileStat);
	if (status != 0) {
		fprintf(stderr, "Error faced while getting file characteristics.\n");
		fflush(stderr);
		return -1;
	}

	if (S_ISDIR(fileStat.st_mode)) {
		fprintf(stderr, "%s is a directory. Please provide a file to upload\n",
				filename.c_str());
		fflush(stderr);
		return -1;
	}

	if (S_ISREG(fileStat.st_mode)) {
		return 1;
	} else {
		return -1;
	}
}

/**
 * Function that sends an UPLOADREJECT message to the remote host that initiated a download request
 */
int sendUploadRejectMessage(int sockfd) {
	//Send response to other client
	//Length of data to be sent
	int length = 2 * sizeof(unsigned int);
	char* buf = (char*) malloc(length);
	memset(buf, 0, length);
	unsigned int command = htonl(commandIdMap.find("UPLOADREJECT")->second);
	memcpy(buf, &command, sizeof(unsigned int));
	unsigned int dataLength = htonl(0);
	memcpy(buf + sizeof(unsigned int), &dataLength, sizeof(unsigned int));

	if (sendall(sockfd, buf, &length) == -1) {
		perror("Error occurred while sending data: sendall()\n");
		printf("We only sent %d bytes because of the error!\n", length);
		free(buf);
		return -1;
	}

	free(buf);
	return 1;
}


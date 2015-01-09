/**
 * srao2_server.cpp :
 * Single file to handle the routing protocol.
 * Starts off as a server on a given port.
 * Created for CSE 589 Spring 2014 Programming Assignment 3.
 * @author Sudarshan Rao
 * @created 17 April 2014
 */

#include <getopt.h>
#include <stdio.h>
#include <stdlib.h>
#include <cerrno>
#include <deque>
#include <string>
#include <string.h>
#include <fstream>
#include <sstream>
#include <arpa/inet.h>
#include <netdb.h>
#include <netinet/in.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <iostream>
#include <unistd.h>
#include <netdb.h>
#include <map>
#include <sys/stat.h>
#include <sys/time.h>
#include <list>

//File descriptor for standard input
#define STDIN 0

using namespace std;

//Function definitions begin
void printUsage(void);
void readTopologyFile(void);
void startServer(void);
void trim(string& s);
int recvAll(int s);
int validateAndProcessUserCommands(const string& userCommand);
int processStepCommand(void);
int processPacketsCommand(void);
int processCrashCommand(void);
int processDisplayCommand(void);
int processUpdateCommand(const string& serverId, const string& neighborId,
		const string& cost);
int processDisableCommand(const string& serverId);
char* generateDistanceVectorMessage(int* len);
int sendall(int s, char* dataToSend, int* len, const string& destIp,
		const string& destPort);
void printRoutingTable(void);
void printDistanceVectorMap(void);
void printMyDistanceVectorMap(void);
void calculateBellmanFordAlgo(void);
void insertNewSendTimeoutInList(void);
void updateReceiveTimeoutForNeighbor(const string& remoteId);
void populateTimeoutList(void);
void printTimeoutList(void);
void sendDistanceVectorToNeighbors(void);
//Function definitions end

//Global variables list starts
//A single row of the routing table
struct routingTabRow {

	//Id of the server that we get from the topology file
	string serverId;

	//Cost from server to this neighbor. Set to 0 for self & inf if not reachable
	string cost;

	//IP address of this server that we get from the topology file
	string ipAdd;

	//Port number of the server that we get from the topology file
	string port;

	//Variable to verify if this server is a neighbor, set to true for self
	bool isNeighbour;

	//Variable to know which next hop is
	string nextHopId;

	//Variable to set the time for next timeout. Type of event will be determined by server id.
	//If it is self id it will be to send else set link cost to infinity for neighbor
	timeval nextTimeout;

	//Variable to denote if this neighbor has been disabled
	bool isDisabled;
};

//Double ended Queue used to represent a routing table
deque<routingTabRow> routingTable;

//Map used to store the distance vector
//Used map instead of 2D array for faster access and update speed
//Outer Map stores all server IP address and their neighbor distance vectors
//Inner map has server IPs and their distance vectors
map<string, map<string, routingTabRow> > distanceVectorMap;

//Distance vector for this host
map<string, routingTabRow> myDistanceVector;

//List that maintains in ascending order the timeout events that are to occur on this host
list<routingTabRow> timeoutList;

//Variable to hold the number of distance vector packets this server has received
int noPackets = 0;

//Variable to hold large value for infinite cost
int inf = 30000;

//Variable to hold the host IP address
string hostIP;

//Variable to hold the host port number
string hostPort;

//Variable to hold the host server id number
string hostServerId;

//Variable to hold the path of the topology file
string topologyFilePath = "";

//Variable to hold the time interval
int timeInterval = 0;

//Variable to hold the number of servers
int noServers;

//Variable to hold the number of neighbors
int noNeighbours;

//Master file descriptor list
fd_set master;

//Temporary file descriptor list for select()
fd_set read_fds;

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
 */
int main(int argc, char* argv[]) {

	//Validate the inputs provided by the user
	//Variable used to assist validation of number of parameters provided by user
	int c, count = 0, tcount = 0, icount = 0;
	char * pEnd;

	//Get the arguments provided by the user
	while ((c = getopt(argc, argv, "t:i:")) != -1) {
		//If more than 2 arguments provided exit
		if (++count > 2) {
			printUsage();
			exit(EXIT_FAILURE);
		}
		switch (c) {
		case 't':
			//If more than 1 topology file provided exit else get the topology file path
			if (++tcount > 1) {
				printUsage();
				exit(EXIT_FAILURE);
			}
			topologyFilePath = optarg;
			break;
		case 'i':
			//If more than 1 interval provided exit else get the time interval
			if (++icount > 1) {
				printUsage();
				exit(EXIT_FAILURE);
			}
			timeInterval = strtol(optarg, &pEnd, 10);
			break;
		default:
			printUsage();
			exit(EXIT_FAILURE);
		}
	}

	//If time interval or topology file name is not set exit
	if (timeInterval <= 0 || errno == ERANGE
			|| strcmp(topologyFilePath.c_str(), "") == 0) {
		fprintf(stderr,
				"Please enter time interval as a number greater than 0 and a non blank topology file path.\n");
		printUsage();
		exit(EXIT_FAILURE);
	}

	//Read the topology file
	readTopologyFile();

	//Start the server
	startServer();
}

/**
 * Function that starts the server and handles the user commands
 */
void startServer() {

	//Clear the master and temporary file descriptor lists
	FD_ZERO(&master);
	FD_ZERO(&read_fds);

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
	hints.ai_socktype = SOCK_DGRAM;
	hints.ai_flags = AI_PASSIVE;

	//Return value variable to check status of getaddrinfo call.
	int rv;

	//Call to getaddrinfo to get network information of the host.
	if ((rv = getaddrinfo(NULL, hostPort.c_str(), &hints, &ai)) != 0) {
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

	//Add STDIN and the listener to the master set
	FD_SET(STDIN, &master);
	FD_SET(listener, &master);

	//Keep track of the biggest file descriptor so far, it's this one
	fdmax = listener;

	//Set the timeout events for this server
	populateTimeoutList();

	//Continuously check for activity on this server
	while (1) {

		//Copy master file descriptor set into read file descriptors set
		read_fds = master;

		struct timeval tv, res;
		gettimeofday(&tv, NULL);

		//If the event has already gone behind the current time set time interval for select = 0
		if (timercmp(&tv, &timeoutList.front().nextTimeout, >=)) {
			res.tv_sec = 0;
			res.tv_sec = 0;
		} else {
			timersub(&timeoutList.front().nextTimeout, &tv, &res);
		}

		//Make the select call which blocks
		//Time out of the select event will be 1st element of the timeout list as we keep it in ascending order
		if (select(fdmax + 1, &read_fds, NULL, NULL, &res) == -1) {
			perror(
					"Problem faced in the select function call: select\nExiting.\n");
			exit(EXIT_FAILURE);
		}

		// Run through the existing connections looking for data to read
		for (int i = 0; i <= fdmax; i++) {

			//Check if we got one new set file descriptor
			if (FD_ISSET(i, &read_fds)) {

				//Check if new packet received
				if (i == listener) {
					if (recvAll(i) == -1) {
						perror(
								"Error occurred while receiving data: recvAll()");
					}
				}

				//Check if user entered a command
				else if (i == STDIN) {
					getline(cin, userCommand);
					trim(userCommand);
					if (strlen(userCommand.c_str())) {
						/*cout << "Command entered by user is: "
						 << userCommand.c_str() << endl;*/
						validateAndProcessUserCommands(userCommand);
					}
				}

			}	//Checking type of fd for loop ends

		}	//Checking fdset for loop ends

		//Now check if any timeout event has occurred for send or receive
		//Get the current time and compare with 1st element of the list
		struct timeval curTim;
		gettimeofday(&curTim, NULL);

		//If current time is greater than the set time in the front of the timeout list
		while (timercmp(&curTim, &timeoutList.front().nextTimeout, >=)) {

			if (!strcmp(timeoutList.front().ipAdd.c_str(), hostIP.c_str())) {

				//Send my distance vector to my neighbors
				sendDistanceVectorToNeighbors();

				//Once sending is done we need to set the new timeout for next send event
				insertNewSendTimeoutInList();

			}

			//Else we know it is a receive timeout condition in which case
			//we set the link cost for that specific neighbor to infinity
			else {

				//Assume link died so set link cost for this neighbor to infinity
				for (deque<routingTabRow>::iterator it = routingTable.begin();
						it != routingTable.end(); ++it) {
					if (!strcmp(timeoutList.front().ipAdd.c_str(),
							it->ipAdd.c_str())) {
						it->cost = "30000";
						break;
					}
				}

				//Recalculate distance vectors using bellman ford
				calculateBellmanFordAlgo();

				//Remove entry for this neighbor from the event timeout list
				timeoutList.pop_front();
			}
		}

	}	//infinite while ends

}	//startServer ends

/**
 *
 */
void sendDistanceVectorToNeighbors() {

	//Data and length of data to be sent
	int len;
	char *data = generateDistanceVectorMessage(&len);

	//Send the routing updates to all the peers
	for (deque<routingTabRow>::iterator it = routingTable.begin();
			it != routingTable.end(); ++it) {

		//Send routing updates only to neighbors & not to self
		if (it->isNeighbour && !it->isDisabled) {
			if (sendall(listener, data, &len, it->ipAdd, it->port) == -1) {
				perror("Error occurred while sending data: sendall()\n");
				printf("We only sent %d bytes because of the error!\n", len);
			}
		}
	}
}

/**
 * Function to populate the timeout events for send and receive
 */
void populateTimeoutList() {

	//Populate the timeout event list for sending and receiving data
	for (deque<routingTabRow>::iterator it = routingTable.begin();
			it != routingTable.end(); ++it) {

		//If neighbor or self only then we need to set timeout condition
		if (it->isNeighbour || !strcmp(it->cost.c_str(), "0")) {
			//Copy the row
			struct routingTabRow rtbrw;
			rtbrw = *it;
			//Set send timeout condition for send event from this host
			if (!strcmp(rtbrw.ipAdd.c_str(), hostIP.c_str())) {
				gettimeofday(&rtbrw.nextTimeout, NULL);
				rtbrw.nextTimeout.tv_sec += timeInterval;
				rtbrw.nextTimeout.tv_usec += 0;
			}
			//Else set the timeout for receive event from the neighbors
			else {
				gettimeofday(&rtbrw.nextTimeout, NULL);
				rtbrw.nextTimeout.tv_sec += (3 * timeInterval);
				rtbrw.nextTimeout.tv_usec += 0;
			}

			timeoutList.push_back(rtbrw);
		}
	}

	/*fprintf(stdout, "\nInitial timeout list\n");
	 printTimeoutList();*/
}

/**
 * Function to insert new timeout for a condition in the list
 */
void insertNewSendTimeoutInList() {

	/*fprintf(stdout, "Timeout list before send update\n");
	 printTimeoutList();*/

	//Get copy of the element for which timeout event has finished
	struct routingTabRow tm = timeoutList.front();
	//Remove that element from the list
	timeoutList.pop_front();

	//Set new timeout time in it
	struct timeval tv;
	gettimeofday(&tv, NULL);
	tv.tv_sec += timeInterval;
	tv.tv_usec += 0;
	tm.nextTimeout = tv;

	bool isInserted = false;

	//Now insert the new time interval in its correct position in the sorted time list
	for (list<routingTabRow>::iterator it = timeoutList.begin();
			it != timeoutList.end(); ++it) {
		if (timercmp(&tv, &it->nextTimeout, <=)) {
			isInserted = true;
			timeoutList.insert(it, tm);
			break;
		}
	}
	if (!isInserted) {
		timeoutList.push_back(tm);
	}

	/*fprintf(stdout, "Timeout list after send update\n");
	 printTimeoutList();*/
}

/**
 * Function to handle partial sends
 */
int sendall(int s, char* dataToSend, int* len, const string& destIp,
		const string& destPort) {

	//variable which checks how many bytes we've sent
	int total = 0;

	//variable which checks how many we have left to send
	int bytesleft = *len;

	//Variable to keep track of number of bytes sent per call of sendto
	int n;

	struct sockaddr_in soin;
	soin.sin_family = AF_INET;
	soin.sin_port = htons(atoi(destPort.c_str()));
	soin.sin_addr.s_addr = inet_addr(destIp.c_str());
	memset(soin.sin_zero, '\0', sizeof soin);

	while (total < *len) {
		n = sendto(s, dataToSend + total, bytesleft, 0,
				(struct sockaddr *) &soin, sizeof(soin));
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
 * Function to handle partial recvs
 */
int recvAll(int s) {

	//Buffer for receiving data
	char headBuf[2];

	//Length first set to 2 bytes to get the length of data to be received
	int len = 2;

	//variable which checks how many bytes we've received
	int total = 0;

	//variable which checks how many we have left to receive
	int bytesleft = len;

	//Variable to hold number of bytes received in each recv call
	int n;

	//The structure required for recvfrom
	struct sockaddr addr;

	//The length of the sockaddr structure
	socklen_t addr_len = sizeof addr;

	//Receive header data first of 2 bytes
	while (total < len) {
		if ((n = recvfrom(s, headBuf + total, bytesleft, MSG_PEEK, &addr,
				&addr_len)) <= 0) {
			//This implies that the remote host has terminated its process
			fprintf(stderr, "The remote host has terminated the connection.\n");
			fflush(stderr);
			return -1;
		} else {
			total += n;
			bytesleft -= n;
		}
	}

	//Based on the first two bytes received we can determine the length of the remaining data
	uint16_t * tmp = (uint16_t *) headBuf;
	uint16_t noUpdates = ntohs(*tmp);
	int dataLen = 8 + (12 * noUpdates);

	bytesleft = dataLen;
	len = bytesleft;
	total = 0;
	n = 0;

	//Buffer to hold the remaining data sent
	char* dataBuf = (char*) malloc(dataLen * sizeof(char));
	memset(dataBuf, 0, dataLen);
	char* dataBufo = dataBuf;

	//Second call to receive to get the actual data
	while (total < len) {
		if ((n = recvfrom(s, dataBufo + total, bytesleft, 0, &addr, &addr_len))
				<= 0) {
			//This implies that the remote host has terminated its process
			fprintf(stderr, "The remote host has terminated the connection.\n");
			fflush(stderr);
			return -1;
		} else {
			total += n;
			bytesleft -= n;
		}
	}

	int recvCnt = 2;

	//Get the port number of the host which sent this packet
	uint16_t prtN;
	memcpy(&prtN, dataBufo + recvCnt, 2);
	recvCnt += 2;
	prtN = ntohs(prtN);
	stringstream ss;
	ss << prtN;
	string recPrt(ss.str());

	//Get the IP address of the host which sent this packet
	uint32_t ipInt;
	memcpy(&ipInt, dataBufo + recvCnt, 4);
	recvCnt += 4;
	ipInt = ntohl(ipInt);
	char ip4[INET_ADDRSTRLEN];
	inet_ntop(AF_INET, &ipInt, ip4, INET_ADDRSTRLEN);

	//Get the id of the remote host from which update message received
	string remoteId;
	for (deque<routingTabRow>::iterator it = routingTable.begin();
			it != routingTable.end(); ++it) {
		if (!strcmp(ip4, it->ipAdd.c_str())) {
			remoteId = it->serverId;
		}
	}

	//Check in server list if this id has been disabled
	for (deque<routingTabRow>::iterator it = routingTable.begin();
			it != routingTable.end(); ++it) {
		if (!strcmp(it->serverId.c_str(), remoteId.c_str())) {
			if (it->isDisabled) {
				return 1;
			}
		}
	}

	//Display the id of the remote server which sent the update message
	fprintf(stdout, "\nRECEIVED A MESSAGE FROM SERVER %s\n", remoteId.c_str());
	fflush(stdout);

	//Increase the count of received packets
	noPackets++;

	//Now build a map of the distance vector received from the remote host
	map<string, routingTabRow> dvMap;
	for (int i = 0; i < noUpdates; i++) {

		struct routingTabRow rtb;
		char remIp[INET_ADDRSTRLEN];

		//Get the ip address and set in struct
		uint32_t remIpTmp;
		memcpy(&remIpTmp, dataBufo + recvCnt, 4);
		recvCnt += 4;
		remIpTmp = ntohl(remIpTmp);
		inet_ntop(AF_INET, &remIpTmp, remIp, INET_ADDRSTRLEN);
		rtb.ipAdd = string(remIp);

		//Get the port number and set in struct
		uint16_t rmPrt;
		memcpy(&rmPrt, dataBufo + recvCnt, 2);
		recvCnt += 4;
		rmPrt = ntohs(rmPrt);
		stringstream ss;
		ss << rmPrt;
		rtb.port = ss.str();

		//Get the server id and set in struct
		uint16_t sId;
		memcpy(&sId, dataBufo + recvCnt, 2);
		recvCnt += 2;
		sId = ntohs(sId);
		stringstream sss;
		sss << sId;
		rtb.serverId = sss.str();

		//Get the cost and set in struct
		uint16_t cst;
		memcpy(&cst, dataBufo + recvCnt, 2);
		recvCnt += 2;
		cst = ntohs(cst);
		stringstream ssss;
		ssss << cst;
		rtb.cost = ssss.str();

		rtb.isNeighbour = false;
		rtb.nextHopId = "0";

		dvMap[remIp] = rtb;

	}

	//Replace the routing vector of the server from which this distance vector came
	distanceVectorMap[ip4] = dvMap;

	fprintf(stdout, "Distance vector map after receive\n");
	printDistanceVectorMap();

	//Now call the bellman ford algorithm and update self distance vector
	calculateBellmanFordAlgo();

	//Once the receive has been completed we need to update the timeout for this neighbor in the timeoutList
	updateReceiveTimeoutForNeighbor(remoteId);

	return 1;
}

/**
 * Function that updates the receive timeout event for a neighbor on receipt of routing vector from it
 */
void updateReceiveTimeoutForNeighbor(const string& remoteId) {

	/*fprintf(stdout, "\nTimeout list before receive update\n");
	 printTimeoutList();*/

	bool isExist = false;

	//Get the neighbor for whom the receive has finished and remove it from the timeout list
	struct routingTabRow rt;
	for (list<routingTabRow>::iterator it = timeoutList.begin();
			it != timeoutList.end(); it++) {
		if (!strcmp(it->serverId.c_str(), remoteId.c_str())) {
			rt = *it;
			timeoutList.erase(it);
			isExist = true;
			break;
		}
	}

	//If it was removed earlier due to timeout and now is being added again
	if (!isExist) {
		for (deque<routingTabRow>::iterator it = routingTable.begin();
				it != routingTable.end(); ++it) {
			if (!strcmp(it->serverId.c_str(), remoteId.c_str())) {
				rt = *it;
				break;
			}
		}
	}

	//Set new timeout time in it
	struct timeval tv;
	gettimeofday(&tv, NULL);
	tv.tv_sec += (3 * timeInterval);
	tv.tv_usec += 0;
	rt.nextTimeout = tv;

	bool isInserted = false;

	//Now insert the new time interval in its correct position in the sorted time list
	for (list<routingTabRow>::iterator it = timeoutList.begin();
			it != timeoutList.end(); ++it) {
		if (timercmp(&tv, &it->nextTimeout, <=)) {
			isInserted = true;
			timeoutList.insert(it, rt);
			break;
		}
	}

	if (!isInserted) {
		timeoutList.push_back(rt);
	}

	/*fprintf(stdout, "\nTimeout list after receive update\n");
	 printTimeoutList();*/

}

/**
 * Function that updates the distance vectors using Bellman Ford Algorithm
 */
void calculateBellmanFordAlgo() {

	/*fprintf(stdout, "Local DV map before bellman ford\n");
	 printMyDistanceVectorMap();
	 fprintf(stdout, "Routing table before bellman ford\n");
	 printRoutingTable();*/

	int neighborCost;
	string nxtHp;

	//Need to update my routing vector now based on what I just received
	for (deque<routingTabRow>::iterator rit = routingTable.begin();
			rit != routingTable.end(); ++rit) {

		//Ignore self for calculation as cost to self is 0
		if (strcmp(rit->ipAdd.c_str(), hostIP.c_str())) {

			//Get current cost and next hop id of that node from self
			neighborCost = atoi(rit->cost.c_str());
			nxtHp = rit->nextHopId;

			//Now check remaining neighbors for indirect paths
			for (deque<routingTabRow>::iterator nit = routingTable.begin();
					nit != routingTable.end(); ++nit) {

				//Ignore the neighbor for whom we are doing this as well as the host as cost to self is 0
				if (nit->isNeighbour && !nit->isDisabled
						&& strcmp(nit->ipAdd.c_str(), rit->ipAdd.c_str())) {

					//Check if this neighbor is present in the distance vector map
					if (distanceVectorMap.count(nit->ipAdd) > 0) {

						map<string, map<string, routingTabRow> >::iterator dit =
								distanceVectorMap.find(nit->ipAdd);

						//Calculate the distance if present
						if (neighborCost
								> (atoi(nit->cost.c_str())
										+ atoi(
												dit->second.find(rit->ipAdd)->second.cost.c_str()))) {

							//If cost to neighbor is greater than this then this is the minimum distance
							neighborCost =
									atoi(nit->cost.c_str())
											+ atoi(
													dit->second.find(rit->ipAdd)->second.cost.c_str());

							//This is the next hop
							nxtHp = nit->serverId;
						}
					}

				}
			}	//Inner for loop for neighbor

			//Now we have checked all neighbors and have the minimum cost and next hop this host
			//So update the local distance vector map for that host
			map<string, routingTabRow>::iterator ldvit = myDistanceVector.find(
					rit->ipAdd);

			//Update the link to cost to this neighbor and also the next hop to be taken
			stringstream ss;
			ss << neighborCost;
			ldvit->second.cost = ss.str();
			ldvit->second.nextHopId = nxtHp;

			//Update the next hop in the routing table
			rit->nextHopId = nxtHp;

		}	//End self check if block

	}	//End of main for

	/*fprintf(stdout, "Local DV map after bellman ford\n");
	 printMyDistanceVectorMap();
	 fprintf(stdout, "Routing table after bellman ford\n");
	 printRoutingTable();*/

}

/**
 * This function takes appropriate actions based on the provided user commands
 */
int validateAndProcessUserCommands(const string& userCommand) {

	//Check if user provided step command
	if (!strcasecmp(userCommand.c_str(), "step")) {
		if (processStepCommand()) {
			fprintf(stdout, "\n%s SUCCESS\n", userCommand.c_str());
			fflush(stdout);
		} else {
			fprintf(stderr,
					"\n%s failed as could not send the distance vector to the neighbors.\n",
					userCommand.c_str());
			fflush(stderr);
		}
	}

	//Check if user provided packets command
	else if (!strcasecmp(userCommand.c_str(), "packets")) {
		if (processPacketsCommand()) {
			fprintf(stdout, "\n%s SUCCESS\n", userCommand.c_str());
			fflush(stdout);
		} else {
			fprintf(stderr,
					"\n%s failed as could not get the number of packets.\n",
					userCommand.c_str());
			fflush(stderr);
		}
	}

	//Check if user provided crash command
	else if (!strcasecmp(userCommand.c_str(), "crash")) {
		fprintf(stdout, "\n%s SUCCESS\n", userCommand.c_str());
		fflush(stdout);
		processCrashCommand();
	}

	//Check if user provided display command
	else if (!strcasecmp(userCommand.c_str(), "display")) {
		if (processDisplayCommand()) {
			fprintf(stdout, "\n%s SUCCESS\n", userCommand.c_str());
			fflush(stdout);
		} else {
			fprintf(stderr, "\n%s failed as could not get the routing table.\n",
					userCommand.c_str());
			fflush(stderr);
		}
	}

	//Split the user command string and check if disable or update
	else {

		istringstream iss(userCommand);
		string token;
		getline(iss, token, ' ');

		//If command is disable or update proceed
		if (!strcasecmp(token.c_str(), "disable")
				|| !strcasecmp(token.c_str(), "update")) {

			//If command is disable check if server id provided is correct
			if (!strcasecmp(token.c_str(), "disable")) {

				if (getline(iss, token, ' ')) {

					string serverId = token;
					char * pEnd;

					//If more than 1 arguments provided to disable give error message
					if (getline(iss, token, ' ')) {
						fprintf(stderr,
								"Invalid number of arguments provided to disable command.\n");
						fflush(stderr);
						return 0;
					}

					//Check if provided server id is a proper number
					if (strtol(token.c_str(), &pEnd, 10) == 0 || errno == ERANGE) {
						errno = 0;
						fprintf(stderr,
								"Please provide a valid number as argument to the disable command.\n");
						fflush(stderr);
						return 0;
					}

					bool isValid = false;
					//Check if provided server id is a neighbor
					for (deque<routingTabRow>::iterator it =
							routingTable.begin(); it != routingTable.end();
							++it) {
						if (!strcmp(it->serverId.c_str(), serverId.c_str())
								&& it->isNeighbour) {
							isValid = true;
							break;
						}
					}

					//If server id is not a neighbor
					if (!isValid) {
						fprintf(stderr,
								"You can only disable your neighbors using the disable command.\n");
						fflush(stderr);
						return 0;
					}

					//If provided server id is valid
					else {
						if (processDisableCommand(serverId)) {
							fprintf(stdout, "\n%s SUCCESS\n",
									userCommand.c_str());
							fflush(stdout);
						} else {
							fprintf(stderr,
									"\n%s failed as could not disable the neighbor.\n",
									userCommand.c_str());
							fflush(stderr);
						}

					}
				}

				//Print error message as server id not provided
				else {
					fprintf(stderr,
							"Please provide server id to disable command.\n");
					fflush(stderr);
					return 0;
				}
			}

			//If command is update check if server ids & cost provided is correct
			if (!strcasecmp(token.c_str(), "update")) {

				string argus[3];
				int count = 0;

				//First validate the number of arguments
				while (getline(iss, token, ' ')) {
					if (count == 4) {
						break;
					}
					argus[count++] = token;
				}

				//If 3 arguments are not provided show error message
				if (count != 3) {
					fprintf(stderr,
							"Please provide correct number of arguments to update command.\n");
					fflush(stderr);
					return 0;
				}

				char * pEnd;
				//Then validate if the arguments provided are numbers/inf
				for (int i = 0; i < 2; i++) {
					if (strtol(argus[i].c_str(), &pEnd, 10)
							== 0|| errno == ERANGE) {
						errno = 0;
						fprintf(stderr,
								"Please provide a valid number as server id argument to the update command.\n");
						fflush(stderr);
						return 0;
					}
				}

				if (strcasecmp(argus[2].c_str(), "inf")
						&& (strtol(argus[2].c_str(), &pEnd, 10) == 0
								|| errno == ERANGE)) {
					errno = 0;
					fprintf(stderr,
							"Please provide a valid number or inf as cost argument to the update command.\n");
					fflush(stderr);
					return 0;
				}

				//Then validate if 1st argument is host server id
				if (strcasecmp(hostServerId.c_str(), argus[0].c_str())) {
					fprintf(stderr,
							"Please provide the server id as 1st argument to the update command.\n");
					fflush(stderr);
					return 0;
				}

				//Then validate if 2nd argument is a valid neighbor server id
				bool isValid = false;
				for (deque<routingTabRow>::iterator it = routingTable.begin();
						it != routingTable.end(); ++it) {
					if (!strcmp(it->serverId.c_str(), argus[1].c_str())
							&& it->isNeighbour && !it->isDisabled) {
						isValid = true;
						break;
					}
				}

				//If server id is not a neighbor
				if (!isValid) {
					fprintf(stderr,
							"You can only update costs to your non disabled neighbors using the update command.\n");
					fflush(stderr);
					return 0;
				}

				//If provided server id is valid
				else {
					if (processUpdateCommand(argus[0], argus[1], argus[2])) {
						fprintf(stdout, "\n%s SUCCESS\n", userCommand.c_str());
						fflush(stdout);
					} else {
						fprintf(stderr,
								"\n%s failed as could not update the link cost.\n",
								userCommand.c_str());
						fflush(stderr);
					}
				}
			}
		}

		//User entered incorrect command
		else {
			fprintf(stderr, "Incorrect command entered.\n");
			fflush(stderr);
			return 0;
		}
	}

	return 1;
}	//processUserCommands ends

/**
 * Function that sends routing updates to all neighbors immediately
 */
int processStepCommand() {

	//Send distance vector immediately to all neighbors
	sendDistanceVectorToNeighbors();

	struct routingTabRow rt;

	//Update the timeout interval for sending in the timeout list
	for (list<routingTabRow>::iterator it = timeoutList.begin();
			it != timeoutList.end(); ++it) {
		if (!strcmp(it->ipAdd.c_str(), hostIP.c_str())) {
			rt = *it;
			timeoutList.erase(it);
			break;
		}
	}

	struct timeval tv;
	gettimeofday(&tv, NULL);
	tv.tv_sec += timeInterval;
	tv.tv_usec += 0;

	rt.nextTimeout = tv;

	bool isInserted = false;

	//Now insert the new time interval in its correct position in the sorted time list
	for (list<routingTabRow>::iterator it = timeoutList.begin();
			it != timeoutList.end(); ++it) {
		if (timercmp(&tv, &it->nextTimeout, <=)) {
			isInserted = true;
			timeoutList.insert(it, rt);
			break;
		}
	}
	if (!isInserted) {
		timeoutList.push_back(rt);
	}

	return 1;
}

/**
 * Function that displays the number of distance vector packets this server
 * has received since the last instance when this information was requested.
 */
int processPacketsCommand() {

	//Display the number of packets received and reset it after displaying
	fprintf(stdout,
			"\nNumber of distance vector packets this server has received since the last instance "
					"when this information was requested = %d.\n", noPackets);
	fflush(stdout);
	noPackets = 0;
	return 1;
}

/**
 * Process to simulate a crash. Closes all connections on all links
 */
int processCrashCommand() {
	while (1) {

	}
	return 1;
}

/**
 * Function that displays the current routing table of this host
 */
int processDisplayCommand() {

	//Sort the map using a copy
	map<int, routingTabRow> displayMap;
	for (map<string, routingTabRow>::iterator it = myDistanceVector.begin();
			it != myDistanceVector.end(); ++it) {
		displayMap[atoi(it->second.serverId.c_str())] = it->second;
	}

	//Display the map
	fprintf(stdout, "\nCurrent routing table:\n");
	fprintf(stdout, "Server ID\tNext Hop ID\tCost\n");
	for (map<int, routingTabRow>::iterator it = displayMap.begin();
			it != displayMap.end(); ++it) {
		if (it->first != atoi(hostServerId.c_str())) {
			fprintf(stdout, "%d\t\t%s\t\t%s\n", it->first,
					!strcmp(it->second.nextHopId.c_str(), "0") ?
							"NA" : it->second.nextHopId.c_str(),
					!strcmp(it->second.cost.c_str(), "30000") ?
							"inf" : it->second.cost.c_str());

		}
	}
	fflush(stdout);

	return 1;
}

/**
 * Function that disables a link to the neighbor
 */
int processDisableCommand(const string& serverId) {

	//Disable the neighbor with the provided id
	for (deque<routingTabRow>::iterator it = routingTable.begin();
			it != routingTable.end(); ++it) {
		if (!strcmp(it->serverId.c_str(), serverId.c_str())) {
			it->isDisabled = true;
			it->cost = "30000";
			break;
		}
	}

	//Remove this id from the timeoutList
	for (list<routingTabRow>::iterator it = timeoutList.begin();
			it != timeoutList.end(); ++it) {
		if (!strcmp(it->serverId.c_str(), serverId.c_str())) {
			timeoutList.erase(it);
			break;
		}
	}

	//Recalculate the distance vectors by calling the bellman ford algorithm
	calculateBellmanFordAlgo();

	return 1;
}

/**
 * Function that updates the link cost between this server and a neighbor
 */
int processUpdateCommand(const string& serverId, const string& neighborId,
		const string& cost) {

	for (deque<routingTabRow>::iterator it = routingTable.begin();
			it != routingTable.end(); ++it) {
		if (!strcmp(it->serverId.c_str(), neighborId.c_str())) {
			if (!strcmp(cost.c_str(), "inf")) {
				it->cost = "30000";
				break;
			} else {
				it->cost = cost;
				break;
			}

		}
	}

	//Recalculate the distance vectors by calling the bellman ford algorithm
	calculateBellmanFordAlgo();

	return 1;
}

/**
 * Function to generate the distance vector message to be sent to all the neighbors
 */
char* generateDistanceVectorMessage(int* len) {

	//Reserve space for buffer based on the packet structure provided in specs
	*len = 8 + (12 * myDistanceVector.size());
	char* sendBuf = (char*) malloc(*len);
	memset(sendBuf, 0, *len);

	int count = 0;

	//Write number of update fields
	uint16_t noUpdateFields = htons(myDistanceVector.size());
	memcpy(sendBuf, &noUpdateFields, sizeof(uint16_t));
	count += sizeof(uint16_t);

	//Write port number
	uint16_t portNoTmp = htons(atoi(hostPort.c_str()));
	memcpy(sendBuf + count, &portNoTmp, sizeof(uint16_t));
	count += sizeof(uint16_t);

	//Write server IP
	struct sockaddr_in sa;
	inet_pton(AF_INET, hostIP.c_str(), &(sa.sin_addr));
	uint32_t tmpIp = htonl(sa.sin_addr.s_addr);
	memcpy(sendBuf + count, &tmpIp, sizeof(uint32_t));
	count += sizeof(uint32_t);

	//Now loop over the routing vector and prepare rest of the packet
	for (map<string, routingTabRow>::iterator it = myDistanceVector.begin();
			it != myDistanceVector.end(); ++it) {

		//Write the IP address of neighbor
		struct sockaddr_in sin;
		inet_pton(AF_INET, it->second.ipAdd.c_str(), &(sin.sin_addr));
		uint32_t tempIp = htonl(sin.sin_addr.s_addr);
		memcpy(sendBuf + count, &tempIp, sizeof(uint32_t));
		count += sizeof(uint32_t);

		//Write the port of neighbor
		uint16_t prt = htons(atoi(it->second.port.c_str()));
		memcpy(sendBuf + count, &prt, sizeof(uint16_t));
		count += sizeof(uint16_t);

		//Write a blank field as provided by specs
		uint16_t blank = htons(0);
		memcpy(sendBuf + count, &blank, sizeof(uint16_t));
		count += sizeof(uint16_t);

		//Write the server id of the neighbor
		uint16_t servId = htons(atoi(it->second.serverId.c_str()));
		memcpy(sendBuf + count, &servId, sizeof(uint16_t));
		count += sizeof(uint16_t);

		//Write the cost of link to the neighbor
		uint16_t cst = htons(atoi(it->second.cost.c_str()));
		memcpy(sendBuf + count, &cst, sizeof(uint16_t));
		count += sizeof(uint16_t);
	}

	return sendBuf;
}

/**
 * Function to print usage of program on the console to the user.
 */
void printUsage(void) {
	fprintf(stderr,
			"Usage: ./server -t <topology-file-name> -i <routing-update-interval>\n"
					"Sample usage: ./server -t timberlake_init.txt -i 10\n"
					"Exiting.\n");
	fflush(stderr);
} //printUsage ends

/**
 * This function reads the topology file to construct the initial routing table
 */
void readTopologyFile() {

	//Check if file or directory, if directory show error message
	struct stat st;
	lstat(topologyFilePath.c_str(), &st);
	if (S_ISDIR(st.st_mode)) {
		fprintf(stderr,
				"The provided file name and path is a directory. Please provide a topology file.\n");
		printUsage();
		exit(EXIT_FAILURE);
	}

	//Open the file in read mode
	ifstream ifile;
	ifile.open(topologyFilePath.c_str(), ifstream::binary);

	//Check if the file exists else exit
	if (!ifile.is_open()) {
		fprintf(stderr,
				"The provided file name and path is invalid and not present on this client.\n");
		printUsage();
		exit(EXIT_FAILURE);
	}

	//Read from the file and populate the routing table
	string line;

	//Get the number of servers
	getline(ifile, line);
	noServers = atoi(line.c_str());

	//Get the number of neighbors
	getline(ifile, line);
	noNeighbours = atoi(line.c_str());

	//Add each server details to the routing vector table
	int tmp = 0;
	string token;

	//Get the initial server details as provided in the topology file
	while (++tmp <= noServers) {
		getline(ifile, line);
		struct routingTabRow s;
		istringstream iss(line);
		getline(iss, token, ' ');
		s.serverId = token;
		getline(iss, token, ' ');
		s.ipAdd = token;
		getline(iss, token, ' ');
		s.port = token;
		s.cost = "30000";
		s.isNeighbour = false;
		s.nextHopId = "0";
		s.isDisabled = false;
		routingTable.push_back(s);
	}

	//Get the server IP address, port number, serverId, neighbors and their costs
	tmp = 0;
	string myId;
	bool isSelfSet = false;
	while (++tmp <= noNeighbours) {
		getline(ifile, line);
		istringstream iss(line);
		getline(iss, token, ' ');
		myId = token;
		getline(iss, token, ' ');
		for (deque<routingTabRow>::iterator it = routingTable.begin();
				it != routingTable.end(); ++it) {
			if (!strcmp(myId.c_str(), it->serverId.c_str()) && !isSelfSet) {
				isSelfSet = true;
				hostIP = it->ipAdd;
				hostPort = it->port;
				hostServerId = it->serverId;
				it->isNeighbour = false;
				it->cost = "0";
			}
			if (!strcmp(token.c_str(), it->serverId.c_str())) {
				it->isNeighbour = true;
				getline(iss, token, ' ');
				it->cost = token;
				it->nextHopId = it->serverId;
			}
		}
	}

	//Populate my local distance vector
	for (deque<routingTabRow>::iterator it = routingTable.begin();
			it != routingTable.end(); ++it) {
		myDistanceVector[it->ipAdd] = *it;
	}

	/*fprintf(stdout, "\n My routing table:\n");
	 printRoutingTable();

	 fprintf(stdout, "\n My local distance vector table:\n");
	 printMyDistanceVectorMap();*/
}
//readTopologyFile ends

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
 * Function to print the routing table to stdout
 */
void printRoutingTable() {
//Print the routing table
	for (deque<routingTabRow>::iterator it = routingTable.begin();
			it != routingTable.end(); ++it) {
		fprintf(stdout,
				"Server Id = %s,  cost = %s, ip add = %s, port = %s, is neighbor %d, nextHop %s\n",
				it->serverId.c_str(), it->cost.c_str(), it->ipAdd.c_str(),
				it->port.c_str(), (it->isNeighbour ? 1 : 0),
				it->nextHopId.c_str());
	}
	fprintf(stdout, "\n");
	fflush(stdout);
}

/**
 * Function to print the current distance vector of all neighbors to stdout
 */
void printDistanceVectorMap() {
//Print the distance vector
	for (map<string, map<string, routingTabRow> >::iterator it =
			distanceVectorMap.begin(); it != distanceVectorMap.end(); ++it) {
		fprintf(stdout, "Ip add = %s \n", it->first.c_str());
		for (map<string, routingTabRow>::iterator its = it->second.begin();
				its != it->second.end(); ++its) {
			fprintf(stdout,
					"Server Id = %s,  cost = %s, is neighbor %d, nextHop %s\n",
					its->second.serverId.c_str(), its->second.cost.c_str(),
					(its->second.isNeighbour ? 1 : 0),
					its->second.nextHopId.c_str());
		}
	}
	fprintf(stdout, "\n");
	fflush(stdout);
}

/**
 * Function to print my distance vector map
 */
void printMyDistanceVectorMap() {
	for (map<string, routingTabRow>::iterator it = myDistanceVector.begin();
			it != myDistanceVector.end(); ++it) {
		fprintf(stdout,
				"Server Id = %s,  cost = %s, is neighbor %d, nextHop %s\n",
				it->second.serverId.c_str(), it->second.cost.c_str(),
				(it->second.isNeighbour ? 1 : 0), it->second.nextHopId.c_str());
	}
	fprintf(stdout, "\n");
	fflush(stdout);
}

/**
 * Function to print the timeout list to stdout
 */
void printTimeoutList() {
	//Print the timeout list
	for (list<routingTabRow>::iterator it = timeoutList.begin();
			it != timeoutList.end(); ++it) {
		fprintf(stdout,
				"Server Id = %s,  cost = %s, is neighbor %d, nextHop %s, timeout %ld\n",
				it->serverId.c_str(), it->cost.c_str(),
				(it->isNeighbour ? 1 : 0), it->nextHopId.c_str(),
				it->nextTimeout.tv_sec);
	}
	fprintf(stdout, "\n");
	fflush(stdout);
}

#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <deque>
#include <string.h>

using namespace std;

/* ******************************************************************
 ALTERNATING BIT AND GO-BACK-N NETWORK EMULATOR: VERSION 1.1  J.F.Kurose

   This code should be used for PA2, unidirectional or bidirectional
   data transfer protocols (from A to B. Bidirectional transfer of data
   is for extra credit and is not required).  Network properties:
   - one way network delay averages five time units (longer if there
     are other messages in the channel for GBN), but can be larger
   - packets can be corrupted (either the header or the data portion)
     or lost, according to user-defined probabilities
   - packets will be delivered in the order in which they were sent
     (although some can be lost).
**********************************************************************/

#define BIDIRECTIONAL 0    /* change to 1 if you're doing extra credit */
                           /* and write a routine called B_output */

/* a "msg" is the data unit passed from layer 5 (teachers code) to layer  */
/* 4 (students' code).  It contains the data (characters) to be delivered */
/* to layer 5 via the students transport level protocol entities.         */
struct msg {
  char data[20];
  };

/* a packet is the data unit passed from layer 4 (students code) to layer */
/* 3 (teachers code).  Note the pre-defined packet structure, which all   */
/* students must follow. */
struct pkt {
   int seqnum;
   int acknum;
   int checksum;
   char payload[20];
    };

/********* STUDENTS WRITE THE NEXT SEVEN ROUTINES *********/

//Function declaration list begins

int calculateChecksum(struct pkt pack);
void tolayer3(int AorB,struct pkt packet);
void starttimer(int AorB,float increment);
void tolayer5(int AorB,char *datasent);
void stoptimer(int AorB);

//Function declaration list ends

//Global variable declarations begins

//Buffer to hold the messages sent by the application layer from side A
deque<pkt> buffer;

//Variable that indicates the index till which data from the buffer has been accessed
int bufferIndex;

//Variable to hold the base of the buffer used to enqueue messages
int base;

//Variable to hold the next sequence number of the message to be sent to B from A
int nextSeqNo;

//Variable to hold the window size
int windowSize;

//Variable that assigns the timeout value for
float timeoutA;

//Variable to hold the next expected sequence number at the receiver end B
int expectedSeqNo;

//The packet to be sent to A from B through the unreliable channel
struct pkt packetB;

//Variable to count number of messages sent from application layer of Sender A
int layer5A;

//Variable to count number of packets sent from transport layer of Sender A
int layer3A;

//Variable to count number of packets received at the transport layer of Receiver B
int layer3B;

//Variable to count number of messages received at the application layer of Receiver B
int layer5B;

//Global variable declarations ends


/* called from layer 5, passed the data to be sent to other side */
void A_output(struct msg message) //ram's comment - students can change the return type of the function from struct to pointers if necessary
{
	cout << "A: Message arrived. Count = " << ++layer5A << endl;

	//First check if the queue is full or not
	//If the queue is full exit the program
	if(buffer.size() > 1000) {
		cout << "Buffer overflowed. Exiting the program." << endl;
		exit(EXIT_FAILURE);
	}

	//Then make the packet to send to B through the unreliable channel
	//Variable to hold the packet to be sent to B from A through the unreliable channel
	struct pkt packetA;

	//Copy the message data char array into the packet pay load char array
	memcpy(packetA.payload, message.data, 20);

	//Set the sequence number for the packet incrementally till its window size + 1
	packetA.seqnum = nextSeqNo;

	//Set the acknowledgment number for packetA to 0 permanently as the data transfer is unidirectional
	//and thus we do not have to acknowledge any data that is received from B
	packetA.acknum = 0;

	//Calculate the checksum for the packet and assign to the checksum attribute in the
	//packet so that we can verify at the B side on receipt of this packet if the packet is corrupt or not
	packetA.checksum = calculateChecksum(packetA);

	//Put this packet to the end of the buffer
	buffer.push_back(packetA);

	//Check if the next packet lies within the window range and
	//check if the index of the element being sent is less than the window size and
	//check if the element being sent is not null by ensuring that its index lies within the buffer size
	while(nextSeqNo < base + windowSize && (bufferIndex < (windowSize - 1)) && (bufferIndex < (int32_t)(buffer.size() - 1))) {

		//Send the packet from A to B through the unreliable channel
		tolayer3(0, packetA);

		cout << "A: Sent packet to layer 3. Count =  " << ++layer3A << endl;

		//If the base and the next sequence number is the same then set the timeout for that packet
		if(base == nextSeqNo) {
			starttimer(0, timeoutA);
		}

		//Increment the sequence number variable by one
		nextSeqNo++;

		//Increment the buffer index by one
		bufferIndex++;

	}
}

/**
 * Function used to calculate the checksum for every packet by adding all the values in the packet.
 */
int calculateChecksum(struct pkt pack) {

	//Variable to hold the checksum value
	int checksum = 0;

	//Add the sequence and acknowledgment numbers and assign to checksum
	checksum = pack.acknum + pack.seqnum;

	//Add the characters in the pay load as integers to the checksum
	for(int i = 0; i <20; i++) {
		checksum += pack.payload[i];
	}

	return checksum;
}

void B_output(struct msg message)  /* need be completed only for extra credit */
// ram's comment - students can change the return type of this function from struct to pointers if needed  
{

}

/* called from layer 3, when a packet arrives for layer 4 */
void A_input(struct pkt packet)
{
	//Check if the packet delivered is not corrupt
	if(packet.checksum == calculateChecksum(packet)) {

		cout << "Received acknowledgment from B." << endl;

		//Pop all the entries which have been acknowledged from the front of the buffer and
		//reduce the size of the buffer index by 1 for each pop
		for(int i = 0; i <= packet.acknum - base; i++) {
			buffer.pop_front();
			bufferIndex--;
		}

		//Update the base so that the window can move ahead
		base = packet.acknum + 1;

		//If the base is equal to the next sequence number this means the last packet was acknowledged
		if(base == nextSeqNo) {
			//Stop the timer
			stoptimer(0);
		} else {
			//Restart the timer
			stoptimer(0);
			starttimer(0, timeoutA);
		}

		//As the window base has moved forward we can send new packets
		while(nextSeqNo < base + windowSize && (bufferIndex < (windowSize - 1)) && (bufferIndex < (int32_t)(buffer.size() - 1))) {

			//Send the packet from A to B through the unreliable channel
			tolayer3(0, buffer.at(bufferIndex));

			cout << "A: Sent packet to layer 3. Count =  " << ++layer3A << endl;

			//If the base and the next sequence number is the same then set the timeout for that packet
			if(base == nextSeqNo) {
				starttimer(0, timeoutA);
			}

			//Increment the sequence number variable by one
			nextSeqNo++;

			//Increment the buffer index by one
			bufferIndex++;

		}
	}

}

/* called when A's timer goes off */
void A_timerinterrupt() //ram's comment - changed the return type to void.
{
	//As the timeout occurred we have to retransmit all the packets for which
	//we have not received acknowledgment from B
	//Start the timer
	starttimer(0, timeoutA);

	//Reset the buffer index to from where we need to send the packets again
	bufferIndex = -1;

	//Re send the packets
	for(int i = 0; i < nextSeqNo - base; i++) {
		//As the timeout occurred we have to retransmit the packet again
		tolayer3(0, buffer.at(i));

		cout << "A: Timer interrupt occurred. Re-Sent packet to layer 3. Count =  " << ++layer3A << endl;

		//Increment the buffer index to point to the next element
		bufferIndex++;
	}
}  

/* the following routine will be called once (only) before any other */
/* entity A routines are called. You can use it to do any initialization */
void A_init() //ram's comment - changed the return type to void.
{
	//Set the base and the next sequence number to 1 initially.
	base = nextSeqNo = 1;

	//Set the window size to 10
	windowSize = 10;

	//Set the buffer index to -1 indicating that it is not accessing anything in the queue initially.
	bufferIndex = -1;

	//Set the timeout for packet expire to 15
	timeoutA = 15.0;

	//Initialize all the variables for number of packets counting at A side to 0 initially.
	layer5A = layer3A = 0;
}


/* Note that with simplex transfer from a-to-B, there is no B_output() */

/* called from layer 3, when a packet arrives for layer 4 at B*/
void B_input(struct pkt packet)
{
	cout << "B: Received packet at layer 3. Count =  " << ++layer3B << endl;

	//First check if the received packet has the right sequence number and is not corrupt
	if(packet.seqnum == expectedSeqNo && packet.checksum == calculateChecksum(packet)) {

		//If everything is correct then deliver the packet to the application layer of B
		tolayer5(1, packet.payload);

		cout << "B: Received message at layer 5. Count =  " << ++layer5B << endl;

		//Now set the acknowledgment number in the packet to be sent to A depending upon the packet that was received
		packetB.acknum = expectedSeqNo;

		//Calculate the checksum for the packet and assign to the checksum attribute in the
		//packet so that we can verify at the A side on receipt of this packet if the packet is corrupt or not
		packetB.checksum = calculateChecksum(packetB);

		//Send the packet from B to A through the unreliable channel
		tolayer3(1, packetB);

		//Increment the expected sequence number
		expectedSeqNo++;
	}

	//If the received packet is not correct send as acknowledgment the previous packet
	else {

		//Re send the packet from B to A through the unreliable channel
		tolayer3(1, packetB);

		cout << "Sent acknowledgment from B. Ack No. = " << packet.seqnum << endl;
	}
}

/* called when B's timer goes off */
void B_timerinterrupt() //ram's comment - changed the return type to void.
{
	//There is no timeout logic at B in case of unidirectional transfer from A to B
}

/* the following rouytine will be called once (only) before any other */
/* entity B routines are called. You can use it to do any initialization */
void B_init() //ram's comment - changed the return type to void.
{
	//Set the expected sequence number to 1 initially.
	expectedSeqNo = 1;

	//Set the sequence number for packetB to 0 permanently as the data transfer is unidirectional
	//and thus we do not have to send any data to A
	packetB.seqnum = 0;

	//As we send acknowledgment in packet from B to A without pay load, set the pay load to 0
	memset(packetB.payload, 0, 20);

	//Set the number of packets received from layer 5 and layer 3 to 0 initially.
	layer5B = layer3B = 0;

	//Make a default packet and keep just in case we need to acknowledge if the first packet is corrupt
	//Assign the expected sequence number to the acknowledgment number to send back to A
	packetB.acknum = expectedSeqNo;

	//Calculate the checksum for the packet and assign to the checksum attribute in the
	//packet so that we can verify at the A side on receipt of this packet if the packet is corrupt or not
	packetB.checksum = calculateChecksum(packetB);
}

int TRACE = 1;             /* for my debugging */
int nsim = 0;              /* number of messages from 5 to 4 so far */
int nsimmax = 0;           /* number of msgs to generate, then stop */
float time_local = 0;
float lossprob;            /* probability that a packet is dropped  */
float corruptprob;         /* probability that one bit is packet is flipped */
float lambda;              /* arrival rate of messages from layer 5 */
int   ntolayer3;           /* number sent into layer 3 */
int   nlost;               /* number lost in media */
int ncorrupt;              /* number corrupted by media*/

/****************************************************************************/
/* jimsrand(): return a float in range [0,1].  The routine below is used to */
/* isolate all random number generation in one location.  We assume that the*/
/* system-supplied rand() function return an int in therange [0,mmm]        */
/****************************************************************************/
float jimsrand() 
{
  double mmm = 2147483647;   /* largest int  - MACHINE DEPENDENT!!!!!!!!   */
  float x;                   /* individual students may need to change mmm */ 
  x = rand()/mmm;            /* x should be uniform in [0,1] */
  return(x);
}  


/*****************************************************************
***************** NETWORK EMULATION CODE IS BELOW ***********
The code below emulates the layer 3 and below network environment:
  - emulates the tranmission and delivery (possibly with bit-level corruption
    and packet loss) of packets across the layer 3/4 interface
  - handles the starting/stopping of a timer, and generates timer
    interrupts (resulting in calling students timer handler).
  - generates message to be sent (passed from later 5 to 4)

THERE IS NOT REASON THAT ANY STUDENT SHOULD HAVE TO READ OR UNDERSTAND
THE CODE BELOW.  YOU SHOLD NOT TOUCH, OR REFERENCE (in your code) ANY
OF THE DATA STRUCTURES BELOW.  If you're interested in how I designed
the emulator, you're welcome to look at the code - but again, you should have
to, and you defeinitely should not have to modify
******************************************************************/



/* possible events: */
#define  TIMER_INTERRUPT 0  
#define  FROM_LAYER5     1
#define  FROM_LAYER3     2

#define  OFF             0
#define  ON              1
#define   A    0
#define   B    1


struct event {
   float evtime;           /* event time */
   int evtype;             /* event type code */
   int eventity;           /* entity where event occurs */
   struct pkt *pktptr;     /* ptr to packet (if any) assoc w/ this event */
   struct event *prev;
   struct event *next;
 };
struct event *evlist = NULL;   /* the event list */


void insertevent(struct event *p)
{
   struct event *q,*qold;

   if (TRACE>2) {
      printf("            INSERTEVENT: time is %lf\n",time_local);
      printf("            INSERTEVENT: future time will be %lf\n",p->evtime); 
      }
   q = evlist;     /* q points to header of list in which p struct inserted */
   if (q==NULL) {   /* list is empty */
        evlist=p;
        p->next=NULL;
        p->prev=NULL;
        }
     else {
        for (qold = q; q !=NULL && p->evtime > q->evtime; q=q->next)
              qold=q; 
        if (q==NULL) {   /* end of list */
             qold->next = p;
             p->prev = qold;
             p->next = NULL;
             }
           else if (q==evlist) { /* front of list */
             p->next=evlist;
             p->prev=NULL;
             p->next->prev=p;
             evlist = p;
             }
           else {     /* middle of list */
             p->next=q;
             p->prev=q->prev;
             q->prev->next=p;
             q->prev=p;
             }
         }
}





/********************* EVENT HANDLINE ROUTINES *******/
/*  The next set of routines handle the event list   */
/*****************************************************/

void generate_next_arrival()
{
   double x,log(),ceil();
   struct event *evptr;
//    //char *malloc();
   float ttime;
   int tempint;

   if (TRACE>2)
       printf("          GENERATE NEXT ARRIVAL: creating new arrival\n");

   x = lambda*jimsrand()*2;  /* x is uniform on [0,2*lambda] */
                             /* having mean of lambda        */

   evptr = (struct event *)malloc(sizeof(struct event));
   evptr->evtime =  time_local + x;
   evptr->evtype =  FROM_LAYER5;
   if (BIDIRECTIONAL && (jimsrand()>0.5) )
      evptr->eventity = B;
    else
      evptr->eventity = A;
   insertevent(evptr);
}





void init()                         /* initialize the simulator */
{
  int i;
  float sum, avg;
  float jimsrand();
  
  
   printf("-----  Stop and Wait Network Simulator Version 1.1 -------- \n\n");
   printf("Enter the number of messages to simulate: ");
   scanf("%d",&nsimmax);
   printf("Enter  packet loss probability [enter 0.0 for no loss]:");
   scanf("%f",&lossprob);
   printf("Enter packet corruption probability [0.0 for no corruption]:");
   scanf("%f",&corruptprob);
   printf("Enter average time between messages from sender's layer5 [ > 0.0]:");
   scanf("%f",&lambda);
   printf("Enter TRACE:");
   scanf("%d",&TRACE);

   srand(9999);              /* init random number generator */
   sum = 0.0;                /* test random number generator for students */
   for (i=0; i<1000; i++)
      sum=sum+jimsrand();    /* jimsrand() should be uniform in [0,1] */
   avg = sum/1000.0;
   if (avg < 0.25 || avg > 0.75) {
    printf("It is likely that random number generation on your machine\n" ); 
    printf("is different from what this emulator expects.  Please take\n");
    printf("a look at the routine jimsrand() in the emulator code. Sorry. \n");
    exit(0);
    }

   ntolayer3 = 0;
   nlost = 0;
   ncorrupt = 0;

   time_local=0;                    /* initialize time to 0.0 */
   generate_next_arrival();     /* initialize event list */
}






//int TRACE = 1;             /* for my debugging */
//int nsim = 0;              /* number of messages from 5 to 4 so far */ 
//int nsimmax = 0;           /* number of msgs to generate, then stop */
//float time = 0.000;
//float lossprob;            /* probability that a packet is dropped  */
//float corruptprob;         /* probability that one bit is packet is flipped */
//float lambda;              /* arrival rate of messages from layer 5 */   
//int   ntolayer3;           /* number sent into layer 3 */
//int   nlost;               /* number lost in media */
//int ncorrupt;              /* number corrupted by media*/

main()
{
   struct event *eventptr;
   struct msg  msg2give;
   struct pkt  pkt2give;
   
   int i,j;
   char c; 
  
   init();
   A_init();
   B_init();
   
   while (1) {
        eventptr = evlist;            /* get next event to simulate */
        if (eventptr==NULL)
           goto terminate;
        evlist = evlist->next;        /* remove this event from event list */
        if (evlist!=NULL)
           evlist->prev=NULL;
        if (TRACE>=2) {
           printf("\nEVENT time: %f,",eventptr->evtime);
           printf("  type: %d",eventptr->evtype);
           if (eventptr->evtype==0)
	       printf(", timerinterrupt  ");
             else if (eventptr->evtype==1)
               printf(", fromlayer5 ");
             else
	     printf(", fromlayer3 ");
           printf(" entity: %d\n",eventptr->eventity);
           }
        time_local = eventptr->evtime;        /* update time to next event time */
        if (nsim==nsimmax)
	  break;                        /* all done with simulation */
        if (eventptr->evtype == FROM_LAYER5 ) {
            generate_next_arrival();   /* set up future arrival */
            /* fill in msg to give with string of same letter */    
            j = nsim % 26; 
            for (i=0; i<20; i++)  
               msg2give.data[i] = 97 + j;
            if (TRACE>2) {
               printf("          MAINLOOP: data given to student: ");
                 for (i=0; i<20; i++) 
                  printf("%c", msg2give.data[i]);
               printf("\n");
	     }
            nsim++;
            if (eventptr->eventity == A) 
               A_output(msg2give);  
             else
               B_output(msg2give);  
            }
          else if (eventptr->evtype ==  FROM_LAYER3) {
            pkt2give.seqnum = eventptr->pktptr->seqnum;
            pkt2give.acknum = eventptr->pktptr->acknum;
            pkt2give.checksum = eventptr->pktptr->checksum;
            for (i=0; i<20; i++)  
                pkt2give.payload[i] = eventptr->pktptr->payload[i];
	    if (eventptr->eventity ==A)      /* deliver packet by calling */
   	       A_input(pkt2give);            /* appropriate entity */
            else
   	       B_input(pkt2give);
	    free(eventptr->pktptr);          /* free the memory for packet */
            }
          else if (eventptr->evtype ==  TIMER_INTERRUPT) {
            if (eventptr->eventity == A) 
	       A_timerinterrupt();
             else
	       B_timerinterrupt();
             }
          else  {
	     printf("INTERNAL PANIC: unknown event type \n");
             }
        free(eventptr);
        }

terminate:
   printf(" Simulator terminated at time %f\n after sending %d msgs from layer5\n",time_local,nsim);

   cout << endl;
   	cout << "Protocol: Go-Back-N" << endl;
   	cout << layer5A << " packets sent from the Application Layer of Sender A" << endl;
	cout << layer3A << " packets sent from the Transport Layer of Sender A" << endl;
	cout << layer3B << " packets received at the Transport layer of Receiver B" << endl;
	cout << layer5B << " packets received at the Application layer of Receiver B" << endl;
	cout << "Total time: " << time_local << " time units" << endl;
	cout << "Throughput = " << layer5B/time_local << " packets/time units" << endl;
}


/********************* EVENT HANDLINE ROUTINES *******/
/*  The next set of routines handle the event list   */
/*****************************************************/
 
/*void generate_next_arrival()
{
   double x,log(),ceil();
   struct event *evptr;
    //char *malloc();
   float ttime;
   int tempint;

   if (TRACE>2)
       printf("          GENERATE NEXT ARRIVAL: creating new arrival\n");
 
   x = lambda*jimsrand()*2;  // x is uniform on [0,2*lambda] 
                             // having mean of lambda       
   evptr = (struct event *)malloc(sizeof(struct event));
   evptr->evtime =  time + x;
   evptr->evtype =  FROM_LAYER5;
   if (BIDIRECTIONAL && (jimsrand()>0.5) )
      evptr->eventity = B;
    else
      evptr->eventity = A;
   insertevent(evptr);
} */




void printevlist()
{
  struct event *q;
  int i;
  printf("--------------\nEvent List Follows:\n");
  for(q = evlist; q!=NULL; q=q->next) {
    printf("Event time: %f, type: %d entity: %d\n",q->evtime,q->evtype,q->eventity);
    }
  printf("--------------\n");
}



/********************** Student-callable ROUTINES ***********************/

/* called by students routine to cancel a previously-started timer */
void stoptimer(int AorB)
 //AorB;  /* A or B is trying to stop timer */
{
 struct event *q,*qold;

 if (TRACE>2)
    printf("          STOP TIMER: stopping timer at %f\n",time_local);
/* for (q=evlist; q!=NULL && q->next!=NULL; q = q->next)  */
 for (q=evlist; q!=NULL ; q = q->next) 
    if ( (q->evtype==TIMER_INTERRUPT  && q->eventity==AorB) ) { 
       /* remove this event */
       if (q->next==NULL && q->prev==NULL)
             evlist=NULL;         /* remove first and only event on list */
          else if (q->next==NULL) /* end of list - there is one in front */
             q->prev->next = NULL;
          else if (q==evlist) { /* front of list - there must be event after */
             q->next->prev=NULL;
             evlist = q->next;
             }
           else {     /* middle of list */
             q->next->prev = q->prev;
             q->prev->next =  q->next;
             }
       free(q);
       return;
     }
  printf("Warning: unable to cancel your timer. It wasn't running.\n");
}


void starttimer(int AorB,float increment)
// AorB;  /* A or B is trying to stop timer */

{

 struct event *q;
 struct event *evptr;
 ////char *malloc();

 if (TRACE>2)
    printf("          START TIMER: starting timer at %f\n",time_local);
 /* be nice: check to see if timer is already started, if so, then  warn */
/* for (q=evlist; q!=NULL && q->next!=NULL; q = q->next)  */
   for (q=evlist; q!=NULL ; q = q->next)  
    if ( (q->evtype==TIMER_INTERRUPT  && q->eventity==AorB) ) { 
      printf("Warning: attempt to start a timer that is already started\n");
      return;
      }
 
/* create future event for when timer goes off */
   evptr = (struct event *)malloc(sizeof(struct event));
   evptr->evtime =  time_local + increment;
   evptr->evtype =  TIMER_INTERRUPT;
   evptr->eventity = AorB;
   insertevent(evptr);
} 


/************************** TOLAYER3 ***************/
void tolayer3(int AorB,struct pkt packet)
{
 struct pkt *mypktptr;
 struct event *evptr,*q;
 ////char *malloc();
 float lastime, x, jimsrand();
 int i;


 ntolayer3++;

 /* simulate losses: */
 if (jimsrand() < lossprob)  {
      nlost++;
      if (TRACE>0)    
	printf("          TOLAYER3: packet being lost\n");
      return;
    }  

/* make a copy of the packet student just gave me since he/she may decide */
/* to do something with the packet after we return back to him/her */ 
 mypktptr = (struct pkt *)malloc(sizeof(struct pkt));
 mypktptr->seqnum = packet.seqnum;
 mypktptr->acknum = packet.acknum;
 mypktptr->checksum = packet.checksum;
 for (i=0; i<20; i++)
    mypktptr->payload[i] = packet.payload[i];
 if (TRACE>2)  {
   printf("          TOLAYER3: seq: %d, ack %d, check: %d ", mypktptr->seqnum,
	  mypktptr->acknum,  mypktptr->checksum);
    for (i=0; i<20; i++)
        printf("%c",mypktptr->payload[i]);
    printf("\n");
   }

/* create future event for arrival of packet at the other side */
  evptr = (struct event *)malloc(sizeof(struct event));
  evptr->evtype =  FROM_LAYER3;   /* packet will pop out from layer3 */
  evptr->eventity = (AorB+1) % 2; /* event occurs at other entity */
  evptr->pktptr = mypktptr;       /* save ptr to my copy of packet */
/* finally, compute the arrival time of packet at the other end.
   medium can not reorder, so make sure packet arrives between 1 and 10
   time units after the latest arrival time of packets
   currently in the medium on their way to the destination */
 lastime = time_local;
/* for (q=evlist; q!=NULL && q->next!=NULL; q = q->next) */
 for (q=evlist; q!=NULL ; q = q->next) 
    if ( (q->evtype==FROM_LAYER3  && q->eventity==evptr->eventity) ) 
      lastime = q->evtime;
 evptr->evtime =  lastime + 1 + 9*jimsrand();
 


 /* simulate corruption: */
 if (jimsrand() < corruptprob)  {
    ncorrupt++;
    if ( (x = jimsrand()) < .75)
       mypktptr->payload[0]='Z';   /* corrupt payload */
      else if (x < .875)
       mypktptr->seqnum = 999999;
      else
       mypktptr->acknum = 999999;
    if (TRACE>0)    
	printf("          TOLAYER3: packet being corrupted\n");
    }  

  if (TRACE>2)  
     printf("          TOLAYER3: scheduling arrival on other side\n");
  insertevent(evptr);
} 

void tolayer5(int AorB,char *datasent)
{
  
  int i;  
  if (TRACE>2) {
     printf("          TOLAYER5: data received: ");
     for (i=0; i<20; i++)  
        printf("%c",datasent[i]);
     printf("\n");
   }
  
}

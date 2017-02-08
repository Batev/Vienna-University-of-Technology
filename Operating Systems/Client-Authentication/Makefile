# Made by Evgeni Batev (e1328036)
# Program name: auth

CC = gcc
DEFS = -D_XOPEN_SOURCE=500 -D_BSD_SOURCE
CFLAGS = -Wall -g -std=c99 -pedantic $(DEFS)

OBJECTS = auth-server auth-client

all: clean auth-server auth-client

auth-server:
	$(CC) $(CFLAGS) auth-server.c -o auth-server -lrt -pthread
	
auth-client: 
	$(CC) $(CFLAGS) auth-client.c -o auth-client -lrt -pthread
	
clean:
	rm -f $(OBJECTS)

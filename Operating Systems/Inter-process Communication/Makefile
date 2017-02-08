# Made by Evgeni Batev (e1328036)
# Program name: mygzip

CC = gcc
DEFS = -D_XOPEN_SOURCE=500 -D_BSD_SOURCE
CFLAGS = -Wall -g -std=c99 -pedantic $(DEFS)
	
TARGETS = main
FUNCTIONS = mygzip

all: clean main

main:
	$(CC) $(CFLAGS) main.c mygzip.c -o mygzip

clean:
	rm -f main

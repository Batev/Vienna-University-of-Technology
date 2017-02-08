/**
 * @brief Implementation of the mygzip module.
 * 
 * The module is created for only two children that comunicate with eachother.
 * The program that is started by the first child is gzip with params -cf.
 **/

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include "mygzip.h"

int execute_child1(int *pipefd1, int *pipefd2)
{
    (void) close(pipefd1[1]);
    (void) close(pipefd2[0]);

    if (dup2(pipefd1[0], STDIN_FILENO) == -1) 
    {
        (void) fprintf(stderr, "Error while executing dup2()!\n");
        return EXIT_FAILURE;
    }
    
    (void) close(pipefd1[0]);
    
    if (dup2(pipefd2[1], STDOUT_FILENO) == -1)
    {
        (void) fprintf(stderr, "Error while executing dup2()!\n");
        return EXIT_FAILURE;
    }
    
    (void) close(pipefd2[1]);
    
    if (execlp("gzip", "gzip", "-cf", (char *) NULL) == -1)
    {
        (void) fprintf(stderr, "Error while executing execlp()!\n");
        return EXIT_FAILURE;
    }
    
    /** unreachable statment
        assert(0); */
    return EXIT_FAILURE;
}

int execute_child2(int *pipefd2, const char *filename)
{
    (void) close(pipefd2[1]);
    
    char buffer[BUFFER_SIZE];
    FILE *file;
    
    if (filename == NULL)
    {
        file = stdout;
    }
    else
    {
        file = fopen(filename, "w+");
        
        if (file == NULL) 
        {
            (void) fprintf(stderr, "Error while executing fopen()!\n");
            return EXIT_FAILURE;
        }
    }
    
    int res;
    
    while((res = read(pipefd2[0], buffer, BUFFER_SIZE)) != 0)
    {
        if (res < 0)
        {
            (void) fprintf(stderr, "Error while executing read()!\n");
            return EXIT_FAILURE;
        }

        if ((fwrite(buffer, sizeof(char), res, file)) == -1)
        {
            (void) fprintf(stderr, "Error while executing write()!\n");
            return EXIT_FAILURE;
        }
    }
    
    (void) close(pipefd2[0]);    
    (void) fclose(file);
    
    return EXIT_SUCCESS;
}

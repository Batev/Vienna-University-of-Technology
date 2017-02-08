/**
 * @brief The main module of the program.
 **/

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include "mygzip.h"

/**
 * Program entry point.
 * @brief The program establishes the communication between
 * two processes through pipes. The first process stars also
 * for compression of the input.
 * @param argc The argument counter.
 * @param argv The argument vector.
 * @return Returns EXIT_SUCCESS on success and EXIT_FAILURE on failure.
*/
int main(int argc, char *argv[])
{
    /** declaring the two piepes */
    int pipefd1[2];
    int pipefd2[2];
    
    /** the name of the file if presented */
    char *filename = NULL;
    
    if (argc > 2)
    {
        (void)fprintf(stderr, "Usage: %s [file]\n", argv[0]);                
        exit(EXIT_FAILURE);
    }
    else if (argc == 2)
    {
        filename = argv[1];
    }
    
    if (pipe(pipefd1) == -1 || pipe(pipefd2) == -1)
    {
        (void) fprintf(stderr, "Error while executing pipe()!\n");
        exit(EXIT_FAILURE);
    }
    
    /** starting the first process */
    pid_t pid1 = fork();
    pid_t pid2 = -1;
    
    switch (pid1) {
        case -1:
            (void) fprintf(stderr, "Error while executing fork()!\n");
            (void) close(pipefd1[0]);
            (void) close(pipefd1[1]);
            (void) close(pipefd2[0]);
            (void) close(pipefd2[1]);
            exit(EXIT_FAILURE);
            
        case 0:
            /** child 1 */
            if (execute_child1(pipefd1, pipefd2) == -1)
            {

                exit(EXIT_FAILURE);
            }
            exit(EXIT_SUCCESS);
        break;
        
        default:
            /** parent */
            pid2 = fork();
            
            switch (pid2) {
                case -1:
                    (void) fprintf(stderr, "Error while executing fork()!\n");
                    (void) close(pipefd1[0]);
                    (void) close(pipefd1[1]);
                    (void) close(pipefd2[0]);
                    (void) close(pipefd2[1]);
                    exit(EXIT_FAILURE);
                    
                case 0:
                    /** child 2 */
                    
                    (void) close(pipefd1[0]);
                    (void) close(pipefd1[1]);
                    
                    if (execute_child2(pipefd2, filename) == -1)
                    {
                        exit(EXIT_FAILURE);
                    }
                    
                    (void) close(STDIN_FILENO);
                    exit(EXIT_SUCCESS);
                break;
                
                default:
                    break;                    
            }
            break;    
    }
    
    /** parent */
                    
    (void) close(pipefd1[0]);
    (void) close(pipefd2[0]);
    (void) close(pipefd2[1]);
    
    int res;
    char buffer[BUFFER_SIZE];
    
    while((res = read(STDIN_FILENO, buffer, BUFFER_SIZE)) != 0)
    {
        if (res < 0)
        {
            (void) fprintf(stderr, "Error while executing read()!\n");
            return EXIT_FAILURE;
        }

        if ((write(pipefd1[1], buffer, res)) == -1)
        {
            (void) fprintf(stderr, "Error while executing write()!\n");
            return EXIT_FAILURE;
        }
    }
    
    (void) close(pipefd1[1]);
    
    int statusChild1 = 0;
    int statusChild2 = 0;
    
    /** waiting for the two processes to terminate */
    if (waitpid(pid1,&statusChild1,0) == -1) {
        (void) fprintf(stderr, "Error while waiting for child with pid: %d!\n", pid1);
        exit(EXIT_FAILURE);
    }
    
    if (WEXITSTATUS(statusChild1) == 1)
    {
        (void) fprintf(stderr, "Error while executing child with pid: %d!\n", pid1);
        exit(EXIT_FAILURE);
    }
    
    if (waitpid(pid2,&statusChild2,0) == -1) {
        (void) fprintf(stderr, "Error while waiting for child with pid: %d!\n", pid2);
        exit(EXIT_FAILURE);
    }
    
    if (WEXITSTATUS(statusChild2) == 1)
    {
        (void) fprintf(stderr, "Error while executing child with pid: %d!\n", pid2);
        exit(EXIT_FAILURE);
    }
                        
    return EXIT_SUCCESS;
}

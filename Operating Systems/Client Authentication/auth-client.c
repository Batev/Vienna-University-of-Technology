/**
*   The client side of the application which takes care about
*   sending request and offering user interface.
*/

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <semaphore.h>
#include <signal.h>
#include <string.h>
#include "common.h"

volatile sig_atomic_t want_quit = 0;

void usage(char* name);

void on_error(char* msg);

void save_array(char* arr, char* input_data);

void handle_signal(int signal);

void setup_signal();

void print_options(void);

void save_secret(char buffer[], sem_t* sem3);

void get_secret(char buffer[]);

/**
* @brief main method of the program.
* @param argc The number of the arguments of the program.
* @param argv The arguments of the program.
* @return the exit status of the process.
*/
int main(int argc, char* argv[])
{
    (void) setup_signal();

     /** Declare the variables */
    char* username;
    char* password;
    int opt;
    int binr = 0;
    int binl = 0;
    struct data *shared;
    int shmfd;
    sem_t *sem1;
    sem_t *sem2;
    sem_t *sem3;
    char buffer[MAX_DATA+1];
    int command = EMPTY;
    int current_flag = EMPTY;
    
    /** Check the optional arguments */
    while ((opt = getopt(argc, argv, "rl")) != -1)
    {
        switch(opt)
        {
            case 'r':
                binr = 1;
                break;
            case 'l':
                binl = 1;             
                break;

            case '?':
                (void) usage(argv[0]);

            default:
                (void) usage(argv[0]);
        }
    }
    
    /** Check the number of the arguments */
    if(!(argc == 4 && (binr^binl)))
    {
        (void) usage(argv[0]);
    }
    
    username = argv[2];
    password = argv[3]; 
        
    /** Open the semaphores */
    sem1 = sem_open(SEM_1, 0);
    sem2 = sem_open(SEM_2, 0);
    sem3 = sem_open(SEM_3, 0);
    
    if (sem1 == SEM_FAILED || sem2 == SEM_FAILED || sem3 == SEM_FAILED)
    {
        (void) on_error("Problem while executing sem_open(3).");
    } 
    
    /** Create a share memory */
    shmfd = shm_open(SHM_NAME, O_RDWR | O_CREAT, PERMISSION);
    
    if(shmfd == -1)
    {
        (void) on_error("Problem while executing shm_open(3).");
    }
            
    /** Map the data to be shared */
    shared = mmap(NULL, sizeof *shared, PROT_READ | PROT_WRITE, MAP_SHARED, shmfd, 0);
    
    if (shared == MAP_FAILED)
    {
        (void) on_error("Problem while executing mmap(2).");
    }
    
    /** Close the shared memory file descriptor */
    if (close(shmfd) == -1)
    {
        (void) on_error("Problem while executing close(2).");
    }
    
        
    /** critical section */   
    
    if (sem_wait(sem3) == -1)
    {
        if (errno != EINTR) (void) on_error("Problem while executing sem_wait(3).");        
    }
        
    if(binr)
    {
        shared->flag = REGISTER;
    }
    else if(binl)
    {
        shared->flag = LOGIN;
    }
    
    /** Save the arrays in the shared memory */
    (void) save_array(shared->username, username);    
    (void) save_array(shared->password, password);
          
    if (sem_post(sem1) == -1)
    {
        (void) on_error("Problem while executing sem_post(3).");
    }
   
    if (sem_wait(sem2) == -1)
    {        
        if (errno != EINTR) (void) on_error("Problem while executing sem_wait(3).");
    }
      
    current_flag = shared->flag;
 
    if (sem_post(sem3) == -1)
    {
        (void) on_error("Problem while executing sem_post(3).");
    }
      
    if(binr && (current_flag == ERROR))
    {
        (void) on_error("User with this name and password already registered.");
    }
    else if (binl && (current_flag == ERROR))
    {
        (void) on_error("User with this name and password does not exist.");
    }    
    
    while (command != 3 && !want_quit)
    {
        if (sem_wait(sem3) == -1)
        {
            if (errno == EINTR) continue;
            (void) on_error("Problem while executing sem_wait(3).");
        }
        
        if (shared->flag == SERVER_ERROR)
        {
            break;
        }
        
        if (sem_post(sem3) == -1)
        {
            (void) on_error("Problem while executing sem_post(3).");
        }
        
        (void) print_options();
        
        if (read(STDIN_FILENO, buffer, MAX_DATA) == -1)
        {
            (void) on_error("Problem while executing read(2).");
        }
        
        buffer[MAX_DATA] = '\0';
        
        command = (int)strtol(buffer, NULL, 10);
        
        switch (command)
        {
        
            case 1:            
                (void) fprintf(stdout, "\nWrite a new secret:\n");
                
                char buffer[MAX_DATA+1];
    
                if (read(STDIN_FILENO, buffer, MAX_DATA) == -1)
                {
                    if (errno == EINTR) continue;
                    (void) on_error("Problem while executing read(2).");
                }
                
                buffer[MAX_DATA] = '\0';
                
                if (sem_wait(sem3) == -1)
                {
                    if (errno == EINTR) continue;
                    (void) on_error("Problem while executing sem_wait(3).");
                }
                
                (void) strcpy(shared->username, username);
                (void) strcpy(shared->password, password);
                (void) strcpy(shared->secret, buffer);
                    
                shared->flag = WRITE_SECRET;
                                
                if (sem_post(sem1) == -1 || sem_post(sem3) == -1)
                {
                    (void) on_error("Problem while executing sem_post(3).");
                }
                
                if (sem_wait(sem2) == -1)
                {
                    if (errno == EINTR) continue;
                    (void) on_error("Problem while executing sem_wait(3).");
                }
                break;
                
                
            case 2:
                (void) fprintf(stdout, "\nReading the secret:\n");
                
                if (sem_wait(sem3) == -1)
                {
                    if (errno == EINTR) continue;
                    (void) on_error("Problem while executing sem_wait(3).");
                }
                                
                (void) strcpy(shared->username, username);
                (void) strcpy(shared->password, password);
                shared->flag = READ_SECRET;
                
                if (sem_post(sem1) == -1)
                {
                    (void) on_error("Problem while executing sem_post(3).");
                }

                if (sem_wait(sem2) == -1)
                {
                    if (errno == EINTR) continue;
                    (void) on_error("Problem while executing sem_wait(3).");
                }
                               
                int i = 0;

                if (shared->secret[i] == '\0')
                {
                    (void) fprintf(stdout, "No secret set.");
                }

                while (shared->secret[i] != '\0')
                {
                    (void) fprintf(stdout, "%c", shared->secret[i]);
                    i++;
                }
                
                (void) fprintf(stdout, "\n\n");
                
                if (sem_post(sem3) == -1)
                {
                    (void) on_error("Problem while executing sem_post(3).");
                }
                break;
                
                
            case 3:
                if (sem_wait(sem3) == -1)
                {
                    if (errno == EINTR) continue;
                    (void) on_error("Problem while executing sem_wait(3).");
                }
            
                (void) strcpy(shared->username, username);
                (void) strcpy(shared->password, password);
                shared->flag = LOGOUT;
               
                if (sem_post(sem1) == -1 || sem_post(sem3) == -1)
                {
                    (void) on_error("Problem while executing sem_post(3).");
                }
                (void) fprintf(stdout, "\nLogging out...\n\n");
                
                if (sem_wait(sem2) == -1)
                {
                    if (errno == EINTR) continue;
                    (void) on_error("Problem while executing sem_wait(3).");
                }
                break;
                
                
            default:
                (void) fprintf(stdout, "\nInvalid command!\n\n");
                command = EMPTY;
                break;
        }    
    }
    
    /** send error code to the server */
    if (want_quit == 1)
    {
        if (sem_wait(sem3) == -1)
        {
            (void) on_error("Problem while executing sem_wait(3).");
        }
        
        shared->flag = ERROR;
        
        if (sem_post(sem3) == -1)
        {
            (void) on_error("Problem while executing sem_post(3).");
        }
    }    
    
    /** critical section out */
    
    /** Unmap the data */
    if (munmap(shared, sizeof *shared) == -1)
    {
        (void) on_error("Problem while executing munmap(2).");
    }
          
    /** Clse the semaphores */  
    if (sem_close(sem1) == -1)
    {
        (void) on_error("Problem while executing sem_close(3).");
    }
    
    if (sem_close(sem2) == -1)
    {
        (void) on_error("Problem while executing sem_close(3).");
    }
    
    if (sem_close(sem3) == -1)
    {
        (void) on_error("Problem while executing sem_close(3).");
    }
    
    return EXIT_SUCCESS;
}

/**
* @brief prints usage info.
* @param name the name of the program.
*/
void usage(char* name)
{
    (void) fprintf(stderr, "USAGE: %s { -r | -l } username password\n", name);
    
    exit(EXIT_FAILURE);
}

/**
* @brief Prints info when error occurs.
* @param msg Message to be printed.
*/
void on_error(char* msg)
{
    (void) fprintf(stderr, "Error code: %s\n", msg);
    
    exit(EXIT_FAILURE);
}

/**
*   @brief saves the an array
*   @param arr
*   @param input_data
*/
void save_array(char* arr, char* input_data)
{
    int i;
    size_t size = strlen(input_data);
    
    if (size >= MAX_DATA || size == 0)
    {
        (void) on_error("Input data either too big or too small.");
    }
    
    for (i = 0; i < size; i++)
    {
        if (((int)(*(input_data + i)) == 92) && size > i+1 && (((int)(*(input_data + i + 1))) == 110))
        {
            (void) on_error("Input data either too big or too small.");
        }
                
        *(arr + i) = *(input_data + i);
    }
    
    *(arr + size) = '\0';
}

/**
*   @brief prints the user options.
*/
void print_options(void)
{
    (void) fprintf(stdout, "Commands:\n  1) write secret\n  2) read secret\n  3) logout\n");
    (void) fprintf(stdout, "Please select a command (1-3):\n");
}

/**
*   @brief Saves the secret of the client in the shared memory.
*   @param secret an array with the secret.
*   @param sem3 the blocking semaphores.
*/
void save_secret(char secret[], sem_t *sem3)
{
    char buffer[MAX_DATA+1];
    
    if (read(STDIN_FILENO, buffer, MAX_DATA) == -1)
    {
        (void) on_error("Problem while executing read(2).");
    }
    
    buffer[MAX_DATA] = '\0';
    
    sem_wait(sem3);
    
    strcpy(secret, buffer);    
    
    sem_post(sem3);
}

/**
*   @brief Gets the secret of the current client.
*
*/
void get_secret(char buffer[])
{
    int i = 0;

    if (buffer[i] == '\0')
    {
        (void) fprintf(stdout, "No secret set.");
    }

    while (buffer[i] != '\0')
    {
        (void) fprintf(stdout, "%c", buffer[i]);
        i++;
    }
    
    (void) fprintf(stdout, "\n\n");
}

/**
*   @brief Defines the behiviour of the program when a signal occurs.
*   @signal The catched signal.
*/
void handle_signal(int signal)
{
    want_quit = 1;
}

/**
*   @brief Setups the signal handlers for the SIGINT and SIGTERM.
*/
void setup_signal()
{
    const int signals[] = {SIGINT, SIGTERM};
    
    struct sigaction s;
    memset(&s, 0, sizeof(struct sigaction));
 
    if (sigfillset(&s.sa_mask) < 0) {
        on_error("sigfillset");
    }
    
    sigdelset(&s.sa_mask,SIGINT);
    sigdelset(&s.sa_mask,SIGTERM);
    sigprocmask(SIG_SETMASK, &s.sa_mask, NULL);
    
    s.sa_handler = handle_signal;
    s.sa_flags = 0;

    for(int i = 0; i < 2; i++) {
        if (sigaction(signals[i], &s,NULL) < 0) {
            on_error("sigaction");
        }
    }
}

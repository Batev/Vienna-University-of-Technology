/**
*   The server side of the application which takes care about
*   the requests of the clients and the shared ressources.
*/

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <semaphore.h>
#include <signal.h>
#include <string.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/wait.h>
#include "common.h"

void usage(char* name);

void on_error(char* msg);

int check_for_user(FILE* fp, FILE* database, char* input_username, char* input_password, char* secret, int flag);

void write_to_database(FILE *fp, FILE *database);

void write_secret(FILE *fp, int count, int semicount, char *secret);

void register_user(FILE* fp, char* input_username, char* input_password);

void setup_signal(void);

void handle_signal(int signal);

void free_resources(void);

volatile sig_atomic_t want_quit = 0;

/**
* @brief main method of the program.
* @param argc The number of the arguments of the program.
* @param argv The arguments of the program.
* @return the exit status of the process.
*/
int main(int argc, char* argv[])
{
    (void) atexit(free_resources);
    (void) setup_signal();

    /** Declare the variables */
    char* database;
    int opt;
    int bin = 0;
    FILE *fp;
    FILE *databasefp;
    struct data *shared;
    int shmfd;
    sem_t *sem1;
    sem_t *sem2;
    sem_t *sem3;
    int user_option;
    int check_user;
    int number_of_users = -1;
    
    /** Check the optional arguments */
    while ((opt = getopt(argc, argv, "l:")) != -1)
    {
        switch(opt)
        {
            case 'l':
                bin = 1;
                database = optarg;
                break;

            case '?':
                (void) usage(argv[0]);

            default:
                (void) usage(argv[0]);
        }
    }
    
    /** Check the number of the arguments */
    if(!(argc == 1 || (argc == 3 && bin)))
    {
        (void) usage(argv[0]);
    }
        
    /** Open the database */    
    fp = bin ? fopen(database, "r+") : fopen("database", "w+");
    
    databasefp = fopen("auth-server.db.csv", "w+");
    
    if (fp == NULL || databasefp == NULL)
    {
        (void) on_error("Problem while executing fopen(3).");
    }
    
    /** Open the semaphores */
    sem1 = sem_open(SEM_1, O_CREAT | O_EXCL, 0600, 0);
    sem2 = sem_open(SEM_2, O_CREAT | O_EXCL, 0600, 0);
    sem3 = sem_open(SEM_3, O_CREAT | O_EXCL, 0600, 1);
    
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
    
    /** Set the size of the shared memory */
    if (ftruncate(shmfd, sizeof *shared) == -1)
    {
        (void) on_error("Problem while executing ftruncate(2).");
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
    
    /** critical section in */
    
    while(!want_quit && number_of_users != 0)
    { 
        if (number_of_users == -1)
        {
            number_of_users = 0;
        }
        
        if (sem_wait(sem1) == -1)
        {
            if (errno == EINTR) continue;
            (void) on_error("Problem while executing sem_wait(3).");
        }
         
        user_option = shared->flag;
        
        /** register a new user */
        if (user_option == REGISTER)
        {
            /** checks, whether a user is registered or not. */   
            check_user = check_for_user(fp, databasefp, shared->username, shared->password, shared->secret, REGISTER);
            if (check_user == USER_REGISTERED)
            {
                (void) printf("User with this name exists alredy.\n"); 
                shared->flag = ERROR;
            }
            else
            {
                number_of_users++;
                shared->flag = EMPTY;
                (void) register_user(fp, shared->username, shared->password);
                (void) fprintf(stdout, "User registered successfully.\n");
            }
        }
        /** log in a user */
        else if (user_option == LOGIN)
        {                   
            /** checks, whether a user is registered or not. */   
            check_user = check_for_user(fp, databasefp, shared->username, shared->password, shared->secret, LOGIN);
            if (check_user == USER_UNREGISTERED)
            {
                (void)printf("No such user.\n"); 
                shared->flag = ERROR;
            }
            else
            {
                number_of_users++;
                shared->flag = EMPTY;
                (void) fprintf(stdout, "User logged in successfully.\n");
            }            
        }
        else if (user_option == READ_SECRET)
        {
                shared->flag = EMPTY;
                
                (void) check_for_user(fp, databasefp, shared->username, shared->password, shared->secret, READ_SECRET);
                
                (void) printf("Secret read successfully.\n");
        }
        else if (user_option == WRITE_SECRET)
        {                
            shared->flag = EMPTY;           
            (void) check_for_user(fp, databasefp, shared->username, shared->password, shared->secret, WRITE_SECRET);           
            
            (void) printf("Secret written successfully.\n");
        
        }
        else if (user_option == LOGOUT)
        {
            number_of_users--;
            shared->flag = EMPTY;
            (void) printf("User logged out successfully.\n");
        }
        else if (user_option == ERROR)
        {
            number_of_users--;
        }

        if (sem_post(sem2) == -1)
        {
            (void) on_error("Problem while executing sem_post(3).");
        }
    }    
    
    if (want_quit == 1)
    {
        if (sem_wait(sem1) == -1)
        {
            (void) on_error("Problem while executing sem_wait(3).");
        }
        
        shared->flag = SERVER_ERROR;
        
        if (sem_post(sem2) == -1)
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
    
    /** Close the semaphores */
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
    
    (void) write_to_database(fp, databasefp);
    
    (void) fclose(fp);
    
    (void) fclose(databasefp);
    
    return EXIT_SUCCESS;
}

/**
* @brief prints usage info.
* @param name the name of the program.
*/
void usage(char* name)
{
    (void) fprintf(stderr, "USAGE: %s [-l database]\n", name);
    
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
* @brief frees the shared ressources.
*/
void free_resources(void)
{ 
    /** Unlink the semaphores */
    if (sem_unlink(SEM_1) == -1)
    {
        (void) fprintf(stderr, "Problem while executing sem_unlink(3).\n");
    }
    
    if (sem_unlink(SEM_2) == -1)
    {
        (void) fprintf(stderr, "Problem while executing sem_unlink(3).\n");
    }
    
    if (sem_unlink(SEM_3) == -1)
    {
        (void) fprintf(stderr, "Problem while executing sem_unlink(3).\n");
    }
    
    /** Unlink the shared memory */
    if (shm_unlink(SHM_NAME) == -1)
    {
        (void) fprintf(stderr, "Problem while executing shm_unlink(3).\n");
    }
}

/**
*   @brief Checks whether a user is registered or not.
*   @param fp The current text file for IO.
*   @param database The database.
*   @param input_username The user name.
*   @param input_password The user password.
*   @param secret The user secret.
*   @param flag The option with which the method is called.
*   @return USER_REGISTERED or USER_UNREGISTERED 
*/
int check_for_user(FILE* fp, FILE* database, char* input_username, char* input_password, char *secret, int flag)
{
    char usrn[MAX_DATA+1];
    char psswd[MAX_DATA+1];
    char scrt[MAX_DATA+1];
    char c1;
    int k = 0;
    int i = 0;
    int count = 0;
    int semicount = 0;

    (void) rewind(fp);

    while ((c1 = fgetc(fp)) != EOF)
    {
        count++;
        int i1 = (int)c1;
        
        if (i1 != 10 && i1 != 59)
        {
            if(k == 0)
            {
                usrn[i] = c1;
                i++;
            }
            else if (k == 1)
            {
                psswd[i] = c1;
                i++;
            }
            else
            {
                scrt[i] = c1;
                i++;
            }   
        }
        /** \n */
        else if(i1 == 10)
        {
            scrt[i] = '\0';
            k = 0;
            i = 0;
                        
            int check_usrn = strcmp(input_username, &usrn[0]);
            int check_psswd = strcmp(input_password, &psswd[0]);
                               
            if (check_usrn == 0 && check_psswd == 0)
            {
            
                if (flag == READ_SECRET)
                {
                    (void) strcpy(secret, scrt);
                }
                else if (flag == WRITE_SECRET)
                {
                    (void) write_secret(fp, count, semicount, secret);
                }
                
                return USER_REGISTERED;
            }
        }
        /** ; */
        else if (i1 == 59)
        {
            semicount = count;
            if(k == 0)
            {
                usrn[i] = '\0';
            }
            else if (k == 1)
            {
                psswd[i] = '\0';
            }
            
            k++;
            i = 0;
        }
    }    
    
    return USER_UNREGISTERED;
}

/**
*   @brief Saves the secret to the current user.
*   @param fp The current text file for IO.
*   @param count The end of the secret.
*   @param semicount The start of the secret.
*   @param secret The secret to be written.
*/
void write_secret(FILE *fp, int count, int semicount, char *secret)
{
    (void) rewind(fp);
    (void) fseek(fp, count, SEEK_SET);   
    int max = 4096;
    
    char buffer[max];    
    int j = 0;
    
    char c1;
    
    while((c1 = fgetc(fp)) != EOF && j < max)
    {
        buffer[j] = c1;
        j++;
    }
    
    buffer[++j] = '\0';
    
    (void) fseek(fp, semicount, SEEK_SET);  
    
    for(j = 0; j < MAX_DATA; j++)
    {
        if(secret[j] == '\0' || ((int)secret[j]) <= 0)
        {
            break;
        }
        else if (((int)secret[j]) != 92 && secret[j] != '\n' && secret[j] != '\t')
        {
            (void) fputc(secret[j], fp);
        }
    }
    
    (void) fputc('\n', fp);
    
    for(j = 0; j < max; j++)
    {
        if(buffer[j] == '\0')
        {
            break;
        }
        
        (void) fputc(buffer[j], fp);
    }
                        
    for(j = 0; j < 30; j++)
    {                        
        (void) fputc('*', fp);
    }
    (void) fputc('\n', fp);
}

/**
*   @brief Writes all the info from the text file to the database.
*   @param fp The text file.
*   @param database The database.
*/
void write_to_database(FILE *fp, FILE *database)
{
    char c;
    
    (void) rewind(fp);
    
    while ((c = fgetc(fp)) != EOF)
    {
        if (c != '*')
        {
            (void) fputc(c, database);
        }
    }
}

/**
*   Method for registering a new user. 
*   @brief The new user will be saved in a temporary file before saved to the database.
*   @param fp The temporary file.
*   @param input_username The username of the new user.
*   @param input_password The password of the new user.
*   @return void.
*/
void register_user(FILE* fp, char* input_username, char* input_password)
{
    int i;
    for(i = 0; i < MAX_DATA; i++)
    {
        if (*(input_username + i) == '\0')
        {
            break;
        }
        
        (void) fputc(*(input_username + i), fp);
    }
    
    fputc(';', fp);
    
    for(i = 0; i < MAX_DATA; i++)
    {
        if (*(input_password + i) == '\0')
        {
            break;
        }
        
        (void) fputc(*(input_password + i), fp);
    }
    
    fputc(';', fp);
    fputc('\n', fp);
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

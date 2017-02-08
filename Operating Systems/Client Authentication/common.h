#define SEM_1       "/1328036sem_1"
#define SEM_2       "/1328036sem_2"
#define SEM_3       "/1328036sem_3"

#define SHM_NAME    "/1328036myshm"

#define MAX_DATA   (50)
#define PERMISSION (0600)

struct data
{
    char username[MAX_DATA+1];
    char password[MAX_DATA+1];
    char secret[MAX_DATA+1];
    int flag;
};

enum flag 
{
    ERROR = 0,
    REGISTER = 1,
    LOGIN = 2,
    LOGOUT = 3,
    READ_SECRET = 4,
    WRITE_SECRET = 5,
    EMPTY = 10,
    SERVER_ERROR = 11
};

enum user
{
    USER_UNREGISTERED,
    USER_REGISTERED    
};

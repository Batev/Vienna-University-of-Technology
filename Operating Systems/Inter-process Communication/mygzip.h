/**
 * @file mygzip.h
 * @author Evgeni Batev <e1328036@student.tuwien.ac.at>
 * @date 27.11.2016
 *  
 * @brief Provides functions for the execution of the child processes.
 *
 * It contains generic functions related to standard in and output.
 */

#ifndef MYGZIP_H_ /* prevent multiple inclusion */
#define MYGZIP_H_

#define BUFFER_SIZE 256 /** defines a default buffer size for the buffered operations */

/**
* Executes the operations for the first child process.
* @brief The function takes two pipes as an input and sets their end
* appropriately. Then calls execlp and starts the gzip process.
* @details The program does not check whether an error has occurred
* while closing the pipe ends.
* @param pipefd1 The first pipe for the communication with the other process.
* @param pipefd2 The second pipe for the communication with the other process.
*/
int execute_child1(int *pipefd1, int *pipefd2);

/**
* Executes the operations for the second child process.
* @brief The function takes a file if presented and writes to 
* it the compressed data from the other process.
* @details The program does not check whether an error has occurred
* while closing the pipe ends and the file.
* @param pipefd2 The second pipe for the communication with the other process.
* @param filename The name of the file where the data is going to be written.
* If null than write to stdout.
*/
int execute_child2(int *pipefd2, const char *filename);

#endif /** MYGZIP_H_ */

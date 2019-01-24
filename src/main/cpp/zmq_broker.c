#include "zmq.h"
#include <errno.h>
#include <stdlib.h>
#include <string.h>

#define RECV_BUFFER_SIZE 1024

typedef struct recv_msg_t {
    char client_id_buffer[RECV_BUFFER_SIZE];
    char address_id_buffer[RECV_BUFFER_SIZE];
    char data_buffer[RECV_BUFFER_SIZE];
} recv_msg_t;

int recv_msg(void *socket, char *recv_buffer) {
    int maxSize = RECV_BUFFER_SIZE;
    int size = zmq_recv(socket, recv_buffer, maxSize, 0);
    if (size == -1) {
        perror("recv_msg error: ");
        return 0;
    }
    if (size > maxSize) {
        size = maxSize;
    }
    return size;
}

int main(void) {
    recv_msg_t recv_msg_data;

    //  Prepare our context and sockets
    void *context = zmq_ctx_new();
    void *sender = zmq_socket(context, ZMQ_ROUTER);
    void *receiver = zmq_socket(context, ZMQ_ROUTER);

    zmq_bind(sender, "tcp://*:5211");
    zmq_bind(receiver, "tcp://*:5210");

    zmq_pollitem_t items[] = {
        { sender, 0, ZMQ_POLLIN, 0 }
    };
    while (1) {
        int rc = zmq_poll(items, 1, -1);
        if (rc == -1) {
            // Interrupted
            printf("Interrupted.\n");
            break;
        }

        //  Handle activity on sender
        if (items[0].revents & ZMQ_POLLIN) {
            //  Client request is [identity][address][data]
            int client_id_size = recv_msg(sender, recv_msg_data.client_id_buffer);
            int address_id_size = recv_msg(sender, recv_msg_data.address_id_buffer);
            int data_size = recv_msg(sender, recv_msg_data.data_buffer);
            
            //printf("[%s][%s][%s]\n", recv_msg_data.address_id_buffer, recv_msg_data.client_id_buffer, recv_msg_data.data_buffer);
            zmq_send(receiver, recv_msg_data.address_id_buffer, address_id_size, ZMQ_SNDMORE);
            zmq_send(receiver, recv_msg_data.client_id_buffer, client_id_size, ZMQ_SNDMORE);
            zmq_send(receiver, recv_msg_data.data_buffer, data_size, 0);
        }
    }
    zmq_close(sender);
    zmq_close(receiver);
    zmq_ctx_destroy(context);
    return 1;
}
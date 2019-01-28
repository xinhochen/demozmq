#include "zmq.h"
#include <errno.h>
#include <stdlib.h>
#include <assert.h>
#include <unistd.h>

#define MSG_PART 3

int recv_msg(void *socket, zmq_msg_t *message) {
    int rc;
    int more;
    int count = 0;
    do {
        ++count;
        if (count > 3) {
            break;
        }

        rc = zmq_msg_recv(message, socket, 0);
        if (rc == -1) {
            perror("recv_msg error: ");
            return 0;
        }

        more = zmq_msg_more(message++);
    } while(more);
    if (count != 3) {
        printf("message struct error.\n");
        return 0;
    }
    return 1;
}

void send_msg(void *socket, zmq_msg_t *message) {
    zmq_msg_send(&message[1], socket, ZMQ_SNDMORE);
    zmq_msg_send(&message[0], socket, ZMQ_SNDMORE);
    zmq_msg_send(&message[2], socket, 0);
}

void init_msg(zmq_msg_t *message) {
    int i;
    for (i = 0; i < MSG_PART; i++) {
        zmq_msg_init(&message[i]);
    }
}

void free_msg(zmq_msg_t *message) {
    int i;
    for (i = 0; i < MSG_PART; i++) {
        zmq_msg_close(&message[i]);
    }
}

void configSocket(void *socket) {
    int mandatory = 1;
    int rc = zmq_setsockopt(socket, ZMQ_ROUTER_MANDATORY, &mandatory, sizeof(mandatory));
    assert(rc == 0);

    int handover = 1;
    rc = zmq_setsockopt(socket, ZMQ_ROUTER_HANDOVER, &handover, sizeof(handover));
    assert(rc == 0);

    int immediate = 1;
    rc = zmq_setsockopt(socket, ZMQ_IMMEDIATE, &immediate, sizeof(immediate));
    assert(rc == 0);
}

int main(void) {
    if (daemon(1, 1) == -1) {
        printf("error: create daemon failed!\n");
        return 1;
    }

    //  Prepare our context and sockets
    void *context = zmq_ctx_new();
    void *sender = zmq_socket(context, ZMQ_ROUTER);
    void *receiver = zmq_socket(context, ZMQ_ROUTER);

    configSocket(receiver);

    zmq_bind(sender, "tcp://*:5211");
    zmq_bind(receiver, "tcp://*:5210");

    zmq_pollitem_t items[] = {
        { sender, 0, ZMQ_POLLIN, 0 }
    };

    zmq_msg_t msg[MSG_PART];
    init_msg(msg);

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
            int rc = recv_msg(sender, msg);
            if (rc == 0) {
                continue;
            }

            send_msg(receiver, msg);
        }
    }

    free_msg(msg);

    zmq_close(sender);
    zmq_close(receiver);
    zmq_ctx_destroy(context);
    return 1;
}
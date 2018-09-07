#include <stdlib.h>
#include "Queue.h"

/**
 * 队列，这里主要用于存放AVPacket的指针
 * 这里使用生产者消费者模式来使用队列，只要需要两个队列实例，分别用来存储音频AVPacket和视频AVPacket
 * 1.生产者：read_stream线程负责不断的读取视频文件中的AVPacket，分别放入两个队列
 * 2.消费者
 * 2.1）视频解码，从视频AVPacket Queue中获取元素，解码，绘制
 * 2.2）音频解码，从音频AVPacket Queue中获取元素，解码，播放
*/
struct _Queue {
    // 长度
    int size;

    // 任意类型的指针数组，这里每一个元素都是AVPacket指针，总共有size个
    // AVPacket **packets; 如果设置为void**类型就可以兼容其他类型
    void **tab;

    // push或者pop元素时需要按照先后顺序，依次进行
    int next_to_write;
    int next_to_read;

    int *ready;
};


/**
 * 初始化队列
 * @param size 队列的长度
 * @return 初始化好并分配好内存的队列
 */
Queue *queue_init(int size) {
    Queue *queue = (Queue *) malloc(sizeof(Queue));
    queue->size = size;
    queue->next_to_read = 0;
    queue->next_to_write = 0;
    // 为数组开辟内存
    queue->tab = malloc(sizeof(*queue->tab) * size);
    // 为数组的每一个元素开辟内存
    int i = 0;
    for (; i < size; ++i) {
        queue->tab[i] = malloc(sizeof(*queue->tab));
    }
    return queue;
}


/**
 * 释放队列内存
 * @param free_func 释放队列元素内存的函数指针，可以自定义释放元素内存的逻辑
 */
void queue_free(Queue *queue, queue_free_func free_func) {
    // 释放队列每一个元素的内存
    int i = 0;
    for (; i < queue->size; ++i) {
        free_func((void *) queue->tab[i]);
    }
    // 释放队列中的数组内存
    free(queue->tab);
    // 释放队列内存
    free(queue);
}

/**
 * 获取队列中数组下一个索引位置
 * @param current 队列中数组的当前索引位置
 */
int queue_get_next(Queue *queue, int current) {
    return (current + 1) % queue->size;
}

/**
 * 获取队列队尾的元素指针，通过将该指针指向要添加到队列的元素即可完成入队
 * @return 队列队尾的元素指针
 */
void *queue_push(Queue *queue) {
    int current = queue->next_to_write;
    queue->next_to_write = queue_get_next(queue, current);
    return queue->tab[current];
}


/**
 * 获取队列的队首指针，通过这个指针就可以获取到元素了
 * @return 队列的队首指针
 */
void *queue_pop(Queue *queue) {
    int current = queue->next_to_read;
    queue->next_to_read = queue_get_next(queue, current);
    return queue->tab[current];
}


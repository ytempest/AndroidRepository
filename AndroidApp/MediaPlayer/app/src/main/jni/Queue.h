#ifndef AUDIOVIDEODECODE_QUEUE_H
#define AUDIOVIDEODECODE_QUEUE_H
typedef struct _Queue Queue;

// 释放队列中元素所占用的内存的函数
typedef void *(*queue_free_func)(void *elem);

/**
 * 初始化队列
 * @param size 队列的长度
 * @return 初始化好并分配好内存的队列
 */
Queue *queue_init(int size);

/**
 * 释放队列内存
 * @param free_func 释放队列元素内存的函数指针，可以自定义释放元素内存的逻辑
 */
void queue_free(Queue *queue, queue_free_func free_func);

/**
 * 获取队列中数组下一个索引位置
 * @param current 队列中数组的当前索引位置
 */
int queue_get_next(Queue *queue, int current);

/**
 * 获取队列队尾的元素指针，通过将该指针指向要添加到队列的元素即可完成入队
 * @return 队列队尾的元素指针
 */
void *queue_push(Queue *queue);

/**
 * 获取队列的队首指针，通过这个指针就可以获取到元素了
 * @return 队列的队首指针
 */
void *queue_pop(Queue *queue);

#endif //AUDIOVIDEODECODE_QUEUE_H

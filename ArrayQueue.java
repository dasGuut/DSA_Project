class ArrayQueue<T> {
    private T[] queue;
    private int front;
    private int rear;
    private int size;
    private int capacity;

    // Default constructor: size 100
    @SuppressWarnings("unchecked")
    public ArrayQueue() {
        capacity = 100;
        queue = (T[]) new Object[capacity];
        front = 0;
        rear = -1;
        size = 0;
    }
    
    // User-defined constructor: custom size
    @SuppressWarnings("unchecked")
    public ArrayQueue(int size) {
        capacity = size;
        queue = (T[]) new Object[capacity];
        front = 0;
        rear = -1;
        this.size = 0;
    }

    // Enqueue: add an element to the rear
    public void enqueue(T item) {
        if (isFull()) {
            throw new RuntimeException("Queue Overflow");
        }
        rear = (rear + 1) % capacity;
        queue[rear] = item;
        size++;
    }

    // Dequeue: remove and return the element from the front
    public T dequeue() {
        if (isEmpty()) {
            throw new RuntimeException("Queue Underflow");
        }
        T item = queue[front];
        front = (front + 1) % capacity;
        size--;
        return item;
    }

    // Peek: get the element at the front without removing
    public T peek() {
        if (isEmpty()) {
            throw new RuntimeException("Queue is Empty");
        }
        return queue[front];
    }

    // Front: return the front element (without removing it)
    public T front() {
        if (isEmpty()) {
            throw new RuntimeException("Queue is Empty");
        }
        return queue[front];
    }

    // Back: return the back element (without removing it)
    public T back() {
        if (isEmpty()) {
            throw new RuntimeException("Queue is Empty");
        }
        return queue[rear];
    }

    // isEmpty: Check if the queue is empty
    public boolean isEmpty() {
        return size == 0;
    }

    // isFull: Check if the queue is full
    public boolean isFull() {
        return size == capacity;
    }

    // Get the current number of elements in the queue
    public int size() {
        return size;
    }
}

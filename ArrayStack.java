
public class ArrayStack<T> {
private T[] stack;
private int top;
private int capacity;
// Default constructor: size 100
@SuppressWarnings("unchecked")
public ArrayStack() {
capacity = 100;
stack = (T[]) new Object[capacity];
top = -1;
}
// User-defined constructor: custom size
@SuppressWarnings("unchecked")
public ArrayStack(int size) {
capacity = size;
stack = (T[]) new Object[capacity];
top = -1;
}
public void push(T item) {
if (top == capacity - 1) {
throw new RuntimeException("Stack Overflow");
}
stack[++top] = item;
}
public T pop() {
if (isEmpty()) {
throw new RuntimeException("Stack Underflow");
}
return stack[top--];
}
public T peek() {
if (isEmpty()) {
throw new RuntimeException("Stack is Empty");
}
return stack[top];
}
public boolean isEmpty() {
return top == -1;
}
public int size() {
return top + 1;
}
}

package com.simon.credit.toolkit.core;

/**
 * 双向链表
 * @author xuziming 2021-006-10
 */
public class DoubleLinkedList<T> {

    private class Node<T> {
        private T data;
        private Node next;// 后继结点
        private Node prev;// 前驱结点

        public Node(T data) {
            this.data = data;
        }
    }

    private Node<T> head;// 头结点
    private Node<T> tail;// 尾结点
    private int     size;// 链表长度

    public DoubleLinkedList() {
        head = new Node<T>(null);
        tail = head;
        size = 0;
    }

    public DoubleLinkedList(T data) {
        head = new Node<T>(data);
        tail = head;
        size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void add(T data) {
        if (isEmpty()) {
            head = new Node<>(data);
            tail = head;
        } else {
            Node<T> temp = new Node<>(data);
            temp.prev = tail;
            tail.next = temp;
            tail = temp;
        }
        size++;
    }

    public void addAfter(T data, T insertData) {
        Node<T> cursor = head;
        while (cursor != null) {
            if (cursor.data.equals(data)) {
                Node<T> temp = new Node<>(insertData);
                temp.prev = cursor;
                temp.next = cursor.next;
                cursor.next = temp;

                if (temp.next == null) {
                    tail = temp;
                } else {
                    temp.next.prev = temp;
                }

                size++;
                break;
            }
            cursor = cursor.next;
        }
    }

    public void remove(T data) {
        Node<T> cursor = head;
        while (cursor != null) {
            if (cursor.data.equals(data)) {
                if (cursor.prev != null) {
                    cursor.prev.next = cursor.next;
                } else {
                    head = cursor.next;
                }

                if (cursor.next == null) {
                    tail = cursor.prev;
                } else {
                    cursor.next.prev = cursor.prev;
                }

                size--;
                break;
            }
            cursor = cursor.next;
        }
    }

    public int size() {
        return size;
    }

    public void printFromHead() {
        System.out.println("=== 正序：");
        Node<T> cursor = head;
        for (int i = 0; i < size; i++) {
            System.out.print(cursor.data + " ");
            cursor = cursor.next;
        }
        System.out.println("\r\n");
    }

    public void printFromTail() {
        System.out.println("=== 倒序：");
        Node<T> temp = tail;
        for (int i = 0; i < size; i++) {
            System.out.print(temp.data + " ");
            temp = temp.prev;
        }
        System.out.println("\r\n");
    }

    public static void main(String[] args) {
        DoubleLinkedList<Integer> link = new DoubleLinkedList<>();
        link.add(1);
//        link.add(2);
//        link.add(3);
//        link.add(4);
//        link.add(5);
//        link.add(6);
//        link.add(7);

//        System.out.println("=== 原始数据");
//        link.printFromHead();
//        link.printFromTail();
//
        System.out.println("=== 在3后面添加一个数据");
        link.addAfter(1, 99);
        link.printFromHead();
        link.printFromTail();
//
//        System.out.println("=== 移除一个数据");
//        link.remove(5);
//        link.printFromHead();
//        link.printFromTail();

        for (int i = 8; i > 0; i--) {
            link.remove(i);
            link.printFromHead();
        }
    }

}
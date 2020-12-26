package com.company;

public class Main {
    public static void main(String[] args) {
        new Server((request, response) -> "<html><body><h1>Handler is working</h1></body></html>").bootstrap();
    }
}


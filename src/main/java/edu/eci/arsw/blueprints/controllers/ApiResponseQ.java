package edu.eci.arsw.blueprints.controllers;
public record ApiResponseQ<T>(int code, String message, T data) {}

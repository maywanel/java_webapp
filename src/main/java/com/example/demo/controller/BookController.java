package com.example.demo.controller;

import com.example.demo.service.BookService;
import com.example.demo.model.Book;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty() || 
            book.getAuthor() == null || book.getAuthor().trim().isEmpty() ||
            book.getDescription() == null || book.getDescription().trim().isEmpty() || 
            book.getPrice() <= 0 || book.getPrice() > 999999.99 ||
            book.getStock() < 0 || book.getStock() > 999999) {
            return ResponseEntity.badRequest().body("Invalid book data");
        }
        if (book.getTitle().length() > 255 || book.getAuthor().length() > 255 || 
            book.getDescription().length() > 2000) {
            return ResponseEntity.badRequest().body("Input exceeds maximum length");
        }
        if (bookService.getBookByName(book.getTitle().trim()) != null) {
            return ResponseEntity.status(409).body("Book with the same title already exists");
        }
        book.setTitle(book.getTitle().trim());
        book.setAuthor(book.getAuthor().trim());
        book.setDescription(book.getDescription().trim());
        bookService.saveBook(book);
        return ResponseEntity.ok("Book has been added");
    }

    @GetMapping
    public ResponseEntity<?> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody Book book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty() || 
            book.getAuthor() == null || book.getAuthor().trim().isEmpty() ||
            book.getDescription() == null || book.getDescription().trim().isEmpty() || 
            book.getPrice() <= 0 || book.getPrice() > 999999.99 ||
            book.getStock() < 0 || book.getStock() > 999999) {
            return ResponseEntity.badRequest().body("Invalid book data");
        }
        if (book.getTitle().length() > 255 || book.getAuthor().length() > 255 || 
            book.getDescription().length() > 2000) {
            return ResponseEntity.badRequest().body("Input exceeds maximum length");
        }
        book.setTitle(book.getTitle().trim());
        book.setAuthor(book.getAuthor().trim());
        book.setDescription(book.getDescription().trim());
        Book updatedBook = bookService.updateBook(id, book);
        if (updatedBook == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Book updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        if (bookService.getBookById(id) == null) {
            return ResponseEntity.status(404).body("Book not found");
        }
        bookService.deleteBook(id);
        return ResponseEntity.ok("Book deleted successfully");
    }
}
